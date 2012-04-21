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
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RouteResultActivity extends Activity
{
	private DbAdapter mDbHelper;
	private String m_from_city;
	private String m_to_city;
	private ArrayList<TableRow> m_rows;

	/* TODO: put this in Constants.java */
	private static final String UNKNOWN = "<Unknown>";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_result);
		Bundle b = getIntent().getExtras();
		m_from_city = (b == null) ? "<NULL>" : b.getString("from_city");
		m_to_city = (b == null) ? "<NULL>" : b.getString("to_city");
		((TextView) findViewById(R.id.title)).setText(m_from_city + " to " + m_to_city);

		m_rows = new ArrayList<TableRow>();

		init_labels();

		mDbHelper = new DbAdapter();
		mDbHelper.open(this);

		sort_by_avg_time();

	}

	private void init_labels()
	{
		TextView company = (TextView) findViewById(R.id.company);
		company.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				sort_by_company();
			}
		});

		TextView avg_time = (TextView) findViewById(R.id.avg_time);
		avg_time.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				sort_by_avg_time();
			}
		});

		TextView avg_delay = (TextView) findViewById(R.id.avg_delay);
		avg_delay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				sort_by_delay();
			}
		});

		TextView num_trips = (TextView) findViewById(R.id.num_trips);
		num_trips.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				sort_by_num_trips();
			}
		});
	}

	private void sort_by_company()
	{
		Cursor c = mDbHelper.fetch_avg_by_company_sort_company(m_from_city, m_to_city);
		startManagingCursor(c);
		print_rows(c);
	}

	private void sort_by_avg_time()
	{
		Cursor c = mDbHelper.fetch_avg_by_company(m_from_city, m_to_city);
		startManagingCursor(c);
		print_rows(c);

	}

	private void sort_by_delay()
	{
		Cursor c = mDbHelper.fetch_avg_by_company_sort_delay(m_from_city, m_to_city);
		startManagingCursor(c);
		print_rows(c);
	}

	private void sort_by_num_trips()
	{
		Cursor c = mDbHelper.fetch_avg_by_company_sort_trips(m_from_city, m_to_city);
		startManagingCursor(c);
		print_rows(c);
	}

	private void print_rows(Cursor c)
	{
		clear_rows();
		if (c.moveToFirst()) do {
			String company = c.getString(0);
			String avg = format_time(c.getInt(1));
			String delay = format_time_min(c.getInt(2));
			String count = c.getString(3);
			print_row(company, avg, delay, count);
		} while (c.moveToNext());
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
		m_rows.add(tr);
		tr.addView(company_view);
		tr.addView(avg_view);
		tr.addView(delay_view);
		tr.addView(count_view);

		TableLayout results_layout = (TableLayout) findViewById(R.id.results_layout);
		results_layout.addView(tr);
	}

	private void clear_rows()
	{
		ViewGroup results_layout = (ViewGroup) findViewById(R.id.results_layout);
		Iterator itr = m_rows.iterator();
		while (itr.hasNext()) {
			results_layout.removeView((TableRow)itr.next());
		}
		m_rows.clear();
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

