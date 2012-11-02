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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RouteActivity extends Activity
{
	private String m_from_city = "";
	private String m_to_city = "";

	private DbAdapter mDbHelper;
	private ArrayList<TableRow> m_rows = new ArrayList<TableRow>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route);

		((RadioButton) findViewById(R.id.company_radio)).setChecked(true);

		mDbHelper = new DbAdapter();
		mDbHelper.open(this);
		fill_data();
	}

	@Override
	protected void onDestroy() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
		super.onDestroy();
	}

	private void fill_data()
	{
		Cursor c = mDbHelper.fetch_from_cities();
		startManagingCursor(c);
		SimpleCursorAdapter from_cities = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_item,
				c, new String[] {DbAdapter.KEY_FROM_CITY},
				new int[] {android.R.id.text1});
		Spinner from_spinner = (Spinner) findViewById(R.id.from_spinner);
		from_spinner.setAdapter(from_cities);
		spinner_set_selection(from_spinner, mDbHelper.get_most_freq_from_city());

		from_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent,
					View selected_item, int pos,
					long id)
			{
				m_from_city = ((Cursor)parent.getItemAtPosition(pos)).getString(1);
				Cursor c = mDbHelper.fetch_to_cities(m_from_city);
				startManagingCursor(c);
				SimpleCursorAdapter to_cities = new SimpleCursorAdapter(getApplicationContext(),
						android.R.layout.simple_spinner_item,
						c, new String[] {DbAdapter.KEY_TO_CITY},
						new int[] {android.R.id.text1});
				Spinner to_spinner = (Spinner) findViewById(R.id.to_spinner);
				to_spinner.setAdapter(to_cities);
				spinner_set_selection(to_spinner, mDbHelper.get_most_freq_to_city(m_from_city));

				to_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View selected_item, int pos,
							long id) {
						m_to_city = ((Cursor)parent.getItemAtPosition(pos)).getString(1);
						print_rows(m_from_city, m_to_city, DbAdapter.AVG_TIME);
					}
					@Override
					public void onNothingSelected(AdapterView<?> parentView) {
					}

				});
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView)
			{
			}

		});
	}

	private void spinner_set_selection(Spinner spinner, String value)
	{
		for (int i = 0; i < spinner.getCount(); ++i) {
			if (((Cursor)spinner.getItemAtPosition(i)).getString(1).equals(value)) {
				spinner.setSelection(i);
			}
		}
	}

	public void on_radio_button_clicked(View v)
	{
		boolean checked = ((RadioButton) v).isChecked();

		if (checked) {
			print_rows(m_from_city, m_to_city, DbAdapter.AVG_TIME);
		}
	}

	private void init_labels(final String from_city, final String to_city)
	{
		TextView company = (TextView) findViewById(R.id.company);
		int company_text_id = ((RadioButton) findViewById(R.id.bus_brand_radio)).isChecked() ?
			R.string.brand : R.string.company;
		company.setText(company_text_id);
		company.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				String company_key = ((RadioButton) findViewById(R.id.bus_brand_radio)).isChecked() ?
					DbAdapter.KEY_BRAND : DbAdapter.KEY_COMP;
				print_rows(from_city, to_city, company_key);
			}
		});

		TextView avg_time = (TextView) findViewById(R.id.avg_time);
		avg_time.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				print_rows(from_city, to_city, DbAdapter.AVG_TIME);
			}
		});

		TextView avg_delay = (TextView) findViewById(R.id.avg_delay);
		avg_delay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				print_rows(from_city, to_city, DbAdapter.AVG_DELAY);
			}
		});

		TextView num_trips = (TextView) findViewById(R.id.num_trips);
		num_trips.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				print_rows(from_city, to_city, DbAdapter.NUM_TRIPS);
			}
		});
	}

	private void print_rows(String from_city, String to_city, String sort_by)
	{
		init_labels(from_city, to_city);

		String group_by = ((RadioButton) findViewById(R.id.bus_brand_radio)).isChecked() ?
			DbAdapter.KEY_BRAND : DbAdapter.KEY_COMP;
		Cursor c = mDbHelper.fetch_avg(from_city, to_city, group_by, sort_by);
		startManagingCursor(c);

		clear_rows();
		if (c.moveToFirst()) do {
			String company = c.getString(c.getColumnIndex(DbAdapter.KEY_COMP));
			String brand = c.getString(c.getColumnIndex(DbAdapter.KEY_BRAND));
			String avg = format_time(c.getInt(c.getColumnIndex(DbAdapter.AVG_TIME)));
			String delay = format_time_min(c.getInt(c.getColumnIndex(DbAdapter.AVG_DELAY)));
			String count = c.getString(c.getColumnIndex(DbAdapter.NUM_TRIPS));

			String disp_comp = ((RadioButton) findViewById(R.id.bus_brand_radio)).isChecked() ?
				brand : company;

			print_row(disp_comp, avg, delay, count, from_city,
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
		return String.format("%s%d%s %02d%s", negative, hr, getResources().getString(R.string.hour_abbr),
				min, getResources().getString(R.string.minute_abbr));
	}

	private void print_row(String company, String avg, String delay,
			String count, String from_city, String to_city)
	{
		String display_company = company;
		if (company.length() > 15) {
//			company = company.substring(0, 15);
			display_company = company.replace(' ', '\n');
		} else if (company.length() == 0) {
			display_company = getResources().getString(R.string.unknown);
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

		TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
				TableRow.LayoutParams.FILL_PARENT, 1.0f);
		tr.setGravity(Gravity.CENTER);
		tr.setLayoutParams(lp);

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
				b.putBoolean("is_brand", ((RadioButton) findViewById(R.id.bus_brand_radio)).isChecked());
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
		return String.format("%s%d%s", negative, min, getResources().getString(R.string.minute_abbr));
	}
}

