/*
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
	/* TODO: move to Constants.java */
	private final String BASE_URL = "https://raw.github.com/sweetiepiggy/Raspberry-Bus-Malaysia/trips/";
	private final String TRIPS_URL = BASE_URL + "trips.csv";
	private final String CITIES_URL = BASE_URL + "cities.csv";
	private final String STATIONS_URL = BASE_URL + "stations.csv";

	private final String LAST_UPDATE_STR = "LAST_MODIFIED";

	private Context mCtx;
	private int mUpdatesFnd = 0;
	private ProgressDialog mProgressDialog;

	public SyncTask(Context ctx)
	{
		mCtx = ctx;
		mProgressDialog = new ProgressDialog(mCtx);
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		mUpdatesFnd = 0;

		mUpdatesFnd += sync_table(CITIES_URL, DbAdapter.TABLE_CITIES, 0, 33);
		publishProgress(33);

		mUpdatesFnd += sync_table(STATIONS_URL, DbAdapter.TABLE_STATIONS, 33, 66);
		publishProgress(66);

		mUpdatesFnd += sync_table(TRIPS_URL, DbAdapter.TABLE_TRIPS, 66, 99);
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
				Integer.toString(mUpdatesFnd) + " " +
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

	private int sync_table(String url_name, String table,
			int progress_offset, int progress_max)
	{
		int added = 0;

		try {
			URL url = new URL(url_name);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream(), "utf-8"));

			String line = in.readLine();
			String[] field_names = null;

			if (line != null) {
				field_names = line.split(",");
			}

			/* skip first line if it is used to store last update time */
			if (field_names.length > 0 &&
					field_names[0].equals(LAST_UPDATE_STR)) {
				line = in.readLine();
				if (line != null) {
					field_names = line.split(",");
				}
			}

			DbAdapter dbHelper = new DbAdapter();
			dbHelper.open_readwrite(mCtx, false);

			long max_id = dbHelper.fetch_max_id(table);
			final long orig_max_id = max_id;

			int read = 0;

			while ((line = in.readLine()) != null) {
				ContentValues cv = parse_line(line, field_names);
				if (dbHelper.replace(cv, table) > orig_max_id) {
					++added;
				}
				++read;
				if (cv.containsKey(DbAdapter.KEY_ROWID)) {
					max_id = java.lang.Math.max(max_id, cv.getAsLong(DbAdapter.KEY_ROWID));
				}
				if (max_id != 0) {
					publishProgress(java.lang.Math.min(progress_max,
								progress_offset +
								(int)((progress_max - progress_offset) *
									(double) read / max_id)));
				}
			}
			dbHelper.close();

			in.close();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			throw new Error(e);
		}

		return added;
	}

	private ContentValues parse_line(String line, String[] field_names)
	{
		ContentValues ret = new ContentValues();

		String[] fields = line.split(",", field_names.length);
		for (int i = 0; i < fields.length; ++i) {
			fields[i] = fields[i].replaceAll("^\"", "");
			fields[i] = fields[i].replaceAll("\"$", "");
			ret.put(field_names[i], fields[i]);
		}

		return ret;
	}
}

