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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

public class SyncTask extends AsyncTask<Void, Integer, Void>
{
	/* TODO: move to Constants.java */
	private final String BASE_URL = "https://raw.github.com/sweetiepiggy/Raspberry-Bus-Malaysia/trips/";
	private final String TRIPS_URL = BASE_URL + "trips.csv";
	private final String CITIES_URL = BASE_URL + "cities.csv";
	private final String STATIONS_URL = BASE_URL + "stations.csv";

	private Context mCtx;
	private int mUpdatesFnd = 0;
	private String mAlertMsg = null;
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

		try {
			DbAdapter dbHelper = new DbAdapter();
			dbHelper.open(mCtx);
			String lastUpdate = dbHelper.getLastUpdate();
			dbHelper.close();

			LinkedList<ContentValues> cities = parse_csv(CITIES_URL,
					lastUpdate, 0, 8);
			mUpdatesFnd += sync_table(cities, DbAdapter.TABLE_CITIES, 8, 33);

			LinkedList<ContentValues> stations = parse_csv(STATIONS_URL,
					lastUpdate, 33, 41);
			mUpdatesFnd += sync_table(stations, DbAdapter.TABLE_STATIONS, 41, 66);

			LinkedList<ContentValues> trips = parse_csv(TRIPS_URL,
					lastUpdate, 66, 74);
			mUpdatesFnd += sync_table(trips, DbAdapter.TABLE_TRIPS, 74, 99);

			//mUpdatesFnd = cities.size() + stations.size() + trips.size();

			publishProgress(100);

			dbHelper.open(mCtx);
			dbHelper.setLastUpdate();
			dbHelper.close();

		/* probably no internet connection */
		} catch (UnknownHostException e) {
			mAlertMsg = mCtx.getResources().getString(R.string.unknown_host);
		} catch (MalformedURLException e) {
			throw new Error(e);
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}

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
		if (mAlertMsg != null) {
			alert(mAlertMsg);
		} else {
			Toast.makeText(mCtx,
					Integer.toString(mUpdatesFnd) + " " +
					mCtx.getResources().getString(R.string.updates_found),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
		if (mProgressDialog != null) {
			mProgressDialog.setProgress(values[0]);
		}
	}

	private LinkedList<ContentValues> parse_csv(String url_name, String lastUpdate,
			int progress_offset, int progress_max) throws
		UnknownHostException, MalformedURLException, UnsupportedEncodingException,
		IOException
	{
		LinkedList<ContentValues> ret = new LinkedList<ContentValues>();

		URL url = new URL(url_name);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(url.openStream(), "utf-8"));

		String line = in.readLine();
		String[] field_names = null;

		if (line != null) {
			field_names = line.split(",");
		}

		long max_id = 0;
		int added = 0;
		boolean done = false;

		while ((line = in.readLine()) != null && !done) {
			ContentValues cv = parse_line(line, field_names);

			if (cv.containsKey(DbAdapter.KEY_UPDATE_DATE) &&
					lastUpdate.compareTo(cv.getAsString(DbAdapter.KEY_UPDATE_DATE)) >= 0) {
				done = true;
			} else {
				ret.addFirst(cv);
				++added;
			}

			if (cv.containsKey(DbAdapter.KEY_ROWID)) {
				max_id = java.lang.Math.max(max_id, cv.getAsLong(DbAdapter.KEY_ROWID));
			}
			if (max_id != 0) {
				publishProgress(java.lang.Math.min(progress_max,
							progress_offset +
							(int)((progress_max - progress_offset) *
								(double) added / max_id)));
			}
		}

		in.close();

		return ret;
	}


	private int sync_table(LinkedList<ContentValues> values, String table,
			int progress_offset, int progress_max)
	{
		int new_added = 0;

		DbAdapter dbHelper = new DbAdapter();
		dbHelper.open_readwrite(mCtx, false);

		long add_total = values.size();
		final long orig_max_id = dbHelper.fetch_max_id(table);

		int added = 0;

		Iterator<ContentValues> itr = values.listIterator();
		while (itr.hasNext()) {
			ContentValues cv = itr.next();
			if (dbHelper.replace(cv, table) > orig_max_id) {
				++new_added;
			}
			++added;
			if (add_total != 0) {
				publishProgress(java.lang.Math.min(progress_max,
							progress_offset +
							(int)((progress_max - progress_offset) *
								(double) added / add_total)));
			}
		}
		dbHelper.close();

		return new_added;
	}

	private ContentValues parse_line(String line, String[] field_names)
	{
		ContentValues ret = new ContentValues();

		String[] fields = splitNotIn(line, '"', ',');
		for (int i = 0; i < fields.length; ++i) {
			fields[i] = fields[i].replaceAll("^\"", "");
			fields[i] = fields[i].replaceAll("\"$", "");
			ret.put(field_names[i], fields[i]);
		}

		return ret;
	}

	private String[] splitNotIn(String str, char notInChar, char delim)
	{
		LinkedList<String> tkns = new LinkedList<String>();

		int len = str.length();
		boolean isIn = false;
		boolean needJoin = false;
		int beg = 0;

		for (int i=0; i < len; ++i) {
			if (str.charAt(i) == notInChar) {
				if (isIn) {
					if (needJoin) {
						String tmp = tkns.removeLast();
						tkns.add(tmp + notInChar +
								str.substring(beg+1, i) +
								notInChar);
						needJoin = false;
					} else {
						tkns.add(str.substring(beg+1, i));
					}
					beg = i + 1;
				} else if (i - beg != 0) {
					tkns.add(str.substring(beg, i));
					beg = i;
					needJoin = true;
				}
				isIn = !isIn;
			/* not in and is a delim */
			} else if (!isIn && str.charAt(i) == delim) {
				if (i - beg != 0) {
					tkns.add(str.substring(beg, i));
				}
				beg = i + 1;
			}
		}

		if (beg != 0 && beg != len) {
			tkns.add(str.substring(beg));
		} else if (beg == 0) {
			tkns.add(str);
		}

		return tkns.toArray(new String[tkns.size()]);
	}

	private void alert(String msg)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(mCtx);
		alert.setTitle(mCtx.getResources().getString(android.R.string.dialog_alert_title));
		alert.setMessage(msg);
		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		alert.show();
	}
}

