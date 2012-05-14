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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RouteResultActivity extends Activity
{
	private DbAdapter mDbHelper;
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
		String from_city = (b == null) ? "<NULL>" : b.getString("from_city");
		String to_city = (b == null) ? "<NULL>" : b.getString("to_city");
		((TextView) findViewById(R.id.title)).setText(from_city + " to " + to_city);

		m_rows = new ArrayList<TableRow>();

		init_labels(from_city, to_city);

		mDbHelper = new DbAdapter();
		mDbHelper.open(this);

		sort_by_avg_time(from_city, to_city);

	}

	private void init_labels(final String from_city, final String to_city)
	{
		TextView company = (TextView) findViewById(R.id.company);
		company.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				sort_by_company(from_city, to_city);
			}
		});

		TextView avg_time = (TextView) findViewById(R.id.avg_time);
		avg_time.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				sort_by_avg_time(from_city, to_city);
			}
		});

		TextView avg_delay = (TextView) findViewById(R.id.avg_delay);
		avg_delay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				sort_by_delay(from_city, to_city);
			}
		});

		TextView num_trips = (TextView) findViewById(R.id.num_trips);
		num_trips.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				sort_by_num_trips(from_city, to_city);
			}
		});
	}

	private void sort_by_company(String from_city, String to_city)
	{
		Cursor c = mDbHelper.fetch_avg_by_company_sort_company(from_city, to_city);
		startManagingCursor(c);
		print_rows(c, from_city, to_city);
	}

	private void sort_by_avg_time(String from_city, String to_city)
	{
		Cursor c = mDbHelper.fetch_avg_by_company(from_city, to_city);
		startManagingCursor(c);
		print_rows(c, from_city, to_city);

	}

	private void sort_by_delay(String from_city, String to_city)
	{
		Cursor c = mDbHelper.fetch_avg_by_company_sort_delay(from_city, to_city);
		startManagingCursor(c);
		print_rows(c, from_city, to_city);
	}

	private void sort_by_num_trips(String from_city, String to_city)
	{
		Cursor c = mDbHelper.fetch_avg_by_company_sort_trips(from_city, to_city);
		startManagingCursor(c);
		print_rows(c, from_city, to_city);
	}

	private void print_rows(Cursor c, String from_city, String to_city)
	{
		clear_rows();
		if (c.moveToFirst()) do {
			String company = c.getString(c.getColumnIndex(DbAdapter.KEY_COMP));
			String avg = format_time(c.getInt(c.getColumnIndex(DbAdapter.AVG_TIME)));
			String delay = format_time_min(c.getInt(c.getColumnIndex(DbAdapter.AVG_DELAY)));
			String count = c.getString(c.getColumnIndex(DbAdapter.NUM_TRIPS));
			print_row(company, avg, delay, count, from_city,
					to_city);
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

	private void print_row(String company, String avg, String delay,
			String count, String from_city, String to_city)
	{
		String display_company = company;
		if (company.length() > 15) {
//			company = company.substring(0, 15);
			display_company = company.replace(' ', '\n');
		} else if (company.length() == 0) {
			display_company = UNKNOWN;
//			return;
		}

		Button company_view = new Button(getApplicationContext());
		TextView avg_view = new TextView(getApplicationContext());
		TextView delay_view = new TextView(getApplicationContext());
		Button count_view = new Button(getApplicationContext());
		company_view.setText(display_company);
		avg_view.setText(avg);
		delay_view.setText(delay);
		count_view.setText(count);

		company_view.setGravity(Gravity.CENTER);
		avg_view.setGravity(Gravity.CENTER);
		delay_view.setGravity(Gravity.CENTER);
		count_view.setGravity(Gravity.CENTER);

		init_company_button(company_view, company);
		init_count_button(count_view, company, from_city, to_city);

		TableRow tr = new TableRow(getApplicationContext());
		m_rows.add(tr);
		tr.addView(company_view);
		tr.addView(avg_view);
		tr.addView(delay_view);
		tr.addView(count_view);

		TableLayout results_layout = (TableLayout) findViewById(R.id.results_layout);
		results_layout.addView(tr);
		tr.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
	}

	private void init_company_button(Button b, final String company)
	{
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(),
					CompanyResultActivity.class);
				Bundle b = new Bundle();
				b.putString("company", company);
				intent.putExtras(b);
				startActivity(intent);
			}
		});

	}

	private void init_count_button(Button b, final String company,
			final String from_city, final String to_city)
	{
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(),
					TripsActivity.class);
				Bundle b = new Bundle();
				b.putString("company", company);
				b.putString("from_city", from_city);
				b.putString("to_city", to_city);
				intent.putExtras(b);
				startActivity(intent);
			}
		});
	}

	private void clear_rows()
	{
		ViewGroup results_layout = (ViewGroup) findViewById(R.id.results_layout);
		Iterator itr = m_rows.iterator();
		while (itr.hasNext()) {
			TableRow row = (TableRow) itr.next();
//			row.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out));
//			row.setVisibility(View.INVISIBLE);
			results_layout.removeView(row);
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

