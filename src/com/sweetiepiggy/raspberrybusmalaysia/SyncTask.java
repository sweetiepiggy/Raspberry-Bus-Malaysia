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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class SyncTask extends AsyncTask<Void, Void, Void>
{
	private final String TAG = "SyncTask";

	/* TODO: move to Constants.java */
	private final String TRIPS_URL = "https://raw.github.com/sweetiepiggy/Raspberry-Bus-Malaysia/trips/rbm.csv";

	private Context mCtx;
	private int new_trip_cnt = 0;

	public SyncTask(Context ctx)
	{
		mCtx = ctx;
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		try {
			URL url = new URL(TRIPS_URL);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream()));

			String line = in.readLine();

			if (line != null) {
				String[] field_names = line.split(",");

				DbAdapter dbHelper = new DbAdapter();
				dbHelper.open_no_sync(mCtx);
				while ((line = in.readLine()) != null) {
					ContentValues trip = parse_line(line, field_names);
					if (dbHelper.create_trip(trip) != -1) {
						++new_trip_cnt;
					}
				}
				dbHelper.close();
			}

			in.close();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			throw new Error(e);
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result)
	{
		Toast.makeText(mCtx,
				Integer.toString(new_trip_cnt) + " " +
				mCtx.getResources().getString(R.string.updates_found),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onProgressUpdate(Void... values)
	{
	}

	private ContentValues parse_line(String line, String[] field_names)
	{
//		Log.i(TAG, "line:[" + line + "]");

		ContentValues ret = new ContentValues();

		String[] fields = line.split(",", field_names.length);
		for (int i = 0; i < fields.length; ++i) {
			fields[i] = fields[i].replaceAll("^\"", "");
			fields[i] = fields[i].replaceAll("\"$", "");
//			Log.i(TAG, field_names[i] + ":[" + fields[i] + "]");
			ret.put(field_names[i], fields[i]);
		}

		return ret;
	}
}

