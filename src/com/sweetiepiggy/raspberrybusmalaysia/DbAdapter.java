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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


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

	public static final String TRIP_TIME = "(strftime('%s', " + KEY_ARRIVAL + ") - strftime('%s', " + KEY_SCHED_DEP + "))";
	public static final String TRIP_DELAY = "(strftime('%s', " + KEY_ACTUAL_DEP + ") - strftime('%s', " + KEY_SCHED_DEP + "))";
	public static final String AVG_TIME = "avg(" + TRIP_TIME + ")";
	public static final String AVG_DELAY = "avg(" + TRIP_DELAY + ")";
	public static final String NUM_TRIPS = "count(" + KEY_COMP + ")";

//	private static final String TAG = "DbAdapter";
	private DatabaseHelper mDbHelper;

	private static final String DATABASE_PATH = "/data/data/com.sweetiepiggy.raspberrybusmalaysia/databases/";
	private static final String DATABASE_NAME = "rbm.db";
	private static final String DATABASE_TABLE = "trips";
	private static final int DATABASE_VERSION = 5;

//	private static final String DATABASE_CREATE =
//		"create table " + DATABASE_TABLE + " (" +
//		KEY_ROWID + " integer primary key autoincrement, " +
//		KEY_COMP + " text, " +
//		KEY_BRAND + " text, " +
//		KEY_FROM_CITY + " text not null, " +
//		KEY_FROM_STN + " text, " +
//		KEY_TO_CITY + " text not null, " +
//		KEY_TO_STN + " text, " +
//		KEY_SCHED_DEP + " text not null, " +
//		KEY_ACTUAL_DEP + " text not null, " +
//		KEY_ARRIVAL + " text not null, " +
//		KEY_CTR + " text, " +
//		KEY_CTR_NAME + " text);";

	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		private final Context mCtx;
		public SQLiteDatabase mDb;

		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mCtx = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
//			db.execSQL(DATABASE_CREATE);
		}

		public void create_database() throws IOException
		{
			if (!database_exists()) {
				this.getReadableDatabase();
				try {
					copy_database();
				} catch (IOException e) {
					throw new Error(e);
				}
			}
		}

		private boolean database_exists()
		{
			SQLiteDatabase db = null;
			try {
				String out_filename = DATABASE_PATH + DATABASE_NAME;
				db = SQLiteDatabase.openDatabase(out_filename, null, SQLiteDatabase.OPEN_READONLY);
			} catch (SQLiteException e) {
				/* database does not exist yet */
			}
			if (db != null) {
				db.close();
			}
			return db != null;
		}

		private void copy_database() throws IOException
		{
			InputStream input = mCtx.getAssets().open(DATABASE_NAME);

			String full_path = DATABASE_PATH + DATABASE_NAME;

			OutputStream output = new FileOutputStream(full_path);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer))>0){
				output.write(buffer, 0, length);
			}

			output.flush();
			output.close();
			input.close();
		}

		public void open_database(int perm) throws SQLException
		{
			String full_path = DATABASE_PATH + DATABASE_NAME;
			mDb = SQLiteDatabase.openDatabase(full_path, null, perm);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
//			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
//					+ newVersion + ", which will destroy all old data");
//			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
//			onCreate(db);
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
		return open(ctx, SQLiteDatabase.OPEN_READONLY);
	}

	public DbAdapter open_readwrite(Context ctx) throws SQLException
	{
		return open(ctx, SQLiteDatabase.OPEN_READWRITE);
	}

	private DbAdapter open(Context ctx, int perm) throws SQLException
	{
		mDbHelper = new DatabaseHelper(ctx);

		try {
			mDbHelper.create_database();
		} catch (IOException e) {
			throw new Error(e);
		}

		mDbHelper.open_database(perm);

		return this;
	}

	public void close()
	{
		mDbHelper.close();
	}

	/** @return row_id or -1 if failed */
	public long create_trip(String company, String bus_brand,
			String from_city, String from_station, String to_city,
			String to_station, String scheduled_departure,
			String actual_departure, String arrival_time,
			String counter, String safety, String comfort, String comment)
	{
		ContentValues initial_values = new ContentValues();
		initial_values.put(KEY_COMP, company);
		initial_values.put(KEY_BRAND, bus_brand);
		initial_values.put(KEY_FROM_CITY, from_city);
		initial_values.put(KEY_FROM_STN, from_station);
		initial_values.put(KEY_TO_CITY, to_city);
		initial_values.put(KEY_TO_STN, to_station);
		initial_values.put(KEY_SCHED_DEP, scheduled_departure);
		initial_values.put(KEY_ACTUAL_DEP, actual_departure);
		initial_values.put(KEY_ARRIVAL, arrival_time);
		initial_values.put(KEY_CTR, counter);
		initial_values.put(KEY_SAFETY, safety);
		initial_values.put(KEY_COMFORT, comfort);
		initial_values.put(KEY_COMMENT, comment);

		return mDbHelper.mDb.insert(DATABASE_TABLE, null, initial_values);
	}

	public Cursor fetch_cities()
	{
		return mDbHelper.mDb.rawQuery("SELECT DISTINCT city FROM " +
				"(SELECT " + KEY_FROM_CITY + " as city from " + DATABASE_TABLE + " UNION " +
				"SELECT " + KEY_TO_CITY + " as city from " + DATABASE_TABLE + ")" +
				" ORDER BY city ASC;",
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
				" ORDER BY station ASC;",
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

	public Cursor fetch_avg_by_company(String from_city, String to_city)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE,
				new String[] {KEY_COMP, AVG_TIME, AVG_DELAY, NUM_TRIPS},
				KEY_FROM_CITY + " = ? AND " + KEY_TO_CITY + " = ? AND " + KEY_ARRIVAL + "!= 'Cancelled'",
				new String[] {from_city, to_city},
				KEY_COMP, null, AVG_TIME + " ASC", null);
	}

	public Cursor fetch_avg_by_company_sort_delay(String from_city, String to_city)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE,
				new String[] {KEY_COMP, AVG_TIME, AVG_DELAY, NUM_TRIPS},
				KEY_FROM_CITY + " = ? AND " + KEY_TO_CITY + " = ? AND " + KEY_ARRIVAL + "!= 'Cancelled'",
				new String[] {from_city, to_city},
				KEY_COMP, null, AVG_DELAY + " ASC", null);
	}

	public Cursor fetch_avg_by_company_sort_company(String from_city, String to_city)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE,
				new String[] {KEY_COMP, AVG_TIME, AVG_DELAY, NUM_TRIPS},
				KEY_FROM_CITY + " = ? AND " + KEY_TO_CITY + " = ? AND " + KEY_ARRIVAL + "!= 'Cancelled'",
				new String[] {from_city, to_city},
				KEY_COMP, null, KEY_COMP + " ASC", null);
	}

	public Cursor fetch_avg_by_company_sort_trips(String from_city, String to_city)
	{
		return mDbHelper.mDb.query(true, DATABASE_TABLE,
				new String[] {KEY_COMP, AVG_TIME, AVG_DELAY, NUM_TRIPS},
				KEY_FROM_CITY + " = ? AND " + KEY_TO_CITY + " = ? AND " + KEY_ARRIVAL + "!= 'Cancelled'",
				new String[] {from_city, to_city},
				KEY_COMP, null, NUM_TRIPS + " DESC", null);
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
}

