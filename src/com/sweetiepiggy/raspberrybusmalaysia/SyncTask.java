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

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class SyncTask extends AsyncTask<Void, Integer, Void>
{
	private final String TAG = "SyncTask";

	/* TODO: move to Constants.java */
	private final String BASE_URL = "https://raw.github.com/sweetiepiggy/Raspberry-Bus-Malaysia/trips/";
	private final String TRIPS_URL = BASE_URL + "trips.csv";
	private final String CITIES_URL = BASE_URL + "cities.csv";
	private final String STATIONS_URL = BASE_URL + "stations.csv";

	private Context mCtx;
	private int mNewTripCnt = 0;
	private ProgressDialog mProgressDialog;

	public SyncTask(Context ctx)
	{
		mCtx = ctx;
		mProgressDialog = new ProgressDialog(mCtx);
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		sync_cities();
		publishProgress(33);
		sync_stations();
		publishProgress(66);
		mNewTripCnt = sync_trips();
		publishProgress(100);

		return null;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		if (mProgressDialog != null) {
			mProgressDialog.setMessage(mCtx.getResources().getString(R.string.syncing));
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setProgress(0);
			mProgressDialog.show();
		}
	}

	@Override
	protected void onPostExecute(Void result)
	{
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		Toast.makeText(mCtx,
				Integer.toString(mNewTripCnt) + " " +
				mCtx.getResources().getString(R.string.updates_found),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
		if (mProgressDialog != null) {
			mProgressDialog.setProgress(values[0]);
		}
	}

	private int sync_cities()
	{
		int added_cities = 0;

		try {
			URL url = new URL(CITIES_URL);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream(), "utf-8"));

			String line = in.readLine();

			if (line != null) {
				String[] field_names = line.split(",");

				DbAdapter dbHelper = new DbAdapter();
				dbHelper.open_no_sync(mCtx);

				long max_id = dbHelper.fetch_cities_max_id();

				while ((line = in.readLine()) != null) {
					ContentValues city = parse_line(line, field_names);
					dbHelper.create_city(city);
					++added_cities;
					if (city.containsKey(DbAdapter.KEY_ROWID)) {
						max_id = java.lang.Math.max(max_id, city.getAsLong(DbAdapter.KEY_ROWID));
					}
					if (max_id != 0) {
						publishProgress(java.lang.Math.min(33, (int)(33. * added_cities / max_id)));
					}
				}
				dbHelper.close();
			}

			in.close();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			throw new Error(e);
		}

		return added_cities;
	}

	private int sync_stations()
	{
		int added_stations = 0;

		try {
			URL url = new URL(STATIONS_URL);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream(), "utf-8"));

			String line = in.readLine();

			if (line != null) {
				String[] field_names = line.split(",");

				DbAdapter dbHelper = new DbAdapter();
				dbHelper.open_no_sync(mCtx);

				long max_id = dbHelper.fetch_stations_max_id();

				while ((line = in.readLine()) != null) {
					ContentValues station = parse_line(line, field_names);
					dbHelper.create_station(station);
					++added_stations;
					if (station.containsKey(DbAdapter.KEY_ROWID)) {
						max_id = java.lang.Math.max(max_id, station.getAsLong(DbAdapter.KEY_ROWID));
					}
					if (max_id != 0) {
						publishProgress(java.lang.Math.min(66, 33 + (int)(33. * added_stations / max_id)));
					}
				}
				dbHelper.close();
			}

			in.close();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			throw new Error(e);
		}

		return added_stations;
	}

	private int sync_trips()
	{
		int added_trips = 0;

		try {
			URL url = new URL(TRIPS_URL);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream(), "utf-8"));

			String line = in.readLine();

			if (line != null) {
				String[] field_names = line.split(",");

				DbAdapter dbHelper = new DbAdapter();
				dbHelper.open_no_sync(mCtx);

				long max_id = dbHelper.fetch_trips_max_id();
				final long orig_max_id = max_id;

				int read_trips = 0;

				while ((line = in.readLine()) != null) {
					ContentValues trip = parse_line(line, field_names);
					if (dbHelper.create_trip(trip) > orig_max_id) {
						++added_trips;
					}
					++read_trips;
					if (trip.containsKey(DbAdapter.KEY_ROWID)) {
						max_id = java.lang.Math.max(max_id, trip.getAsLong(DbAdapter.KEY_ROWID));
					}
					if (max_id != 0) {
						publishProgress(java.lang.Math.min(100, 66 + (int)(33. * read_trips / max_id)));
					}
				}
				dbHelper.close();
			}

			in.close();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			throw new Error(e);
		}

		return added_trips;
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

