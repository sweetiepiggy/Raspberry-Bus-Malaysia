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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class RouteActivity extends Activity
{
	private String m_from_city = "";
	private String m_to_city = "";

	private DbAdapter mDbHelper;

	private static final int ACTIVITY_FROM = 0;
	private static final int ACTIVITY_TO = 1;

	private class RouteListAdapter extends ResourceCursorAdapter
	{
		public RouteListAdapter(Context context, Cursor c)
		{
			super(context, R.layout.route_row, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor c)
		{
			TextView company_v = (TextView) view.findViewById(R.id.company);
			TextView avg_v = (TextView) view.findViewById(R.id.avg);
			TextView count_v = (TextView) view.findViewById(R.id.count);
			RatingBar overall_v = (RatingBar) view.findViewById(R.id.overall_bar);

			final int row_id = c.getInt(c.getColumnIndex(DbAdapter.KEY_ROWID));
			String agent = c.getString(c.getColumnIndex(DbAdapter.KEY_AGENT));
			String operator = c.getString(c.getColumnIndex(DbAdapter.KEY_OPERATOR));
			String avg = format_time(c.getInt(c.getColumnIndex(DbAdapter.AVG_TIME)));
			String count = c.getString(c.getColumnIndex(DbAdapter.NUM_TRIPS));
			float avg_overall = c.getFloat(c.getColumnIndex(DbAdapter.AVG_OVERALL));

			String company = ((RadioButton) findViewById(R.id.operator_radio)).isChecked() ?
				operator : agent;

			if (company.length() > 15) {
				company = company.replace(' ', '\n');
			} else if (company.length() == 0) {
				company = getResources().getString(R.string.unknown);
			}

			if (count == null) {
				count = "0";
			}

			company_v.setText(company);
			avg_v.setText(avg);
			count_v.setText(count + " " + getResources().getString(R.string.reviews));
			overall_v.setRating(avg_overall);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route);

		((RadioButton) findViewById(R.id.agent_radio)).setChecked(true);

		mDbHelper = new DbAdapter();
		mDbHelper.open(this);
		init_from_map_button();
		init_from_spinner(null);
	}

	@Override
	protected void onDestroy() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
		super.onDestroy();
	}

	private void init_from_map_button()
	{
		Button from_map_button = (Button) findViewById(R.id.from_map_button);
		from_map_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RbmMapActivity.class);
				Bundle b = new Bundle();
				b.putBoolean("set_result", true);
				b.putBoolean("valid_from", true);
				intent.putExtras(b);
				startActivityForResult(intent, ACTIVITY_FROM);
			}
		});
	}

	private void init_to_map_button(final String from_city)
	{
		Button to_map_button = (Button) findViewById(R.id.to_map_button);
		to_map_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RbmMapActivity.class);
				Bundle b = new Bundle();
				b.putBoolean("set_result", true);
				b.putBoolean("valid_to", true);
				b.putString("from_city", from_city);
				intent.putExtras(b);
				startActivityForResult(intent, ACTIVITY_TO);
			}
		});
	}

	private void init_from_spinner(String set_from_city)
	{
		Cursor c = mDbHelper.fetch_from_cities();
		startManagingCursor(c);
		SimpleCursorAdapter from_cities = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_item,
				c, new String[] {DbAdapter.KEY_FROM_CITY},
				new int[] {android.R.id.text1});
		from_cities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner from_spinner = (Spinner) findViewById(R.id.from_spinner);
		from_spinner.setAdapter(from_cities);

		if (set_from_city == null) {
			set_from_city = mDbHelper.get_most_freq_from_city();
		}
		spinner_set_selection(from_spinner, set_from_city);

		from_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent,
					View selected_item, int pos,
					long id)
			{
				String new_from_city = ((Cursor)parent.getItemAtPosition(pos)).getString(1);
				if (!m_from_city.equals(new_from_city)) {
					m_from_city = new_from_city;
					init_to_map_button(m_from_city);
					init_to_spinner(m_from_city, null);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView)
			{
			}

		});
	}

	private void init_to_spinner(final String from_city, String set_to_city)
	{
		Cursor c = mDbHelper.fetch_to_cities(from_city);
		startManagingCursor(c);
		SimpleCursorAdapter to_cities = new SimpleCursorAdapter(getApplicationContext(),
				android.R.layout.simple_spinner_item,
				c, new String[] {DbAdapter.KEY_TO_CITY},
				new int[] {android.R.id.text1});
		to_cities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner to_spinner = (Spinner) findViewById(R.id.to_spinner);
		to_spinner.setAdapter(to_cities);

		if (set_to_city == null) {
			set_to_city = mDbHelper.get_most_freq_to_city(from_city);
		}
		spinner_set_selection(to_spinner, set_to_city);

		to_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent,
					View selected_item, int pos,
					long id) {
				m_to_city = ((Cursor) parent.getItemAtPosition(pos)).getString(1);
				print_rows(from_city, m_to_city);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}

		});
	}

	private void spinner_set_selection(Spinner spinner, String value)
	{
		boolean done = false;
		for (int i = 0; i < spinner.getCount() && !done; ++i) {
			if (((Cursor)spinner.getItemAtPosition(i)).getString(1).equals(value)) {
				spinner.setSelection(i);
				done = true;
			}
		}
	}

	public void on_radio_button_clicked(View v)
	{
		boolean checked = ((RadioButton) v).isChecked();

		if (checked) {
			print_rows(m_from_city, m_to_city);
		}
	}

	private void print_rows(String from_city, String to_city)
	{
		String group_by = ((RadioButton) findViewById(R.id.operator_radio)).isChecked() ?
			DbAdapter.KEY_OPERATOR : DbAdapter.KEY_AGENT;
		Cursor c = mDbHelper.fetch_avg(from_city, to_city, group_by);
		startManagingCursor(c);
		ListView lv = (ListView) findViewById(R.id.results_list);
		lv.setAdapter(new RouteListAdapter(this, c));
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int pos, long id) {
				String group_by = ((RadioButton) findViewById(R.id.operator_radio)).isChecked() ?
					DbAdapter.KEY_OPERATOR : DbAdapter.KEY_AGENT;
				String company = mDbHelper.getCompany(id, group_by);
				Intent intent = new Intent(getApplicationContext(),
					CompanyResultActivity.class);
				Bundle b = new Bundle();
				b.putString("company", company);
				b.putBoolean("is_operator", ((RadioButton) findViewById(R.id.operator_radio)).isChecked());
				intent.putExtras(b);
				startActivity(intent);
			}
		});
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

	@Override
	protected void onActivityResult(int request_code, int result_code, Intent data)
	{
		super.onActivityResult(request_code, result_code, data);

		switch (request_code) {
		case ACTIVITY_FROM:
			if (result_code == RESULT_OK) {
				Bundle b = data.getExtras();
				if (b != null) {
					String city = b.getString("city");
					init_from_spinner(city);
				}
			}
			break;
		case ACTIVITY_TO:
			if (result_code == RESULT_OK) {
				Bundle b = data.getExtras();
				if (b != null) {
					String city = b.getString("city");
					init_to_spinner(m_from_city, city);
				}
			}
			break;
		}
	}
}

