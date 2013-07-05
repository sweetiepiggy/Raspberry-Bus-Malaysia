/*
    Copyright (C) 2012,2013 Sweetie Piggy Apps <sweetiepiggyapps@gmail.com>

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
import android.view.ViewGroup.LayoutParams;
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

		/* remove newline for delay label */
		TextView avg_delay_label = (TextView) findViewById(R.id.avg_delay_label);
		String avg_delay_str = avg_delay_label.getText().toString();
		avg_delay_label.setText(avg_delay_str.replace('\n', ' '));

		mDbHelper = new DbAdapter();
		mDbHelper.open(this);

		float rating = m_is_operator ?
			mDbHelper.getOperatorRating(company) :
			mDbHelper.getAgentRating(company);
		((RatingBar) findViewById(R.id.rating_bar)).setRating(rating);
		float comfort = m_is_operator ?
			mDbHelper.getOperatorComfort(company) :
			mDbHelper.getAgentComfort(company);
		((RatingBar) findViewById(R.id.comfort_bar)).setRating(comfort);
		float safety = m_is_operator ?
			mDbHelper.getOperatorSafety(company) :
			mDbHelper.getAgentSafety(company);
		((RatingBar) findViewById(R.id.safety_bar)).setRating(safety);

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

				print_route_row(company, from_city, to_city);
			} while (c_to.moveToNext());
		} while (c_from.moveToNext());

		Cursor c_revw = m_is_operator ?
			mDbHelper.fetchOperatorReviews(company) :
			mDbHelper.fetchAgentReviews(company);
		startManagingCursor(c_revw);
		if (c_revw.moveToFirst()) do {
			String review = c_revw.getString(c_revw.getColumnIndex(DbAdapter.KEY_COMMENT));
			print_review_row(review);
			print_review_row("");
		} while (c_revw.moveToNext());
	}

	@Override
	protected void onDestroy() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
		super.onDestroy();
	}

	private void print_route_row(String company, String from_city,
			String to_city)
	{
		TextView from_view = new TextView(getApplicationContext());
		TextView to_view = new TextView(getApplicationContext());

		from_view.setText(from_city);
		to_view.setText(to_city);

		TableRow tr = new TableRow(getApplicationContext());
		tr.addView(from_view);
		tr.addView(to_view);

		TableLayout results_layout = (TableLayout) findViewById(R.id.results_layout);
		results_layout.addView(tr);
	}

	private void print_review_row(String review)
	{
		TextView review_view = new TextView(getApplicationContext());
		LayoutParams params = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		review_view.setLayoutParams(params);

		review_view.setText(review);

		TableRow tr = new TableRow(getApplicationContext());
		tr.addView(review_view);

		TableLayout review_layout = (TableLayout) findViewById(R.id.review_layout);
		review_layout.addView(tr);
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

