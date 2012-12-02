/*
    Copyright (C) 2012 Sweetie Piggy Apps <sweetiepiggyapps@gmail.com>

    This file is part of Raspberry Bus Malaysia.

    Raspberry Bus Malaysia is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    Raspberry Bus Malaysia is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Raspberry Bus Malaysia; if not, see <http://www.gnu.org/licenses/>.
*/

package com.sweetiepiggy.raspberrybusmalaysia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbAdapter
{
	public static final String KEY_ROWID = "_id";
	public static final String KEY_AGENT = "company";
	public static final String KEY_OPERATOR = "bus_brand";
	public static final String KEY_FROM_CITY = "from_city";
	public static final String KEY_FROM_STN = "from_station";
	public static final String KEY_TO_CITY = "to_city";
	public static final String KEY_TO_STN = "to_station";
	public static final String KEY_FROM_STN_ID = "from_station_id";
	public static final String KEY_TO_STN_ID = "to_station_id";
	public static final String KEY_SCHED_DEP = "scheduled_departure";
	public static final String KEY_ACTUAL_DEP = "actual_departure";
	public static final String KEY_ARRIVAL = "arrival_time";
	public static final String KEY_CTR = "counter";
	public static final String KEY_SAFETY = "safety";
	public static final String KEY_COMFORT = "comfort";
	public static final String KEY_OVERALL = "overall";
	public static final String KEY_COMMENT = "comment";
	public static final String KEY_LAST_UPDATE = "comment";
	public static final String KEY_CITY = "city";
	public static final String KEY_CITY_ID = "city_id";
	public static final String KEY_CITY_EN = "city_en";
	public static final String KEY_CITY_MS = "city_ms";
	public static final String KEY_CITY_ZH = "city_zh";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_STN = "station";
	public static final String KEY_STN_EN = "station_en";
	public static final String KEY_STN_MS = "station_ms";
	public static final String KEY_STN_ZH = "station_zh";
	public static final String KEY_REG = "reg";

	public static final String TRIP_TIME = "(strftime('%s', " + KEY_ARRIVAL + ") - strftime('%s', " + KEY_SCHED_DEP + "))";
	public static final String TRIP_DELAY = "(strftime('%s', " + KEY_ACTUAL_DEP + ") - strftime('%s', " + KEY_SCHED_DEP + "))";
	public static final String AVG_TIME = "avg(" + TRIP_TIME + ")";
	public static final String AVG_DELAY = "avg(" + TRIP_DELAY + ")";
	public static final String NUM_TRIPS = "count(" + KEY_AGENT + ")";

	public static final String TABLE_TRIPS = "trips";
	public static final String TABLE_CITIES = "cities";
	public static final String TABLE_STATIONS = "stations";

	private static HashMap<String, ArrayList<String>> TABLE_MAPS = new HashMap<String, ArrayList<String>>();

	private static final int SEC_BETWEEN_UPDATES = 60 * 60 * 24 * 7;

//	private static final String TAG = "DbAdapter";
	private DatabaseHelper mDbHelper;

	private static final String DATABASE_NAME = "rbm.db";
	/** "tmp" table was originally named "submit" in db-v7 / rbm-v0.1.1 */
	private static final String TABLE_TMP = "tmp";
	private static final String TABLE_TMP_COMPLAINT = "tmp_complaint";
	private static final String TABLE_LAST_UPDATE = "last_update";
	private static final int DATABASE_VERSION = 14;

	private static final String DATABASE_CREATE_TRIPS =
		"CREATE TABLE " + TABLE_TRIPS + " (" +
		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		KEY_AGENT + " TEXT, " +
		KEY_OPERATOR + " TEXT, " +
		KEY_FROM_STN_ID + " INTEGER, " +
		KEY_TO_STN_ID + " INTEGER, " +
		KEY_SCHED_DEP + " TEXT NOT NULL, " +
		KEY_ACTUAL_DEP + " TEXT NOT NULL, " +
		KEY_ARRIVAL + " TEXT NOT NULL, " +
		KEY_CTR + " TEXT, " +
		KEY_SAFETY + " INTEGER, " +
		KEY_COMFORT + " INTEGER, " +
		KEY_OVERALL + " INTEGER, " +
		KEY_COMMENT + " TEXT, " +
		"FOREIGN KEY(" + KEY_FROM_STN_ID + ") REFERENCES " + TABLE_STATIONS + "(" + KEY_ROWID + "), " +
		"FOREIGN KEY(" + KEY_TO_STN_ID + ") REFERENCES " + TABLE_STATIONS + "(" + KEY_ROWID + "));";

	private static final String DATABASE_CREATE_TMP =
		"CREATE TABLE " + TABLE_TMP + " (" +
		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		KEY_AGENT + " TEXT, " +
		KEY_OPERATOR + " TEXT, " +
		KEY_FROM_CITY + " TEXT, " +
		KEY_FROM_STN + " TEXT, " +
		KEY_TO_CITY + " TEXT, " +
		KEY_TO_STN + " TEXT, " +
		KEY_SCHED_DEP + " TEXT, " +
		KEY_ACTUAL_DEP + " TEXT, " +
		KEY_ARRIVAL + " TEXT, " +
		KEY_CTR + " TEXT, " +
		KEY_SAFETY + " INTEGER, " +
		KEY_COMFORT + " INTEGER, " +
		KEY_OVERALL + " INTEGER, " +
		KEY_COMMENT + " TEXT);";

	private static final String DATABASE_CREATE_TMP_COMPLAINT =
		"CREATE TABLE " + TABLE_TMP_COMPLAINT + " (" +
		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		KEY_AGENT + " TEXT, " +
		KEY_OPERATOR + " TEXT, " +
		KEY_FROM_CITY + " TEXT, " +
		KEY_FROM_STN + " TEXT, " +
		KEY_TO_CITY + " TEXT, " +
		KEY_TO_STN + " TEXT, " +
		KEY_SCHED_DEP + " TEXT, " +
		KEY_CTR + " TEXT, " +
		KEY_REG + " TEXT, " +
		KEY_COMMENT + " TEXT);";

	private static final String DATABASE_CREATE_LAST_UPDATE =
		"CREATE TABLE " + TABLE_LAST_UPDATE + " (" +
		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		KEY_LAST_UPDATE + " TEXT);";

	private static final String DATABASE_CREATE_CITIES =
		"CREATE TABLE " + TABLE_CITIES + " (" +
		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		KEY_CITY_EN + " TEXT UNIQUE, " +
		KEY_CITY_MS + " TEXT UNIQUE, " +
		KEY_CITY_ZH + " TEXT UNIQUE);";

	private static final String DATABASE_CREATE_STATIONS =
		"CREATE TABLE " + TABLE_STATIONS + " (" +
		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		KEY_CITY_ID + " INTEGER, " +
		KEY_STN_EN + " TEXT, " +
		KEY_STN_MS + " TEXT, " +
		KEY_STN_ZH + " TEXT, " +
		KEY_LATITUDE + " INTEGER, " +
		KEY_LONGITUDE + " INTEGER, " +
		"FOREIGN KEY(" + KEY_CITY_ID + ") REFERENCES " + TABLE_CITIES + "(" + KEY_ROWID + "));";

	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		private final Context mCtx;
		private boolean mAllowSync;
		public SQLiteDatabase mDb;

		DatabaseHelper(Context context, boolean allow_sync)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mCtx = context;
			mAllowSync = allow_sync;
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TMP);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TMP_COMPLAINT);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAST_UPDATE);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATIONS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES);
			db.execSQL(DATABASE_CREATE_CITIES);
			db.execSQL(DATABASE_CREATE_STATIONS);
			db.execSQL(DATABASE_CREATE_TRIPS);
			db.execSQL(DATABASE_CREATE_TMP_COMPLAINT);
			db.execSQL(DATABASE_CREATE_TMP);
			db.execSQL(DATABASE_CREATE_LAST_UPDATE);
			if (mAllowSync) {
				SyncTask sync = new SyncTask(mCtx);
				sync.execute();

				Cursor c = db.rawQuery("SELECT strftime(\"%Y-%m-%d %H:%M:%S\", 'now')", null);
				if (!c.moveToFirst()) {
					c.close();
					return;
				}
				String last_update = c.getString(0);
				c.close();

				ContentValues cv = new ContentValues();
				cv.put(KEY_LAST_UPDATE, last_update);

				db.delete(TABLE_LAST_UPDATE, null, null);
				db.insert(TABLE_LAST_UPDATE, null, cv);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int old_ver, int new_ver)
		{
			switch (old_ver) {
			case 11:
				db.execSQL(DATABASE_CREATE_TMP_COMPLAINT);
			case 12:
				db.execSQL("ALTER TABLE trips ADD COLUMN overall INTEGER;");
				db.execSQL("ALTER TABLE tmp ADD COLUMN overall INTEGER;");
				break;
			/* removed from_city_id and to_city_id columns from trips table */
			case 13:
				break;
			default:
				onCreate(db);
				break;
			}
		}

		public void open()
		{
			mDb = getReadableDatabase();
		}

		public void open_readwrite() throws SQLException
		{
			mDb = getWritableDatabase();
		}

		@Override
		public synchronized void close()
		{
			if (mDb != null) {
				mDb.close();
			}
			super.close();
		}
	}

	public DbAdapter()
	{
	}

	public DbAdapter open(Context ctx)
	{
		mDbHelper = new DatabaseHelper(ctx, false);
		mDbHelper.open();
		return this;
	}

	public DbAdapter open_readwrite(Context ctx) throws SQLException
	{
		return open_readwrite(ctx, true);
	}

	public DbAdapter open_readwrite(Context ctx, boolean allow_sync) throws SQLException
	{
		mDbHelper = new DatabaseHelper(ctx, allow_sync);
		mDbHelper.open_readwrite();
		return this;
	}

	public void check_last_update_and_sync()
	{
		if (sec_since_last_update() > SEC_BETWEEN_UPDATES) {
			SyncTask sync = new SyncTask(mDbHelper.mCtx);
			sync.execute();
			set_last_update();
		}
	}


	public void close()
	{
		mDbHelper.close();
	}

	/** @return row_id or -1 if failed */
	public long create_city(ContentValues city)
	{
		return replace(city, TABLE_CITIES);
	}

	/** @return row_id or -1 if failed */
	public long create_station(ContentValues station)
	{
		return replace(station, TABLE_STATIONS);
	}

	/** @return row_id or -1 if failed */
	public long create_trip(ContentValues trip)
	{
		return replace(trip, TABLE_TRIPS);
	}

	/** @return row_id or -1 if failed */
	public long replace(ContentValues cv, String table)
	{
		rm_unknown_cols(cv, table);
		return mDbHelper.mDb.replace(table, null, cv);
	}

	private void rm_unknown_cols(ContentValues cv, String table)
	{
		ArrayList<String> col_names;

		if (TABLE_MAPS.containsKey(table)) {
			col_names = TABLE_MAPS.get(table);
		} else {
			Cursor c = mDbHelper.mDb.query(true, table, null, null, null,
					null, null, null, "1");
			col_names = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
			TABLE_MAPS.put(table, col_names);
		}

		ArrayList<String> to_remove = new ArrayList<String>();

		Iterator<Entry<String, Object>> itr = cv.valueSet().iterator();
		while (itr.hasNext()) {
			String key = itr.next().getKey().toString();
			if (!col_names.contains(key)) {
				to_remove.add(key);
			}
		}
		Iterator<String> to_rm_itr = to_remove.iterator();
		while (to_rm_itr.hasNext()) {
			cv.remove(to_rm_itr.next());
		}
	}

	/** @return cities that exist as departure point in submitted trips */
	public Cursor fetch_from_cities()
	{
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		return mDbHelper.mDb.rawQuery("SELECT DISTINCT " + TABLE_TRIPS +
				"." + KEY_ROWID + " AS " + KEY_ROWID + ", " + key_city + " AS " + KEY_FROM_CITY +
				" FROM " + TABLE_TRIPS + " JOIN " + TABLE_STATIONS +
				" ON " + TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				TABLE_STATIONS + "." + KEY_ROWID +
				" JOIN " + TABLE_CITIES +
				" ON " + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID +
				" GROUP BY " + key_city +
				" ORDER BY " + key_city + " ASC",
			null);
	}

	/** @return stations that exist as departure point in submitted trips */
	public Cursor fetch_from_stations()
	{
		String key_station = KEY_STN + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		return mDbHelper.mDb.rawQuery("SELECT DISTINCT " + TABLE_TRIPS +
				"." + KEY_ROWID + " AS " + KEY_ROWID + ", " +
				key_station + " AS " + KEY_STN + ", " +
				key_city + " AS " + KEY_CITY + ", " +
				KEY_LATITUDE + ", " + KEY_LONGITUDE +
				" FROM " + TABLE_TRIPS + " JOIN " + TABLE_STATIONS +
				" on " + TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				TABLE_STATIONS + "." + KEY_ROWID +
				" JOIN " + TABLE_CITIES + " on " +
				KEY_CITY_ID + " == " + TABLE_CITIES + "." + KEY_ROWID +
				" GROUP BY " + key_station +
				" ORDER BY " + key_station + " ASC",
			null);
	}

	/** @return cities that exist as departure point in submitted trips
		@param agent - filter by agent */
	public Cursor fetch_agent_from_cities(String agent)
	{
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		return mDbHelper.mDb.rawQuery("SELECT DISTINCT " + TABLE_TRIPS +
				"." + KEY_ROWID + " AS " + KEY_ROWID + ", " + key_city + " AS " + KEY_FROM_CITY +
				" FROM " + TABLE_TRIPS + " JOIN " + TABLE_STATIONS +
				" ON " + TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				TABLE_STATIONS + "." + KEY_ROWID +
				" JOIN " + TABLE_CITIES +
				" ON " + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID +
				" WHERE " + KEY_AGENT + " == ? " +
				" GROUP BY " + key_city +
				" ORDER BY " + key_city + " ASC",
			new String[] {agent});
	}

	/** @return cities that exist as departure point in submitted trips
		@param operator - filter by operator */
	public Cursor fetch_operator_from_cities(String operator)
	{
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		return mDbHelper.mDb.rawQuery("SELECT DISTINCT " + TABLE_TRIPS +
				"." + KEY_ROWID + " AS " + KEY_ROWID + ", " + key_city + " AS " + KEY_FROM_CITY +
				" FROM " + TABLE_TRIPS + " JOIN " + TABLE_STATIONS +
				" ON " + TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				TABLE_STATIONS + "." + KEY_ROWID +
				" JOIN " + TABLE_CITIES +
				" ON " + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID +
				" WHERE " + KEY_OPERATOR + " == ? " +
				" GROUP BY " + key_city +
				" ORDER BY " + key_city + " ASC",
			new String[] {operator});
	}

	/** @return cities that exist as arrival point in submitted trips for
		a given departure city
		@param from_city - departure city */
	public Cursor fetch_to_cities(String from_city)
	{
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		String from_city_id = fetch_city_id(from_city);

		return mDbHelper.mDb.rawQuery("SELECT DISTINCT " + TABLE_TRIPS +
				"." + KEY_ROWID + " AS " + KEY_ROWID + ", " + key_city + " AS " + KEY_TO_CITY +
				" FROM " + TABLE_TRIPS +

				" JOIN " + TABLE_STATIONS + " AS FROM_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				"FROM_STATIONS." + KEY_ROWID +

				" JOIN " + TABLE_STATIONS + " AS TO_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				"TO_STATIONS." + KEY_ROWID +

				" JOIN " + TABLE_CITIES +
				" ON TO_STATIONS." + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID +

				" WHERE FROM_STATIONS." + KEY_CITY_ID + " == ? " +
				" GROUP BY " + key_city +
				" ORDER BY " + key_city + " ASC",
			new String[] {from_city_id});
	}

	/** @return stations that exist as arrival point in submitted trips
		for a given departure station
		@param from_station - departure station */
	public Cursor fetch_to_stations(String from_station)
	{
		String key_station = KEY_STN + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		String from_station_id = fetch_station_id(from_station);

		return mDbHelper.mDb.rawQuery("SELECT DISTINCT " + TABLE_TRIPS +
				"." + KEY_ROWID + " AS " + KEY_ROWID + ", " + key_station + " AS " + KEY_TO_STN +
				", " + KEY_LATITUDE + ", " + KEY_LONGITUDE +
				" FROM " + TABLE_TRIPS + " JOIN " + TABLE_STATIONS +
				" ON " + TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				TABLE_STATIONS + "." + KEY_ROWID +
				" JOIN " + TABLE_CITIES +
				" ON " + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID +
				" WHERE " + KEY_FROM_STN_ID + " == ? " +
				" GROUP BY " + key_station +
				" ORDER BY " + key_station + " ASC",
			new String[] {from_station_id});
	}

	/** @return stations that exist as arrival point in submitted trips
		for a given departure city
		@param from_city - departure city */
	public Cursor fetch_to_stations_from_city(String from_city)
	{
		String key_station = KEY_STN + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		String from_city_id = fetch_city_id(from_city);

		return mDbHelper.mDb.rawQuery("SELECT DISTINCT " + TABLE_TRIPS +
				"." + KEY_ROWID + " AS " + KEY_ROWID +
				", TO_STATIONS." + key_station + " AS " + KEY_STN +
				", " + key_city + " AS " + KEY_CITY +
				", TO_STATIONS." + KEY_LATITUDE +
				", TO_STATIONS." + KEY_LONGITUDE +
				" FROM " + TABLE_TRIPS +

				" JOIN " + TABLE_STATIONS + " AS FROM_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				"FROM_STATIONS." + KEY_ROWID +

				" JOIN " + TABLE_STATIONS + " AS TO_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				"TO_STATIONS." + KEY_ROWID +

				" JOIN " + TABLE_CITIES +
				" ON TO_STATIONS." + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID +

				" WHERE FROM_STATIONS." + KEY_CITY_ID + " == ? " +
				" GROUP BY TO_STATIONS." + key_station +
				" ORDER BY TO_STATIONS." + key_station + " ASC",
			new String[] {from_city_id});
	}

	/** @return cities that exist as arrival point in submitted trips
		for a given departure city and agent
		@param from_city - departure city
		@param agent - filter by agent */
	public Cursor fetch_agent_to_cities(String from_city, String agent)
	{
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		String from_city_id = fetch_city_id(from_city);

		return mDbHelper.mDb.rawQuery("SELECT DISTINCT " + TABLE_TRIPS +
				"." + KEY_ROWID + " AS " + KEY_ROWID + ", " + key_city + " AS " + KEY_TO_CITY +
				" FROM " + TABLE_TRIPS +

				" JOIN " + TABLE_STATIONS + " AS FROM_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				"FROM_STATIONS." + KEY_ROWID +

				" JOIN " + TABLE_STATIONS + " AS TO_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				"TO_STATIONS." + KEY_ROWID +

				" JOIN " + TABLE_CITIES +
				" ON TO_STATIONS." + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID +

				" WHERE FROM_STATIONS." + KEY_CITY_ID + " == ? " + " AND " +
				KEY_AGENT + " == ? " +
				" GROUP BY " + key_city +
				" ORDER BY " + key_city + " ASC",
			new String[] {from_city_id, agent});
	}

	/** @return cities that exist as arrival point in submitted trips
		for a given departure city and operator
		@param from_city - departure city
		@param operator - filter by operator */
	public Cursor fetch_operator_to_cities(String from_city, String operator)
	{
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		String from_city_id = fetch_city_id(from_city);

		return mDbHelper.mDb.rawQuery("SELECT DISTINCT " + TABLE_TRIPS +
				"." + KEY_ROWID + " AS " + KEY_ROWID + ", " + key_city + " AS " + KEY_TO_CITY +
				" FROM " + TABLE_TRIPS +

				" JOIN " + TABLE_STATIONS + " AS FROM_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				"FROM_STATIONS." + KEY_ROWID +

				" JOIN " + TABLE_STATIONS + " AS TO_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				"TO_STATIONS." + KEY_ROWID +

				" JOIN " + TABLE_CITIES +
				" ON TO_STATIONS." + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID +

				" WHERE " + KEY_CITY_ID + " == ? " + " AND " +
				KEY_OPERATOR + " == ? " +
				" GROUP BY " + key_city +
				" ORDER BY " + key_city + " ASC",
			new String[] {from_city_id, operator});
	}

	public Cursor fetch_stations()
	{
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		String key_station = KEY_STN + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		return mDbHelper.mDb.rawQuery("SELECT " + TABLE_STATIONS +
				"." + KEY_ROWID + " AS " + KEY_ROWID + ", " +
				key_station + " AS " + KEY_STN + ", " +
				key_city + " AS " + KEY_CITY + ", " +
				KEY_LATITUDE + ", " + KEY_LONGITUDE +
				" FROM " + TABLE_STATIONS + " JOIN " + TABLE_CITIES +
				" ON " + TABLE_STATIONS + "." + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID,
			null);
	}

	public Cursor fetch_cities()
	{
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		return mDbHelper.mDb.query(true, TABLE_CITIES, new String[] {KEY_ROWID, key_city + " AS " + KEY_CITY},
				null, null, null, null, key_city + " ASC", null);
	}

	public Cursor fetch_agents()
	{
		return mDbHelper.mDb.query(true, TABLE_TRIPS, new String[] {KEY_ROWID, KEY_AGENT},
				"length(" + KEY_AGENT + ") != 0", null,
				KEY_AGENT, null, KEY_AGENT + " ASC", null);
	}

	public Cursor fetch_operators()
	{
		return mDbHelper.mDb.query(true, TABLE_TRIPS, new String[] {KEY_ROWID, KEY_OPERATOR},
				"length(" + KEY_OPERATOR + ") != 0", null,
				KEY_OPERATOR, null, KEY_OPERATOR + " ASC", null);
	}

	public Cursor fetch_counter_nums()
	{
		return mDbHelper.mDb.query(true, TABLE_TRIPS, new String[] {KEY_ROWID, KEY_CTR},
				"length(" + KEY_CTR + ") != 0", null,
				KEY_CTR, null, KEY_CTR + " ASC", null);
	}

	public Cursor fetch_agents(String agent_query)
	{
		return mDbHelper.mDb.query(true, TABLE_TRIPS, new String[] {KEY_ROWID, KEY_AGENT},
				"length(" + KEY_AGENT + ") != 0 AND " + KEY_AGENT + " LIKE ?", new String[] {agent_query},
				KEY_AGENT, null, KEY_AGENT + " ASC", null);
	}

	public Cursor fetch_operators(String operator_query)
	{
		return mDbHelper.mDb.query(true, TABLE_TRIPS, new String[] {KEY_ROWID, KEY_OPERATOR},
				"length(" + KEY_OPERATOR + ") != 0 AND " + KEY_OPERATOR + " LIKE ?", new String[] {operator_query},
				KEY_OPERATOR, null, KEY_OPERATOR + " ASC", null);
	}

	public Cursor fetch_avg(String from_city, String to_city, String group_by, String sort_by)
	{
		String from_city_id = fetch_city_id(from_city);
		String to_city_id = fetch_city_id(to_city);
		return mDbHelper.mDb.query(true,
				TABLE_TRIPS + " JOIN " +
				TABLE_STATIONS + " AS FROM_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				"FROM_STATIONS." + KEY_ROWID + " JOIN " +
				TABLE_STATIONS + " AS TO_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				"TO_STATIONS." + KEY_ROWID,
				new String[] {KEY_AGENT, KEY_OPERATOR, AVG_TIME, AVG_DELAY, NUM_TRIPS},
				"FROM_STATIONS." + KEY_CITY_ID + " = ? AND " +
				"TO_STATIONS." + KEY_CITY_ID + " = ? AND " + KEY_ARRIVAL + "!= 'Cancelled'",
				new String[] {from_city_id, to_city_id},
				group_by, null, sort_by + " ASC", null);
	}

	public Cursor fetch_agent_avg(String from_city, String to_city, String agent)
	{
		String from_city_id = fetch_city_id(from_city);
		String to_city_id = fetch_city_id(to_city);
		return mDbHelper.mDb.query(true,
				TABLE_TRIPS + " JOIN " +
				TABLE_STATIONS + " AS FROM_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				"FROM_STATIONS." + KEY_ROWID + " JOIN " +
				TABLE_STATIONS + " AS TO_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				"TO_STATIONS." + KEY_ROWID,
				new String[] {AVG_TIME, AVG_DELAY, NUM_TRIPS},
				KEY_AGENT + " = ? AND FROM_STATIONS." +
				KEY_CITY_ID + " = ? AND TO_STATIONS." +
				KEY_CITY_ID + " = ?",
				new String[] {agent, from_city_id, to_city_id},
				null, null, null, null);
	}

	public Cursor fetch_operator_avg(String from_city, String to_city, String operator)
	{
		String from_city_id = fetch_city_id(from_city);
		String to_city_id = fetch_city_id(to_city);
		return mDbHelper.mDb.query(true,
				TABLE_TRIPS + " JOIN " +
				TABLE_STATIONS + " AS FROM_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				"FROM_STATIONS." + KEY_ROWID + " JOIN " +
				TABLE_STATIONS + " AS TO_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				"TO_STATIONS." + KEY_ROWID,
				new String[] {AVG_TIME, AVG_DELAY, NUM_TRIPS},
				KEY_OPERATOR + " = ? AND FROM_STATIONS." +
				KEY_CITY_ID + " = ? AND TO_STATIONS." +
				KEY_CITY_ID + " = ?",
				new String[] {operator, from_city_id, to_city_id},
				null, null, null, null);
	}

	public Cursor fetch_avg_agent_delay(String agent)
	{
		return mDbHelper.mDb.query(true, TABLE_TRIPS, new String[] {AVG_DELAY},
				KEY_AGENT + " = ?", new String[] {agent}, null, null, null, null);
	}

	public Cursor fetch_avg_operator_delay(String operator)
	{
		return mDbHelper.mDb.query(true, TABLE_TRIPS, new String[] {AVG_DELAY},
				KEY_OPERATOR + " = ?", new String[] {operator}, null, null, null, null);
	}

	public Cursor fetch_agent_trips(String from_city, String to_city, String agent)
	{
		String from_city_id = fetch_city_id(from_city);
		String to_city_id = fetch_city_id(to_city);
		return mDbHelper.mDb.query(TABLE_TRIPS + " JOIN " +
				TABLE_STATIONS + " AS FROM_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				"FROM_STATIONS." + KEY_ROWID + " JOIN " +
				TABLE_STATIONS + " AS TO_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				"TO_STATIONS." + KEY_ROWID,
				new String[] {KEY_SCHED_DEP, TRIP_DELAY, TRIP_TIME},
				KEY_AGENT + " = ? AND FROM_STATIONS." +
				KEY_CITY_ID + " = ? AND TO_STATIONS." +
				KEY_CITY_ID + " = ?",
				new String[] {agent, from_city_id, to_city_id},
				null, null, "strftime('%s', " + KEY_SCHED_DEP + ") DESC", null);
	}

	public Cursor fetch_operator_trips(String from_city, String to_city, String operator)
	{
		String from_city_id = fetch_city_id(from_city);
		String to_city_id = fetch_city_id(to_city);
		return mDbHelper.mDb.query(TABLE_TRIPS + " JOIN " +
				TABLE_STATIONS + " AS FROM_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				"FROM_STATIONS." + KEY_ROWID + " JOIN " +
				TABLE_STATIONS + " AS TO_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				"TO_STATIONS." + KEY_ROWID,
				new String[] {KEY_SCHED_DEP, TRIP_DELAY, TRIP_TIME},
				KEY_OPERATOR + " = ? AND FROM_STATIONS." +
				KEY_CITY_ID + " = ? AND TO_STATIONS." +
				KEY_CITY_ID + " = ?",
				new String[] {operator, from_city_id, to_city_id},
				null, null, "strftime('%s', " + KEY_SCHED_DEP + ") DESC", null);
	}

	public String get_most_freq_from_city()
	{
		String ret = null;
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);

		Cursor c = mDbHelper.mDb.rawQuery("SELECT " + key_city +
				" FROM " + TABLE_TRIPS + " JOIN " + TABLE_STATIONS +
				" ON " + TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				TABLE_STATIONS + "." + KEY_ROWID +
				" JOIN " + TABLE_CITIES +
				" ON " + TABLE_STATIONS + "." + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID +
				" GROUP BY " + key_city +
				" ORDER BY COUNT(" + KEY_CITY_ID + ") DESC LIMIT 1",
			null);
		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		return ret;
	}

	public String get_most_freq_to_city(String from_city)
	{
		String ret = null;
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		String from_city_id = fetch_city_id(from_city);

		Cursor c = mDbHelper.mDb.rawQuery("SELECT " + key_city +
				" FROM " + TABLE_TRIPS +
				" JOIN " + TABLE_STATIONS + " AS FROM_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_FROM_STN_ID + " == " +
				"FROM_STATIONS." + KEY_ROWID +
				" JOIN " + TABLE_STATIONS + " AS TO_STATIONS ON " +
				TABLE_TRIPS + "." + KEY_TO_STN_ID + " == " +
				"TO_STATIONS." + KEY_ROWID +
				" JOIN " + TABLE_CITIES +
				" ON TO_STATIONS." + KEY_CITY_ID + " == " +
				TABLE_CITIES + "." + KEY_ROWID +
				" WHERE FROM_STATIONS." + KEY_CITY_ID + " == ? " +
				" GROUP BY " + key_city +
				" ORDER BY COUNT(TO_STATIONS." + KEY_CITY_ID + ") DESC LIMIT 1",
			new String[] {from_city_id});
		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		return ret;
	}

	public Cursor fetch_tmp_sched_time()
	{
		return mDbHelper.mDb.query(TABLE_TMP,
				new String[] {KEY_ROWID,
					"strftime(\"%Y\", " + KEY_SCHED_DEP + ")",
					"strftime(\"%m\", " + KEY_SCHED_DEP + ")",
					"strftime(\"%d\", " + KEY_SCHED_DEP + ")",
					"strftime(\"%H\", " + KEY_SCHED_DEP + ")",
					"strftime(\"%M\", " + KEY_SCHED_DEP + ")",
				},
				KEY_SCHED_DEP + " IS NOT NULL", null, null, null, null, "1");
	}

	public Cursor fetch_tmp_depart_time()
	{
		return mDbHelper.mDb.query(TABLE_TMP,
				new String[] {KEY_ROWID,
					"strftime(\"%Y\", " + KEY_ACTUAL_DEP + ")",
					"strftime(\"%m\", " + KEY_ACTUAL_DEP + ")",
					"strftime(\"%d\", " + KEY_ACTUAL_DEP + ")",
					"strftime(\"%H\", " + KEY_ACTUAL_DEP + ")",
					"strftime(\"%M\", " + KEY_ACTUAL_DEP + ")",
				},
				KEY_ACTUAL_DEP + " IS NOT NULL", null, null, null, null, "1");
	}

	public Cursor fetch_tmp_arrival_time()
	{
		return mDbHelper.mDb.query(TABLE_TMP,
				new String[] {KEY_ROWID,
					"strftime(\"%Y\", " + KEY_ARRIVAL + ")",
					"strftime(\"%m\", " + KEY_ARRIVAL + ")",
					"strftime(\"%d\", " + KEY_ARRIVAL + ")",
					"strftime(\"%H\", " + KEY_ARRIVAL + ")",
					"strftime(\"%M\", " + KEY_ARRIVAL + ")",
				},
				KEY_ARRIVAL + " IS NOT NULL", null, null, null, null, "1");
	}

	public String fetch_tmp(String key)
	{
		Cursor c = mDbHelper.mDb.query(TABLE_TMP,
				new String[] {KEY_ROWID, key},
				key + " IS NOT NULL", null, null, null, null, "1");
		String ret = "";
		if (c.moveToFirst()) {
			ret = c.getString(c.getColumnIndex(key));
		}
		c.close();
		return ret;
	}

	public Cursor fetch_tmp_complaint_sched_time()
	{
		return mDbHelper.mDb.query(TABLE_TMP_COMPLAINT,
				new String[] {KEY_ROWID,
					"strftime(\"%Y\", " + KEY_SCHED_DEP + ")",
					"strftime(\"%m\", " + KEY_SCHED_DEP + ")",
					"strftime(\"%d\", " + KEY_SCHED_DEP + ")",
					"strftime(\"%H\", " + KEY_SCHED_DEP + ")",
					"strftime(\"%M\", " + KEY_SCHED_DEP + ")",
				},
				KEY_SCHED_DEP + " IS NOT NULL", null, null, null, null, "1");
	}

	public String fetch_tmp_complaint(String key)
	{
		Cursor c = mDbHelper.mDb.query(TABLE_TMP_COMPLAINT,
				new String[] {KEY_ROWID, key},
				key + " IS NOT NULL", null, null, null, null, "1");
		String ret = "";
		if (c.moveToFirst()) {
			ret = c.getString(c.getColumnIndex(key));
		}
		c.close();
		return ret;
	}

	public int fetch_safety()
	{
		Cursor c = mDbHelper.mDb.query(TABLE_TMP,
				new String[] {KEY_ROWID, KEY_SAFETY},
				KEY_SAFETY + " IS NOT NULL", null, null, null, null, "1");
		int ret = 3;
		if (c.moveToFirst()) {
			ret = c.getInt(c.getColumnIndex(KEY_SAFETY));
		}
		c.close();
		return ret;
	}

	public int fetch_comfort()
	{
		Cursor c = mDbHelper.mDb.query(TABLE_TMP,
				new String[] {KEY_ROWID, KEY_COMFORT},
				KEY_COMFORT + " IS NOT NULL", null, null, null, null, "1");
		int ret = 3;
		if (c.moveToFirst()) {
			ret = c.getInt(c.getColumnIndex(KEY_COMFORT));
		}
		c.close();
		return ret;
	}

	public int fetch_overall()
	{
		Cursor c = mDbHelper.mDb.query(TABLE_TMP,
				new String[] {KEY_ROWID, KEY_OVERALL},
				KEY_OVERALL + " IS NOT NULL", null, null, null, null, "1");
		int ret = 3;
		if (c.moveToFirst()) {
			ret = c.getInt(c.getColumnIndex(KEY_OVERALL));
		}
		c.close();
		return ret;
	}

	public long fetch_max_id(String table)
	{
		Cursor c = mDbHelper.mDb.query(table,
				new String[] {KEY_ROWID},
				null, null, null, null, KEY_ROWID + " DESC", "1");
		long ret = 0;
		if (c.moveToFirst()) {
			ret = c.getInt(c.getColumnIndex(KEY_ROWID));
		}
		c.close();
		return ret;
	}

	public void save_tmp(String agent, String operator,
			String from_city, String from_station, String to_city,
			String to_station, String scheduled_departure,
			String actual_departure, String arrival_time,
			String counter, int safety, int comfort, int overall,
			String comment)
	{
		ContentValues cv = new ContentValues();
		cv.put(KEY_AGENT, agent);
		cv.put(KEY_OPERATOR, operator);
		cv.put(KEY_FROM_CITY, from_city);
		cv.put(KEY_FROM_STN, from_station);
		cv.put(KEY_TO_CITY, to_city);
		cv.put(KEY_TO_STN, to_station);
		cv.put(KEY_SCHED_DEP, scheduled_departure);
		cv.put(KEY_ACTUAL_DEP, actual_departure);
		cv.put(KEY_ARRIVAL, arrival_time);
		cv.put(KEY_CTR, counter);
		cv.put(KEY_SAFETY, safety);
		cv.put(KEY_COMFORT, comfort);
		cv.put(KEY_OVERALL, overall);
		cv.put(KEY_COMMENT, comment);

		Cursor c =  mDbHelper.mDb.query(TABLE_TMP, new String[] {KEY_ROWID},
				null, null, null, null, null, "1");
		if (c.moveToFirst()) {
			long row_id = c.getInt(0);
			mDbHelper.mDb.update(TABLE_TMP, cv,
					KEY_ROWID + " = ?", new String[] {Long.toString(row_id)});
		} else {
			mDbHelper.mDb.insert(TABLE_TMP, null, cv);
		}
		c.close();
	}

	public void save_tmp_complaint(String agent, String operator,
			String from_city, String from_station, String to_city,
			String to_station, String scheduled_departure,
			String counter, String comment, String reg)
	{
		ContentValues cv = new ContentValues();
		cv.put(KEY_AGENT, agent);
		cv.put(KEY_OPERATOR, operator);
		cv.put(KEY_FROM_CITY, from_city);
		cv.put(KEY_FROM_STN, from_station);
		cv.put(KEY_TO_CITY, to_city);
		cv.put(KEY_TO_STN, to_station);
		cv.put(KEY_SCHED_DEP, scheduled_departure);
		cv.put(KEY_CTR, counter);
		cv.put(KEY_COMMENT, comment);
		cv.put(KEY_REG, reg);

		Cursor c =  mDbHelper.mDb.query(TABLE_TMP_COMPLAINT, new String[] {KEY_ROWID},
				null, null, null, null, null, "1");
		if (c.moveToFirst()) {
			long row_id = c.getInt(0);
			mDbHelper.mDb.update(TABLE_TMP_COMPLAINT, cv,
					KEY_ROWID + " = ?", new String[] {Long.toString(row_id)});
		} else {
			mDbHelper.mDb.insert(TABLE_TMP_COMPLAINT, null, cv);
		}
		c.close();
	}

	public void clear_tmp_table()
	{
		mDbHelper.mDb.delete(TABLE_TMP, null, null);
	}

	public void clear_tmp_complaint_table()
	{
		mDbHelper.mDb.delete(TABLE_TMP_COMPLAINT, null, null);
	}

	private int sec_since_last_update()
	{
		Cursor c = mDbHelper.mDb.query(TABLE_LAST_UPDATE,
				new String[] {
					"strftime(\"%s\", 'now') - strftime(\"%s\", " + KEY_LAST_UPDATE + ")"
				},
				null, null, null, null, null, "1");
		int ret = c.moveToFirst() ? c.getInt(0) : Integer.MAX_VALUE;
		c.close();

		return ret;
	}

	private void set_last_update()
	{
		Cursor c = mDbHelper.mDb.rawQuery("SELECT strftime(\"%Y-%m-%d %H:%M:%S\", 'now')", null);
		if (!c.moveToFirst()) {
			c.close();
			return;
		}
		String last_update = c.getString(0);
		c.close();

		ContentValues cv = new ContentValues();
		cv.put(KEY_LAST_UPDATE, last_update);

		mDbHelper.mDb.delete(TABLE_LAST_UPDATE, null, null);
		mDbHelper.mDb.insert(TABLE_LAST_UPDATE, null, cv);
	}

	private String fetch_city_id(String city)
	{
		String key_city = KEY_CITY + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		Cursor c = mDbHelper.mDb.query(TABLE_CITIES, new String[] {KEY_ROWID},
				key_city + " = ?", new String[] {city},
				null, null, null, "1");
		String ret = "0";
		if (c.moveToFirst()) {
			ret = Integer.toString(c.getInt(c.getColumnIndex(KEY_ROWID)));
		}
		c.close();
		return ret;
	}

	private String fetch_station_id(String station)
	{
		String key_station = KEY_STN + "_" + mDbHelper.mCtx.getResources().getString(R.string.lang_code);
		Cursor c = mDbHelper.mDb.query(TABLE_STATIONS, new String[] {KEY_ROWID},
				key_station + " = ?", new String[] {station},
				null, null, null, "1");
		String ret = "0";
		if (c.moveToFirst()) {
			ret = Integer.toString(c.getInt(c.getColumnIndex(KEY_ROWID)));
		}
		c.close();
		return ret;
	}
}

