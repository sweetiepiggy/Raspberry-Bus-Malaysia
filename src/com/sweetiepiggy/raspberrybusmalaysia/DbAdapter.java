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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DbAdapter
{
	public static final String KEY_ROWID = "_id";
	public static final String KEY_COMP = "company";
	public static final String KEY_BRAND = "bus_brand";
	public static final String KEY_FROM_CITY = "from_city";
	public static final String KEY_FROM_STN = "from_station";
	public static final String KEY_TO_CITY = "to_city";
	public static final String KEY_TO_STN = "to_station";
	public static final String KEY_SCHED_DEP = "scheduled_departure";
	public static final String KEY_ACTUAL_DEP = "actual_departure";
	public static final String KEY_ARRIVAL = "arrival_time";
	public static final String KEY_CTR = "counter";
	public static final String KEY_SAFETY = "safety";
	public static final String KEY_COMFORT = "comfort";
	public static final String KEY_COMMENT = "comment";
	public static final String KEY_LAST_UPDATE = "comment";

	public static final String TRIP_TIME = "(strftime('%s', " + KEY_ARRIVAL + ") - strftime('%s', " + KEY_SCHED_DEP + "))";
	public static final String TRIP_DELAY = "(strftime('%s', " + KEY_ACTUAL_DEP + ") - strftime('%s', " + KEY_SCHED_DEP + "))";
	public static final String AVG_TIME = "avg(" + TRIP_TIME + ")";
	public static final String AVG_DELAY = "avg(" + TRIP_DELAY + ")";
	public static final String NUM_TRIPS = "count(" + KEY_COMP + ")";

	private static final int SEC_BETWEEN_UPDATES = 60 * 60 * 24 * 7;

//	private static final String TAG = "DbAdapter";
	private DatabaseHelper mDbHelper;

	private static final String DATABASE_NAME = "rbm.db";
	private static final String DATABASE_TABLE = "trips";
	/** "tmp" table was originally named "submit" in db-v7 / rbm-v0.1.1 */
	private static final String DATABASE_TMP_TABLE_OLD = "submit";
	private static final String DATABASE_TMP_TABLE = "tmp";
	private static final String DATABASE_LAST_UPDATE_TABLE = "last_update";
	private static final int DATABASE_VERSION = 8;

	private static final String DATABASE_CREATE =
		"CREATE TABLE " + DATABASE_TABLE + " (" +
		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		KEY_COMP + " TEXT, " +
		KEY_BRAND + " TEXT, " +
		KEY_FROM_CITY + " TEXT NOT NULL, " +
		KEY_FROM_STN + " TEXT, " +
		KEY_TO_CITY + " TEXT NOT NULL, " +
		KEY_TO_STN + " TEXT, " +
		KEY_SCHED_DEP + " TEXT NOT NULL, " +
		KEY_ACTUAL_DEP + " TEXT NOT NULL, " +
		KEY_ARRIVAL + " TEXT NOT NULL, " +
		KEY_CTR + " TEXT, " +
		KEY_SAFETY + " INTEGER, " +
		KEY_COMFORT + " INTEGER, " +
		KEY_COMMENT + " TEXT);";

	private static final String DATABASE_CREATE_TMP =
		"CREATE TABLE " + DATABASE_TMP_TABLE + " (" +
		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		KEY_COMP + " TEXT, " +
		KEY_BRAND + " TEXT, " +
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
		KEY_COMMENT + " TEXT);";

	private static final String DATABASE_CREATE_LAST_UPDATE =
		"CREATE TABLE " + DATABASE_LAST_UPDATE_TABLE + " (" +
		KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
		KEY_LAST_UPDATE + " TEXT);";

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
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TMP_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_LAST_UPDATE_TABLE);
			db.execSQL(DATABASE_CREATE);
			db.execSQL(DATABASE_CREATE_TMP);
			db.execSQL(DATABASE_CREATE_LAST_UPDATE);
			if (mAllowSync) {
				SyncTask sync = new SyncTask(mCtx);
				sync.execute();
				Toast.makeText(mCtx, R.string.syncing, Toast.LENGTH_SHORT).show();

				Cursor c = db.rawQuery("SELECT strftime(\"%Y-%m-%d %H:%M:%S\", 'now')", null);
				if (!c.moveToFirst()) {
					return;
				}
				String last_update = c.getString(0);
				c.close();

				ContentValues cv = new ContentValues();
				cv.put(KEY_LAST_UPDATE, last_update);

				db.delete(DATABASE_LAST_UPDATE_TABLE, null, null);
				db.insert(DATABASE_LAST_UPDATE_TABLE, null, cv);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int old_ver, int new_ver)
		{
			//Log.i(TAG, "upgrading database from " + old_ver +
					//" to " + new_ver);
			switch (old_ver) {
			case 5:
			case 6:
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TMP_TABLE_OLD);
				db.execSQL(DATABASE_CREATE_TMP);
			case 7:
				db.execSQL(DATABASE_CREATE_LAST_UPDATE);
				break;
			default:
				onCreate(db);
				break;
			}
		}

		public void open_database(int perm) throws SQLException
		{
			mDb = (perm == SQLiteDatabase.OPEN_READWRITE) ?
				getWritableDatabase() : getReadableDatabase();
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

	public DbAdapter open(Context ctx) throws SQLException
	{
		return open(ctx, SQLiteDatabase.OPEN_READONLY, true);
	}

	public DbAdapter open_no_sync(Context ctx) throws SQLException
	{
		return open(ctx, SQLiteDatabase.OPEN_READONLY, false);
	}

	public DbAdapter open_readwrite(Context ctx) throws SQLException
	{
		return open(ctx, SQLiteDatabase.OPEN_READWRITE, true);
	}

	private DbAdapter open(Context ctx, int perm, boolean allow_sync) throws SQLException
	{
		//Log.i(TAG, "new DatabaseHelper(ctx)");
		mDbHelper = new DatabaseHelper(ctx, allow_sync);
		//Log.i(TAG, "opening database with permission " + perm);
		mDbHelper.open_database(perm);

		return this;
	}

	public void check_last_update_and_sync()
	{
		if (sec_since_last_update() > SEC_BETWEEN_UPDATES) {
			SyncTask sync = new SyncTask(mDbHelper.mCtx);
			sync.execute();
			Toast.makeText(mDbHelper.mCtx, R.string.syncing, Toast.LENGTH_SHORT).show();
			set_last_update();
		}
	}


	public void close()
	{
		mDbHelper.close();
	}

	/** @return row_id or -1 if failed */
	public long create_trip(ContentValues trip)
	{
		if (!trip_exists(trip)) {
			return mDbHelper.mDb.insert(DATABASE_TABLE, null, trip);
		} else {
			return -1;
		}
	}

	private boolean trip_exists(ContentValues trip)
	{
		String company = trip.getAsString(KEY_COMP);
		String bus_brand = trip.getAsString(KEY_BRAND);
		String from_city = trip.getAsString(KEY_FROM_CITY);
		String from_station = trip.getAsString(KEY_FROM_STN);
		String to_city = trip.getAsString(KEY_TO_CITY);
		String to_station = trip.getAsString(KEY_TO_STN);
		String scheduled_departure = trip.getAsString(KEY_SCHED_DEP);
		String actual_departure = trip.getAsString(KEY_ACTUAL_DEP);
		String arrival_time = trip.getAsString(KEY_ARRIVAL);
		String counter = trip.getAsString(KEY_CTR);
		String safety = trip.getAsString(KEY_SAFETY);
		String comfort = trip.getAsString(KEY_COMFORT);
		String comment = trip.getAsString(KEY_COMMENT);

		Cursor c =  mDbHelper.mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID},
				KEY_COMP + " = ? AND " +
				KEY_BRAND + " = ? AND " +
				KEY_FROM_CITY + " = ? AND " +
				KEY_FROM_STN + " = ? AND " +
				KEY_TO_CITY + " = ? AND " +
				KEY_TO_STN + " = ? AND " +
				KEY_SCHED_DEP + " = ? AND " +
				KEY_ACTUAL_DEP + " = ? AND " +
				KEY_ARRIVAL + " = ? AND " +
				KEY_CTR + " = ? AND " +
				KEY_SAFETY + " = ? AND " +
				KEY_COMFORT + " = ? AND " +
				KEY_COMMENT + " = ?",
				new String[] {company, bus_brand, from_city,
					from_station, to_city, to_station,
					scheduled_departure, actual_departure,
					arrival_time, counter, safety, comfort,
					comment},
				null, null, null, "1");
		boolean ret = c.moveToFirst();
		c.close();
		return ret;
	}

	public Cursor fetch_cities()
	{
		return mDbHelper.mDb.rawQuery("SELECT DISTINCT city FROM " +
				"(SELECT " + KEY_FROM_CITY + " as city from " + DATABASE_TABLE + " UNION " +
				"SELECT " + KEY_TO_CITY + " as city from " + DATABASE_TABLE + ")" +
				" ORDER BY city ASC",
				null);
	}

	public Cursor fetch_from_cities()
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FROM_CITY},
				null, null, KEY_FROM_CITY, null, KEY_FROM_CITY + " ASC", null);
	}

	public Cursor fetch_from_cities(String company)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FROM_CITY},
				KEY_COMP + " = ?", new String[] {company},
				KEY_FROM_CITY, null, KEY_FROM_CITY + " ASC", null);
	}

	public Cursor fetch_to_cities(String from_city)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TO_CITY},
				KEY_FROM_CITY + " = ?", new String[] {from_city},
				KEY_TO_CITY, null, KEY_TO_CITY + " ASC", null);
	}

	public Cursor fetch_to_cities(String from_city, String company)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TO_CITY},
				KEY_FROM_CITY + " = ? AND " + KEY_COMP + " = ?",
				new String[] {from_city, company},
				KEY_TO_CITY, null, KEY_TO_CITY + " ASC", null);
	}

	public Cursor fetch_stations()
	{
		return mDbHelper.mDb.rawQuery("SELECT DISTINCT station FROM " +
				"(SELECT " + KEY_FROM_STN + " as station from " + DATABASE_TABLE + " UNION " +
				"SELECT " + KEY_TO_STN + " as station from " + DATABASE_TABLE + ")" +
				" ORDER BY station ASC",
				null);
	}

	public Cursor fetch_companies()
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_COMP},
				"length(" + KEY_COMP + ") != 0", null,
				KEY_COMP, null, KEY_COMP + " ASC", null);
	}

	public Cursor fetch_brands()
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_BRAND},
				"length(" + KEY_BRAND + ") != 0", null,
				KEY_BRAND, null, KEY_BRAND + " ASC", null);
	}

	public Cursor fetch_counter_nums()
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_CTR},
				"length(" + KEY_CTR + ") != 0", null,
				KEY_CTR, null, KEY_CTR + " ASC", null);
	}

	public Cursor fetch_avg(String from_city, String to_city, String group_by, String sort_by)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE,
				new String[] {KEY_COMP, KEY_BRAND, AVG_TIME, AVG_DELAY, NUM_TRIPS},
				KEY_FROM_CITY + " = ? AND " + KEY_TO_CITY + " = ? AND " + KEY_ARRIVAL + "!= 'Cancelled'",
				new String[] {from_city, to_city},
				group_by, null, sort_by + " ASC", null);
	}

	public Cursor fetch_companies(String company_query)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_COMP},
				"length(" + KEY_COMP + ") != 0 AND " + KEY_COMP + " LIKE ?", new String[] {company_query},
				KEY_COMP, null, KEY_COMP + " ASC", null);
	}

	public Cursor fetch_avg(String from_city, String to_city, String company)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {AVG_TIME, AVG_DELAY, NUM_TRIPS},
				KEY_COMP + " = ? AND " + KEY_FROM_CITY + " = ? AND " + KEY_TO_CITY + " = ?",
				new String[] {company, from_city, to_city},
				null, null, null, null);
	}

	public Cursor fetch_avg_brand(String from_city, String to_city, String brand)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {AVG_TIME, AVG_DELAY, NUM_TRIPS},
				KEY_BRAND + " = ? AND " + KEY_FROM_CITY + " = ? AND " + KEY_TO_CITY + " = ?",
				new String[] {brand, from_city, to_city},
				null, null, null, null);
	}

	public Cursor fetch_avg_delay(String company)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE, new String[] {AVG_DELAY},
				KEY_COMP + " = ?", new String[] {company}, null, null, null, null);
	}

	public Cursor fetch_trips(String from_city, String to_city, String company)
	{
		return mDbHelper.mDb.query(DATABASE_TABLE, new String[] {KEY_SCHED_DEP, TRIP_DELAY, TRIP_TIME},
				KEY_COMP + " = ? AND " + KEY_FROM_CITY + " = ? AND " + KEY_TO_CITY + " = ?",
				new String[] {company, from_city, to_city},
				null, null, "strftime('%s', " + KEY_SCHED_DEP + ") DESC", null);
	}

	public Cursor fetch_trips_brand(String from_city, String to_city, String brand)
	{
		return mDbHelper.mDb.query(DATABASE_TABLE, new String[] {KEY_SCHED_DEP, TRIP_DELAY, TRIP_TIME},
				KEY_BRAND + " = ? AND " + KEY_FROM_CITY + " = ? AND " + KEY_TO_CITY + " = ?",
				new String[] {brand, from_city, to_city},
				null, null, "strftime('%s', " + KEY_SCHED_DEP + ") DESC", null);
	}

	public String get_most_freq_from_city()
	{
		String ret = null;
		Cursor c =  mDbHelper.mDb.query(DATABASE_TABLE, new String[] {KEY_FROM_CITY},
				null, null,
				KEY_FROM_CITY, null, "count(" + KEY_FROM_CITY + ") DESC", "1");
		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		return ret;
	}

	public String get_most_freq_to_city(String from_city)
	{
		String ret = null;
		Cursor c =  mDbHelper.mDb.query(DATABASE_TABLE, new String[] {KEY_TO_CITY},
				KEY_FROM_CITY + " = ?", new String[] {from_city},
				KEY_TO_CITY, null, "count(" + KEY_TO_CITY + ") DESC", "1");
		if (c.moveToFirst()) {
			ret = c.getString(0);
		}
		c.close();
		return ret;
	}

	public Cursor fetch_tmp_sched_time()
	{
		return mDbHelper.mDb.query(DATABASE_TMP_TABLE,
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
		return mDbHelper.mDb.query(DATABASE_TMP_TABLE,
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
		return mDbHelper.mDb.query(DATABASE_TMP_TABLE,
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
		Cursor c = mDbHelper.mDb.query(DATABASE_TMP_TABLE,
				new String[] {KEY_ROWID, key},
				key + " IS NOT NULL", null, null, null, null, "1");
		return c.moveToFirst() ? c.getString(1) : "";
	}

	public int fetch_safety()
	{
		Cursor c = mDbHelper.mDb.query(DATABASE_TMP_TABLE,
				new String[] {KEY_ROWID, KEY_SAFETY},
				KEY_SAFETY + " IS NOT NULL", null, null, null, null, "1");
		return c.moveToFirst() ? c.getInt(1) : 3;
	}

	public int fetch_comfort()
	{
		Cursor c = mDbHelper.mDb.query(DATABASE_TMP_TABLE,
				new String[] {KEY_ROWID, KEY_COMFORT},
				KEY_COMFORT + " IS NOT NULL", null, null, null, null, "1");
		return c.moveToFirst() ? c.getInt(1) : 3;
	}

	public void save_tmp(String company, String bus_brand,
			String from_city, String from_station, String to_city,
			String to_station, String scheduled_departure,
			String actual_departure, String arrival_time,
			String counter, int safety, int comfort, String comment)
	{
		ContentValues cv = new ContentValues();
		cv.put(KEY_COMP, company);
		cv.put(KEY_BRAND, bus_brand);
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
		cv.put(KEY_COMMENT, comment);

		Cursor c =  mDbHelper.mDb.query(DATABASE_TMP_TABLE, new String[] {KEY_ROWID},
				null, null, null, null, null, "1");
		if (c.moveToFirst()) {
			long row_id = c.getInt(0);
			mDbHelper.mDb.update(DATABASE_TMP_TABLE, cv,
					KEY_ROWID + " = ?", new String[] {Long.toString(row_id)});
		} else {
			mDbHelper.mDb.insert(DATABASE_TMP_TABLE, null, cv);
		}
		c.close();
	}

	public void clear_tmp_table()
	{
		mDbHelper.mDb.delete(DATABASE_TMP_TABLE, null, null);
	}

	private int sec_since_last_update()
	{
		Cursor c = mDbHelper.mDb.query(DATABASE_LAST_UPDATE_TABLE,
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
			return;
		}
		String last_update = c.getString(0);
		c.close();

		ContentValues cv = new ContentValues();
		cv.put(KEY_LAST_UPDATE, last_update);

		mDbHelper.mDb.delete(DATABASE_LAST_UPDATE_TABLE, null, null);
		mDbHelper.mDb.insert(DATABASE_LAST_UPDATE_TABLE, null, cv);
	}
}

