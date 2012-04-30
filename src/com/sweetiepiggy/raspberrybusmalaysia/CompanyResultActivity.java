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

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CompanyResultActivity extends Activity
{
	/* TODO: put this in Constants.java? */
	private static final String UNKNOWN = "<Unknown Company>";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.company_result);

		Bundle b = getIntent().getExtras();
		String company = (b == null) ? "<NULL>" : b.getString("company");
		String company_display = company.length() == 0 ? UNKNOWN : company;
		((TextView) findViewById(R.id.title)).setText(company_display);

		DbAdapter dbHelper = new DbAdapter();
		dbHelper.open(this);

		Cursor c_comp = dbHelper.fetch_avg_delay(company);
		startManagingCursor(c_comp);
		if (c_comp.moveToFirst()) do {
			String avg_delay = format_time_min(c_comp.getInt(c_comp.getColumnIndex(DbAdapter.AVG_DELAY)));
			((TextView) findViewById(R.id.total_avg_delay)).setText(avg_delay);
		} while (c_comp.moveToNext());

		Cursor c_from = dbHelper.fetch_from_cities(company);
		startManagingCursor(c_from);
		if (c_from.moveToFirst()) do {
			String from_city = c_from.getString(c_from.getColumnIndex(DbAdapter.KEY_FROM_CITY));

			Cursor c_to = dbHelper.fetch_to_cities(from_city, company);
			startManagingCursor(c_to);
			if (c_to.moveToFirst()) do {
				String to_city = c_to.getString(c_to.getColumnIndex(DbAdapter.KEY_TO_CITY));

				Cursor c_avg = dbHelper.fetch_avg(from_city, to_city, company);
				startManagingCursor(c_avg);
				String avg = "<NULL>";
				if (c_avg.moveToFirst()) {
					avg = format_time(c_avg.getInt(c_avg.getColumnIndex(DbAdapter.AVG_TIME)));
				}

				Cursor c_avg_delay = dbHelper.fetch_avg_delay(from_city, to_city, company);
				startManagingCursor(c_avg_delay);

				String avg_delay = "<NULL>";
				if (c_avg_delay.moveToFirst()) {
					avg_delay = format_time_min(c_avg_delay.getInt(c_avg_delay.getColumnIndex(DbAdapter.AVG_DELAY)));
				}

				print_row(from_city, to_city, avg, avg_delay);
			} while (c_to.moveToNext());
		} while (c_from.moveToNext());

//		dbHelper.close();
	}

	private void print_row(String from_city, String to_city, String avg,
			String avg_delay)
	{
		TextView from_view = new TextView(getApplicationContext());
		TextView to_view = new TextView(getApplicationContext());
		TextView avg_view = new TextView(getApplicationContext());
		TextView avg_delay_view = new TextView(getApplicationContext());

		from_view.setGravity(Gravity.CENTER);
		to_view.setGravity(Gravity.CENTER);
		avg_view.setGravity(Gravity.CENTER);
		avg_delay_view.setGravity(Gravity.CENTER);

		from_view.setText(from_city);
		to_view.setText(to_city);
		avg_view.setText(avg);
		avg_delay_view.setText(avg_delay);

		TableRow tr = new TableRow(getApplicationContext());
		tr.addView(from_view);
		tr.addView(to_view);
		tr.addView(avg_view);
		tr.addView(avg_delay_view);

		TableLayout results_layout = (TableLayout) findViewById(R.id.results_layout);
		results_layout.addView(tr);
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
		return String.format("%s%dhr %02dmin", negative, hr, min);
	}

	private String format_time_min(int time)
	{
		String negative = "";
		if (time < 0) {
			negative = "-";
			time *= -1;
		}

		int min = time / 60;
		return String.format("%s%dmin", negative, min);
	}
}

