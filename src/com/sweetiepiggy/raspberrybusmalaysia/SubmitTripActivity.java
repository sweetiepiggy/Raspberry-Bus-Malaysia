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

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sweetiepiggy.raspberrybusmalaysia.DataWrapper.date_and_time;

public class SubmitTripActivity extends Activity
{

	private DataWrapper mData;
	private DbAdapter mDbHelper;

	static final int SCHED_DATE_DIALOG_ID = 0;
	static final int SCHED_TIME_DIALOG_ID = 1;
	static final int DEPART_DATE_DIALOG_ID = 2;
	static final int DEPART_TIME_DIALOG_ID = 3;
	static final int ARRIVAL_DATE_DIALOG_ID = 4;
	static final int ARRIVAL_TIME_DIALOG_ID = 5;

	/* TODO: move this to Constants.java */
	static final String EMAIL_ADDRESS = "sweetiepiggyapps@gmail.com";
	static final String EMAIL_SUBJECT = "Raspberry Bus Malaysia Trip Submission";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submit_trip);

		mDbHelper = new DbAdapter();
		/* TODO: close() */
		mDbHelper.open(this);

		if (savedInstanceState == null) {
			mData = (DataWrapper) getLastNonConfigurationInstance();
			if (mData == null) {
				mData = new DataWrapper();
				init_vars(mData);
				init_entries();
			}
		} else {
			mData = new DataWrapper();
			restore_saved_state(savedInstanceState);
		}

		init_date_time_buttons();
		init_cancel_button();
		init_submit_button();
	}

	@Override
	protected void onDestroy() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putInt("sched_year", mData.sched_time.year);
		savedInstanceState.putInt("sched_month", mData.sched_time.month);
		savedInstanceState.putInt("sched_day", mData.sched_time.day);
		savedInstanceState.putInt("sched_hour", mData.sched_time.hour);
		savedInstanceState.putInt("sched_minute", mData.sched_time.minute);

		savedInstanceState.putInt("depart_year", mData.depart_time.year);
		savedInstanceState.putInt("depart_month", mData.depart_time.month);
		savedInstanceState.putInt("depart_day", mData.depart_time.day);
		savedInstanceState.putInt("depart_hour", mData.depart_time.hour);
		savedInstanceState.putInt("depart_minute", mData.depart_time.minute);

		savedInstanceState.putInt("arrival_year", mData.arrival_time.year);
		savedInstanceState.putInt("arrival_month", mData.arrival_time.month);
		savedInstanceState.putInt("arrival_day", mData.arrival_time.day);
		savedInstanceState.putInt("arrival_hour", mData.arrival_time.hour);
		savedInstanceState.putInt("arrival_minute", mData.arrival_time.minute);

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		restore_saved_state(savedInstanceState);
	}

	private void restore_saved_state(Bundle savedInstanceState)
	{
		mData.sched_time.year = savedInstanceState.getInt("sched_year");
		mData.sched_time.month = savedInstanceState.getInt("sched_month");
		mData.sched_time.day = savedInstanceState.getInt("sched_day");
		mData.sched_time.hour = savedInstanceState.getInt("sched_hour");
		mData.sched_time.minute = savedInstanceState.getInt("sched_minute");

		mData.depart_time.year = savedInstanceState.getInt("depart_year");
		mData.depart_time.month = savedInstanceState.getInt("depart_month");
		mData.depart_time.day = savedInstanceState.getInt("depart_day");
		mData.depart_time.hour = savedInstanceState.getInt("depart_hour");
		mData.depart_time.minute = savedInstanceState.getInt("depart_minute");

		mData.arrival_time.year = savedInstanceState.getInt("arrival_year");
		mData.arrival_time.month = savedInstanceState.getInt("arrival_month");
		mData.arrival_time.day = savedInstanceState.getInt("arrival_day");
		mData.arrival_time.hour = savedInstanceState.getInt("arrival_hour");
		mData.arrival_time.minute = savedInstanceState.getInt("arrival_minute");
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return mData;
	}

	private void init_date_time_buttons()
	{
		init_date_button(R.id.sched_date_button, SCHED_DATE_DIALOG_ID);
		init_time_button(R.id.sched_time_button, SCHED_TIME_DIALOG_ID);
		init_date_button(R.id.depart_date_button, DEPART_DATE_DIALOG_ID);
		init_time_button(R.id.depart_time_button, DEPART_TIME_DIALOG_ID);
		init_date_button(R.id.arrival_date_button, ARRIVAL_DATE_DIALOG_ID);
		init_time_button(R.id.arrival_time_button, ARRIVAL_TIME_DIALOG_ID);

		update_date_label(R.id.sched_date_button, mData.sched_time);
		update_time_label(R.id.sched_time_button, mData.sched_time);
		update_date_label(R.id.depart_date_button, mData.depart_time);
		update_time_label(R.id.depart_time_button, mData.depart_time);
		update_date_label(R.id.arrival_date_button, mData.arrival_time);
		update_time_label(R.id.arrival_time_button, mData.arrival_time);
	}

	private void init_date_button(int button_id, final int dialog_id)
	{
		Button date_button = (Button)findViewById(button_id);
		date_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(dialog_id);
			}
		});

	}

	private void init_time_button(int button_id, final int dialog_id)
	{
		Button time_button = (Button)findViewById(button_id);
		time_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(dialog_id);
			}
		});
	}

	private void init_vars(DataWrapper data)
	{
		final Calendar c = Calendar.getInstance();

		data.sched_time.year = c.get(Calendar.YEAR);
		data.sched_time.month = c.get(Calendar.MONTH);
		data.sched_time.day = c.get(Calendar.DAY_OF_MONTH);
		data.sched_time.hour = c.get(Calendar.HOUR_OF_DAY);
		data.sched_time.minute = c.get(Calendar.MINUTE);

		data.depart_time.year = c.get(Calendar.YEAR);
		data.depart_time.month = c.get(Calendar.MONTH);
		data.depart_time.day = c.get(Calendar.DAY_OF_MONTH);
		data.depart_time.hour = c.get(Calendar.HOUR_OF_DAY);
		data.depart_time.minute = c.get(Calendar.MINUTE);

		data.arrival_time.year = c.get(Calendar.YEAR);
		data.arrival_time.month = c.get(Calendar.MONTH);
		data.arrival_time.day = c.get(Calendar.DAY_OF_MONTH);
		data.arrival_time.hour = c.get(Calendar.HOUR_OF_DAY);
		data.arrival_time.minute = c.get(Calendar.MINUTE);
	}

	private void init_entries()
	{
		update_city_autocomplete(R.id.from_city_entry);
		update_city_autocomplete(R.id.to_city_entry);
		update_station_autocomplete(R.id.to_station_entry);
		update_station_autocomplete(R.id.from_station_entry);
		update_company_autocomplete(R.id.company_entry);
		update_brand_autocomplete(R.id.brand_entry);
		update_counter_num_autocomplete(R.id.counter_num_entry);

		((AutoCompleteTextView) findViewById(R.id.from_city_entry)).setText("");
		((AutoCompleteTextView) findViewById(R.id.to_city_entry)).setText("");
		((AutoCompleteTextView) findViewById(R.id.to_station_entry)).setText("");
		((AutoCompleteTextView) findViewById(R.id.from_station_entry)).setText("");
		((AutoCompleteTextView) findViewById(R.id.company_entry)).setText("");
		((AutoCompleteTextView) findViewById(R.id.brand_entry)).setText("");
		((AutoCompleteTextView) findViewById(R.id.counter_num_entry)).setText("");

		((RatingBar) findViewById(R.id.safety_bar)).setRating(3);
		((RatingBar) findViewById(R.id.comfort_bar)).setRating(3);
		((EditText) findViewById(R.id.comment_entry)).setText("");
		((CheckBox) findViewById(R.id.upload_checkbox)).setChecked(true);
	}

	private void update_city_autocomplete(int id)
	{
		ArrayAdapter<String> cities = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_cities();
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			/* TODO: use getColumnIndex() */
			/* TODO: verify that column 0 exists */
			cities.add(c.getString(0));
		} while (c.moveToNext());
		AutoCompleteTextView cities_entry = (AutoCompleteTextView) findViewById(id);
		cities_entry.setThreshold(1);
		cities_entry.setAdapter(cities);
	}

	private void update_station_autocomplete(int id)
	{
		ArrayAdapter<String> stations = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_stations();
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			/* TODO: verify that column 0 exists */
			stations.add(c.getString(0));
		} while (c.moveToNext());
		AutoCompleteTextView stations_entry = (AutoCompleteTextView) findViewById(id);
		stations_entry.setThreshold(1);
		stations_entry.setAdapter(stations);
	}

	private void update_company_autocomplete(int id)
	{
		ArrayAdapter<String> companies = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_companies();
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			companies.add(c.getString(c.getColumnIndex(DbAdapter.KEY_COMP)));
		} while (c.moveToNext());
		AutoCompleteTextView companies_entry = (AutoCompleteTextView) findViewById(id);
		companies_entry.setThreshold(1);
		companies_entry.setAdapter(companies);
	}

	private void update_brand_autocomplete(int id)
	{
		ArrayAdapter<String> brands = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_brands();
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			brands.add(c.getString(c.getColumnIndex(DbAdapter.KEY_BRAND)));
		} while (c.moveToNext());
		AutoCompleteTextView brands_entry = (AutoCompleteTextView) findViewById(id);
		brands_entry.setThreshold(1);
		brands_entry.setAdapter(brands);
	}

	private void update_counter_num_autocomplete(int id)
	{
		ArrayAdapter<String> counter_nums = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_counter_nums();
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			counter_nums.add(c.getString(c.getColumnIndex(DbAdapter.KEY_CTR)));
		} while (c.moveToNext());
		AutoCompleteTextView counter_nums_entry = (AutoCompleteTextView) findViewById(id);
		counter_nums_entry.setThreshold(1);
		counter_nums_entry.setAdapter(counter_nums);
	}

	private void update_date_label(int button_id, date_and_time dt)
	{
		Button date_button = (Button)findViewById(button_id);
		Date d = new Date(dt.year - 1900, dt.month, dt.day);

		String date = translate_day_of_week(DateFormat.format("EEEE", d).toString()) +
			" " + DateFormat.getLongDateFormat(getApplicationContext()).format(d);
		date_button.setText(date);
	}

	/* TODO: there should be a better way to get the translated day of week */
	private String translate_day_of_week(String day)
	{
		String ret = day;
		if (day.equals("Monday")) {
			ret = getResources().getString(R.string.monday);
		} else if (day.equals("Tuesday")) {
			ret = getResources().getString(R.string.tuesday);
		} else if (day.equals("Wednesday")) {
			ret = getResources().getString(R.string.wednesday);
		} else if (day.equals("Thursday")) {
			ret = getResources().getString(R.string.thursday);
		} else if (day.equals("Friday")) {
			ret = getResources().getString(R.string.friday);
		} else if (day.equals("Saturday")) {
			ret = getResources().getString(R.string.saturday);
		} else if (day.equals("Sunday")) {
			ret = getResources().getString(R.string.sunday);
		}
		return ret;
	}

	private void update_time_label(int button_id, date_and_time d)
	{
		Button time_button = (Button)findViewById(button_id);
		String time = DateFormat.getTimeFormat(getApplicationContext()).format(new Date(0, 0, 0, d.hour, d.minute, 0));
		time_button.setText(time);
	}

	private void init_submit_button()
	{
		Button submit_button = (Button) findViewById(R.id.submit_button);
		submit_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				boolean results_complete = true;
				String incomplete_msg = "";

				if (((AutoCompleteTextView) findViewById(R.id.from_city_entry)).getText().toString().length() == 0) {
					results_complete = false;
					incomplete_msg = getResources().getString(R.string.missing_from_city);
				} else if (((AutoCompleteTextView) findViewById(R.id.to_city_entry)).getText().toString().length() == 0) {
					results_complete = false;
					incomplete_msg = getResources().getString(R.string.missing_to_city);
				}

				if (results_complete) {
					submit();
				} else {
					Toast.makeText(getApplicationContext(), incomplete_msg,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void init_cancel_button()
	{
		Button cancel_button = (Button)findViewById(R.id.cancel_button);
		cancel_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				init_vars(mData);
				init_entries();
				init_date_time_buttons();
			}
		});
	}

	private void submit()
	{
		String sched_time = format_time(mData.sched_time);
		String depart_time = format_time(mData.depart_time);
		String arrival_time = format_time(mData.arrival_time);
		String company = ((AutoCompleteTextView) findViewById(R.id.company_entry)).getText().toString();
		String brand = ((AutoCompleteTextView) findViewById(R.id.brand_entry)).getText().toString();
		String from_city = ((AutoCompleteTextView) findViewById(R.id.from_city_entry)).getText().toString();
		String from_station = ((AutoCompleteTextView) findViewById(R.id.from_station_entry)).getText().toString();
		String to_city = ((AutoCompleteTextView) findViewById(R.id.to_city_entry)).getText().toString();
		String to_station = ((AutoCompleteTextView) findViewById(R.id.to_station_entry)).getText().toString();
		String counter_num = ((AutoCompleteTextView) findViewById(R.id.counter_num_entry)).getText().toString();
		String safety = Integer.toString((int) ((RatingBar) findViewById(R.id.safety_bar)).getRating());
		String comfort = Integer.toString((int) ((RatingBar) findViewById(R.id.comfort_bar)).getRating());
		String comment = ((EditText) findViewById(R.id.comment_entry)).getText().toString();

		DbAdapter dbHelper = new DbAdapter();
		dbHelper.open_readwrite(this);

		long row_id = dbHelper.create_trip(company, brand, from_city,
				from_station, to_city, to_station, sched_time,
				depart_time, arrival_time, counter_num,
				safety, comfort, comment);

		dbHelper.close();

		int msg_id = (row_id == -1) ? R.string.submit_trip_fail :
			R.string.submit_trip_success;

		Toast.makeText(getApplicationContext(), getResources().getString(msg_id),
				Toast.LENGTH_SHORT).show();

		if (((CheckBox) findViewById(R.id.upload_checkbox)).isChecked()) {
			send_email(company, brand, from_city,
				from_station, to_city, to_station, sched_time,
				depart_time, arrival_time, counter_num,
				safety, comfort, comment);

		}
	}

	private void send_email(String company, String bus_brand,
			String from_city, String from_station, String to_city,
			String to_station, String scheduled_departure,
			String actual_departure, String arrival_time,
			String counter, String safety, String comfort, String comment)
	{
		String msg  = company + ',' + bus_brand + ',' + from_city +
			',' + from_station + ',' + to_city + ',' + to_station +
			',' + scheduled_departure + ',' + actual_departure +
			',' + arrival_time + ',' + counter + ',' + safety +
			',' + comfort + ',' + comment + "\n";

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {EMAIL_ADDRESS} );
		intent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
		intent.putExtra(Intent.EXTRA_TEXT, msg);
		intent.setType("text/plain");
		startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
	}

	private String format_time(date_and_time d)
	{
		return String.format("%04d-%02d-%02d %02d:%02d", d.year,
				d.month+1, d.day, d.hour, d.minute);
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		DatePickerDialog.OnDateSetListener sched_date_listener =
			new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					mData.sched_time.year = year;
					mData.sched_time.month = monthOfYear;
					mData.sched_time.day = dayOfMonth;
					update_date_label(R.id.sched_date_button,
							mData.sched_time);
				}
		};

		TimePickerDialog.OnTimeSetListener sched_time_listener =
			new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view,
						int hourOfDay, int minute) {
					mData.sched_time.hour = hourOfDay;
					mData.sched_time.minute = minute;
					update_time_label(R.id.sched_time_button,
							mData.sched_time);
				}
		};

		DatePickerDialog.OnDateSetListener depart_date_listener =
			new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					mData.depart_time.year = year;
					mData.depart_time.month = monthOfYear;
					mData.depart_time.day = dayOfMonth;
					update_date_label(R.id.depart_date_button,
							mData.depart_time);
				}
		};

		TimePickerDialog.OnTimeSetListener depart_time_listener =
			new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view,
						int hourOfDay, int minute) {
					mData.depart_time.hour = hourOfDay;
					mData.depart_time.minute = minute;
					update_time_label(R.id.depart_time_button,
							mData.depart_time);
				}
		};
		DatePickerDialog.OnDateSetListener arrival_date_listener =
			new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					mData.arrival_time.year = year;
					mData.arrival_time.month = monthOfYear;
					mData.arrival_time.day = dayOfMonth;
					update_date_label(R.id.arrival_date_button,
							mData.arrival_time);
				}
		};

		TimePickerDialog.OnTimeSetListener arrival_time_listener =
			new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view,
						int hourOfDay, int minute) {
					mData.arrival_time.hour = hourOfDay;
					mData.arrival_time.minute = minute;
					update_time_label(R.id.arrival_time_button,
							mData.arrival_time);
				}
		};

		switch (id) {
		case SCHED_DATE_DIALOG_ID:
			return new DatePickerDialog(this, sched_date_listener, mData.sched_time.year,
					mData.sched_time.month, mData.sched_time.day);
		case SCHED_TIME_DIALOG_ID:
			return new TimePickerDialog(this, sched_time_listener, mData.sched_time.hour,
					mData.sched_time.minute, false);
		case DEPART_DATE_DIALOG_ID:
			return new DatePickerDialog(this, depart_date_listener, mData.depart_time.year,
					mData.depart_time.month, mData.depart_time.day);
		case DEPART_TIME_DIALOG_ID:
			return new TimePickerDialog(this, depart_time_listener, mData.depart_time.hour,
					mData.depart_time.minute, false);
		case ARRIVAL_DATE_DIALOG_ID:
			return new DatePickerDialog(this, arrival_date_listener, mData.arrival_time.year,
					mData.arrival_time.month, mData.arrival_time.day);
		case ARRIVAL_TIME_DIALOG_ID:
			return new TimePickerDialog(this, arrival_time_listener, mData.arrival_time.hour,
					mData.arrival_time.minute, false);
		}

		return null;
	}

}

