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

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TripsActivity extends Activity {
	private DbAdapter mDbHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trips);

		Bundle b = getIntent().getExtras();
		String from_city = (b == null) ? "<NULL>" : b.getString("from_city");
		String to_city = (b == null) ? "<NULL>" : b.getString("to_city");
		String company = (b == null) ? "<NULL>" : b.getString("company");
		String company_display = company.length() == 0 ? getResources().getString(R.string.unknown) : company;

		((TextView) findViewById(R.id.company)).setText(company_display);
		((TextView) findViewById(R.id.route)).setText(from_city + " -> " + to_city);

		mDbHelper = new DbAdapter();
		mDbHelper.open(this);

		print_averages(from_city, to_city, company);
		print_rows(from_city, to_city, company);
	}

	@Override
	protected void onDestroy() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
		super.onDestroy();
	}

	private void print_averages(String from_city, String to_city,
			String company)
	{
		Cursor c = mDbHelper.fetch_avg(from_city, to_city, company);
		startManagingCursor(c);

		if (c.moveToFirst()) do {
			String avg_delay = format_time_min(c.getInt(c.getColumnIndex(DbAdapter.AVG_DELAY)));
			((TextView) findViewById(R.id.total_avg_delay)).setText(avg_delay);
		} while (c.moveToNext());
	}

	private void print_rows(String from_city, String to_city,
			String company)
	{
		Cursor c = mDbHelper.fetch_trips(from_city, to_city,
				company);
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			String sched_dep = c.getString(c.getColumnIndex(DbAdapter.KEY_SCHED_DEP));
			String trip_delay = format_time_min(c.getInt(c.getColumnIndex(DbAdapter.TRIP_DELAY)));
			String trip_time = format_time(c.getInt(c.getColumnIndex(DbAdapter.TRIP_TIME)));
			print_row(sched_dep, trip_delay, trip_time);
		} while (c.moveToNext());
	}

	private void print_row(String sched_dep, String trip_delay,
			String trip_time)
	{
		TextView sched_dep_view = new TextView(getApplicationContext());
		TextView trip_delay_view = new TextView(getApplicationContext());
		TextView trip_time_view = new TextView(getApplicationContext());

		sched_dep_view.setText(sched_dep);
		trip_delay_view.setText(trip_delay);
		trip_time_view.setText(trip_time);

		sched_dep_view.setGravity(Gravity.CENTER);
		trip_delay_view.setGravity(Gravity.CENTER);
		trip_time_view.setGravity(Gravity.CENTER);

		ArrayList<TableRow> rows = new ArrayList<TableRow>();

		TableRow tr = new TableRow(getApplicationContext());
		rows.add(tr);
		tr.addView(sched_dep_view);
		tr.addView(trip_time_view);
		tr.addView(trip_delay_view);
		TableLayout results_layout = (TableLayout) findViewById(R.id.results_layout);
		results_layout.addView(tr);
	}

	/* TODO: move format_time() and format_time_min() to their own class */
	private String format_time_min(int time)
	{
		String negative = "";
		if (time < 0) {
			negative = "-";
			time *= -1;
		}

		int min = time / 60;
		return String.format("%s%d%s", negative, min, getResources().getString(R.string.minute_abbr));
	}

	private String format_time(int time)
	{
		String negative = "";
		if (time < 0) {
			negative = "-";
			time *= -1;
		}

		int hr = time / 3600;
		time -= hr * 3600;
		int min = time / 60;
		return String.format("%s%d%s %02d%s", negative, hr, getResources().getString(R.string.hour_abbr),
				min, getResources().getString(R.string.minute_abbr));
	}
}

