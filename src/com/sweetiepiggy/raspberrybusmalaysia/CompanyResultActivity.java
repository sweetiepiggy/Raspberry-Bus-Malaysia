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
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CompanyResultActivity extends Activity
{
	private DbAdapter mDbHelper;
	private boolean m_is_operator = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.company_result);

		Bundle b = getIntent().getExtras();
		m_is_operator = (b == null) ? false : b.getBoolean("is_operator");
		String company = (b == null) ? "<NULL>" : b.getString("company");
		String company_display = company.length() == 0 ? getResources().getString(R.string.unknown) : company;
		((TextView) findViewById(R.id.title)).setText(company_display);

		mDbHelper = new DbAdapter();
		mDbHelper.open(this);

		float rating = m_is_operator ?
			mDbHelper.getOperatorRating(company) :
			mDbHelper.getAgentRating(company);
		((RatingBar) findViewById(R.id.rating_bar)).setRating(rating);

		Cursor c_comp = m_is_operator ?
			mDbHelper.fetch_avg_operator_delay(company) :
			mDbHelper.fetch_avg_agent_delay(company);
		startManagingCursor(c_comp);
		if (c_comp.moveToFirst()) do {
			String avg_delay = format_time_min(c_comp.getInt(c_comp.getColumnIndex(DbAdapter.AVG_DELAY)));
			((TextView) findViewById(R.id.total_avg_delay)).setText(avg_delay);
		} while (c_comp.moveToNext());

		Cursor c_from = m_is_operator ?
			mDbHelper.fetch_operator_from_cities(company) :
			mDbHelper.fetch_agent_from_cities(company);
		startManagingCursor(c_from);
		if (c_from.moveToFirst()) do {
			String from_city = c_from.getString(c_from.getColumnIndex(DbAdapter.KEY_FROM_CITY));

			Cursor c_to = m_is_operator ?
				mDbHelper.fetch_operator_to_cities(from_city, company) :
				mDbHelper.fetch_agent_to_cities(from_city, company);
			startManagingCursor(c_to);
			if (c_to.moveToFirst()) do {
				String to_city = c_to.getString(c_to.getColumnIndex(DbAdapter.KEY_TO_CITY));

				Cursor c_avg = m_is_operator ?
					mDbHelper.fetch_operator_avg(from_city, to_city, company) :
					mDbHelper.fetch_agent_avg(from_city, to_city, company);
				startManagingCursor(c_avg);
				if (c_avg.moveToFirst()) {
					/* TODO: check that getColumnIndex is not -1 */
					String avg = format_time(c_avg.getInt(c_avg.getColumnIndex(DbAdapter.AVG_TIME)));
					String avg_delay = format_time_min(c_avg.getInt(c_avg.getColumnIndex(DbAdapter.AVG_DELAY)));
					String count = c_avg.getString(c_avg.getColumnIndex(DbAdapter.NUM_TRIPS));
					print_row(company, from_city, to_city, avg, avg_delay, count);
				}

			} while (c_to.moveToNext());
		} while (c_from.moveToNext());
	}

	@Override
	protected void onDestroy() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
		super.onDestroy();
	}

	private void print_row(String company, String from_city,
			String to_city, String avg, String avg_delay,
			String count)
	{
		TextView route_view = new TextView(getApplicationContext());
		TextView avg_view = new TextView(getApplicationContext());
		TextView avg_delay_view = new TextView(getApplicationContext());
		Button count_view = new Button(getApplicationContext());

		route_view.setGravity(Gravity.CENTER);
		avg_view.setGravity(Gravity.CENTER);
		avg_delay_view.setGravity(Gravity.CENTER);
		count_view.setGravity(Gravity.CENTER);

		route_view.setText(from_city + "\n-> " + to_city);
		avg_view.setText(avg);
		avg_delay_view.setText(avg_delay);
		count_view.setText(count);

		init_count_button(count_view, company, from_city, to_city);

		TableRow tr = new TableRow(getApplicationContext());
		tr.addView(route_view);
		tr.addView(avg_view);
		tr.addView(avg_delay_view);
		tr.addView(count_view);

		TableLayout results_layout = (TableLayout) findViewById(R.id.results_layout);
		results_layout.addView(tr);
	}

	/* TODO: don't duplicate init_count_button() in other files */
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
				b.putBoolean("is_operator", m_is_operator);
				intent.putExtras(b);
				startActivity(intent);
			}
		});
	}

	/* TODO: don't duplicate format_time() in other files */
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
}

