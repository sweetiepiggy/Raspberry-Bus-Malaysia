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
import android.util.Log;


public class DbAdapter {
	public static final String KEY_FROM_CITY = "from_city";
	public static final String KEY_TO_CITY = "to_city";
	public static final String KEY_ROWID = "_id";

	private static final String TAG = "DbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "bus";
	private static final String DATABASE_TABLE = "trips";
	private static final int DATABASE_VERSION = 2;

	private static final String DATABASE_CREATE =
		"create table " + DATABASE_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, " +
	KEY_FROM_CITY + " text not null, " +
	KEY_TO_CITY + " text not null);";


	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			//Toast.makeText(mCtx, "created", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}

	public DbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public int clear() {
		return mDb.delete(DATABASE_TABLE, null, null);
	}

	/** @return row_id or -1 if failed */
	public long create_trip(String from_city, String to_city) {
		ContentValues initial_values = new ContentValues();
		initial_values.put(KEY_FROM_CITY, from_city);
		initial_values.put(KEY_TO_CITY, to_city);

		return mDb.insert(DATABASE_TABLE, null, initial_values);
	}

	public Cursor fetch_from_cities() {
		return mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FROM_CITY},
				null, null, KEY_FROM_CITY, null, KEY_FROM_CITY + " ASC", null);
	}

	public Cursor fetch_to_cities(String from_city) {
		return mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TO_CITY},
				KEY_FROM_CITY + " = ?", new String[] {from_city},
				KEY_TO_CITY, null, KEY_TO_CITY + " ASC", null);
	}

}

