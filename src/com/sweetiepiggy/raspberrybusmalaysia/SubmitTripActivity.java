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
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sweetiepiggy.raspberrybusmalaysia.DataWrapper.date_and_time;

public class SubmitTripActivity extends Activity {

	private DataWrapper mData;

	static final int SCHED_DATE_DIALOG_ID = 0;
	static final int SCHED_TIME_DIALOG_ID = 1;
	static final int DEPART_DATE_DIALOG_ID = 2;
	static final int DEPART_TIME_DIALOG_ID = 3;
	static final int ARRIVAL_DATE_DIALOG_ID = 4;
	static final int ARRIVAL_TIME_DIALOG_ID = 5;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submit_trip);
		mData = (DataWrapper) getLastNonConfigurationInstance();
		if (mData == null) {
			mData = new DataWrapper();
			init_vars(mData);
//			init_entries();
		}
		init_date_time_buttons();
		init_submit_button();
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

		data.depart_time = data.sched_time;
		data.arrival_time = data.sched_time;
	}

	private void update_date_label(int button_id, date_and_time d)
	{
		Button date_button = (Button)findViewById(button_id);
		String date = DateFormat.getMediumDateFormat(getApplicationContext()).format(new Date(d.year - 1900, d.month, d.day));
		date_button.setText(date);
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

				if (((EditText) findViewById(R.id.from_city_entry)).getText().toString().length() == 0) {
					results_complete = false;
					incomplete_msg = getResources().getString(R.string.missing_from_city);
				} else if (((EditText) findViewById(R.id.to_city_entry)).getText().toString().length() == 0) {
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

	private void submit()
	{
		String sched_time = format_time(mData.sched_time);
		String depart_time = format_time(mData.depart_time);
		String arrival_time = format_time(mData.arrival_time);
		String company = ((EditText) findViewById(R.id.company_entry)).getText().toString();
		String brand = ((EditText) findViewById(R.id.brand_entry)).getText().toString();
		String from_city = ((EditText) findViewById(R.id.from_city_entry)).getText().toString();
		String from_station = ((EditText) findViewById(R.id.from_station_entry)).getText().toString();
		String to_city = ((EditText) findViewById(R.id.to_city_entry)).getText().toString();
		String to_station = ((EditText) findViewById(R.id.to_station_entry)).getText().toString();
		String counter_num = ((EditText) findViewById(R.id.counter_num_entry)).getText().toString();
		String counter_name = ((EditText) findViewById(R.id.counter_name_entry)).getText().toString();
		String counter_station = ((EditText) findViewById(R.id.counter_station_entry)).getText().toString();

		DbAdapter mDbHelper = new DbAdapter();
		mDbHelper.open_readwrite(this);

		long row_id = mDbHelper.create_trip(company, brand, from_city,
				from_station, to_city, to_station, sched_time,
				depart_time, arrival_time, counter_num,
				counter_name);

		int msg_id = (row_id == -1) ? R.string.submit_trip_fail :
			R.string.submit_trip_success;

		Toast.makeText(getApplicationContext(), getResources().getString(msg_id),
				Toast.LENGTH_SHORT).show();
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

