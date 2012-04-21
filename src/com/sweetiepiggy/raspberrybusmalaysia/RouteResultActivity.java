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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RouteResultActivity extends Activity
{
	/* TODO: put this in Constants.java */
	private static final String UNKNOWN = "<Unknown>";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_result);
		Bundle b = getIntent().getExtras();
		String from_city = (b == null) ? "<NULL>" : b.getString("from_city");
		String to_city = (b == null) ? "<NULL>" : b.getString("to_city");
		((TextView) findViewById(R.id.title)).setText(from_city + " to " + to_city);

		DbAdapter dbHelper = new DbAdapter();
		dbHelper.open(this);

		Cursor c = dbHelper.fetch_avg_by_company(from_city, to_city);
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			String company = c.getString(0);
			String avg = format_time(c.getInt(1));
			String delay = format_time_min(c.getInt(2));
			String count = c.getString(3);
			print_row(company, avg, delay, count);
		} while (c.moveToNext());

//		dbHelper.close();
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

	private void print_row(String company, String avg, String delay, String count)
	{
		if (company.length() > 20) {
			company = company.substring(0, 20);
		} else if (company.length() == 0) {
			company = UNKNOWN;
//			return;
		}

		TextView company_view = new TextView(getApplicationContext());
		TextView avg_view = new TextView(getApplicationContext());
		TextView delay_view = new TextView(getApplicationContext());
		TextView count_view = new TextView(getApplicationContext());
		company_view.setText(company);
		avg_view.setText(avg);
		delay_view.setText(delay);
		count_view.setText(count);

		company_view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(),
					CompanyResultActivity.class);
				Bundle b = new Bundle();
				String company = ((TextView)v).getText().toString();
				if (company.equals(UNKNOWN)) {
					company = "";
				}
				b.putString("company", company);
				intent.putExtras(b);
				startActivity(intent);
			}
		});

		TableRow tr = new TableRow(getApplicationContext());
		tr.addView(company_view);
		tr.addView(avg_view);
		tr.addView(delay_view);
		tr.addView(count_view);

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
		return String.format("%s%dmin", negative, min);
	}
}
