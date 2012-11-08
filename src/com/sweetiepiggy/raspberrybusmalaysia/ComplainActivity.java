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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TimePicker;

import com.sweetiepiggy.raspberrybusmalaysia.DataWrapper.date_and_time;

public class ComplainActivity extends Activity
{

	private DataWrapper mData;
	private DbAdapter mDbHelper;

	private static final int SCHED_DATE_DIALOG_ID = 0;
	private static final int SCHED_TIME_DIALOG_ID = 1;

	private static final int ACTIVITY_FROM = 0;
	private static final int ACTIVITY_TO = 1;

	/* TODO: move this to Constants.java */
	private static final String EMAIL_ADDRESS = "sweetiepiggyapps@gmail.com";
	private static final String EMAIL_SUBJECT = "Aduan Bas Ekspres";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.complain);

		mDbHelper = new DbAdapter();
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
		init_map_buttons();
		init_cancel_button();
		init_submit_button();
	}

	@Override
	protected void onDestroy()
	{
		String sched_time = format_time(mData.sched_time);
		String agent = ((AutoCompleteTextView) findViewById(R.id.agent_entry)).getText().toString();
		String operator = ((AutoCompleteTextView) findViewById(R.id.operator_entry)).getText().toString();
		String from_city = ((AutoCompleteTextView) findViewById(R.id.from_city_entry)).getText().toString();
		String from_station = ((AutoCompleteTextView) findViewById(R.id.from_station_entry)).getText().toString();
		String to_city = ((AutoCompleteTextView) findViewById(R.id.to_city_entry)).getText().toString();
		String to_station = ((AutoCompleteTextView) findViewById(R.id.to_station_entry)).getText().toString();
		String counter_num = ((AutoCompleteTextView) findViewById(R.id.counter_num_entry)).getText().toString();
		String comment = ((EditText) findViewById(R.id.comment_entry)).getText().toString();
		String reg = ((EditText) findViewById(R.id.reg_entry)).getText().toString();

		if (mDbHelper != null) {
			mDbHelper.save_tmp_complaint(agent, operator, from_city,
					from_station, to_city, to_station, sched_time,
					counter_num, comment, reg);
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

		update_date_label(R.id.sched_date_button, mData.sched_time);
		update_time_label(R.id.sched_time_button, mData.sched_time);
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
		Cursor c_sched_time = mDbHelper.fetch_tmp_complaint_sched_time();
		init_time(c_sched_time, data.sched_time);
	}

	private void init_time(Cursor c, date_and_time dt)
	{
		if (c.moveToFirst()) {
			dt.year = Integer.parseInt(c.getString(1));
			dt.month = Integer.parseInt(c.getString(2)) - 1;
			dt.day = Integer.parseInt(c.getString(3));
			dt.hour = Integer.parseInt(c.getString(4));
			dt.minute = Integer.parseInt(c.getString(5));
		} else {
			final Calendar cal = Calendar.getInstance();
			dt.year = cal.get(Calendar.YEAR);
			dt.month = cal.get(Calendar.MONTH);
			dt.day = cal.get(Calendar.DAY_OF_MONTH);
			dt.hour = cal.get(Calendar.HOUR_OF_DAY);
			dt.minute = cal.get(Calendar.MINUTE);
		}
	}

	private void init_entries()
	{
		update_city_autocomplete(R.id.from_city_entry);
		update_city_autocomplete(R.id.to_city_entry);
		update_station_autocomplete(R.id.to_station_entry);
		update_station_autocomplete(R.id.from_station_entry);
		update_agent_autocomplete(R.id.agent_entry);
		update_operator_autocomplete(R.id.operator_entry);
		update_counter_num_autocomplete(R.id.counter_num_entry);

		String from_city = mDbHelper.fetch_tmp_complaint(DbAdapter.KEY_FROM_CITY);
		String from_station = mDbHelper.fetch_tmp_complaint(DbAdapter.KEY_FROM_STN);
		String to_city = mDbHelper.fetch_tmp_complaint(DbAdapter.KEY_TO_CITY);
		String to_station = mDbHelper.fetch_tmp_complaint(DbAdapter.KEY_TO_STN);
		String agent = mDbHelper.fetch_tmp_complaint(DbAdapter.KEY_AGENT);
		String operator = mDbHelper.fetch_tmp_complaint(DbAdapter.KEY_OPERATOR);
		String counter_num = mDbHelper.fetch_tmp_complaint(DbAdapter.KEY_CTR);
		String comment = mDbHelper.fetch_tmp_complaint(DbAdapter.KEY_COMMENT);
		String reg = mDbHelper.fetch_tmp_complaint(DbAdapter.KEY_REG);

		((AutoCompleteTextView) findViewById(R.id.from_city_entry)).setText(from_city);
		((AutoCompleteTextView) findViewById(R.id.from_station_entry)).setText(from_station);
		((AutoCompleteTextView) findViewById(R.id.to_city_entry)).setText(to_city);
		((AutoCompleteTextView) findViewById(R.id.to_station_entry)).setText(to_station);
		((AutoCompleteTextView) findViewById(R.id.agent_entry)).setText(agent);
		((AutoCompleteTextView) findViewById(R.id.operator_entry)).setText(operator);
		((AutoCompleteTextView) findViewById(R.id.counter_num_entry)).setText(counter_num);

		((EditText) findViewById(R.id.comment_entry)).setText(comment);
		((EditText) findViewById(R.id.reg_entry)).setText(reg);
	}

	private void update_city_autocomplete(int id)
	{
		ArrayAdapter<String> cities = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_cities();
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			cities.add(c.getString(c.getColumnIndex(DbAdapter.KEY_CITY)));
		} while (c.moveToNext());
		AutoCompleteTextView cities_entry = (AutoCompleteTextView) findViewById(id);
		cities_entry.setThreshold(2);
		cities_entry.setAdapter(cities);
	}

	private void update_station_autocomplete(int id)
	{
		ArrayAdapter<String> stations = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_stations();
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			stations.add(c.getString(c.getColumnIndex(DbAdapter.KEY_STN)));
		} while (c.moveToNext());
		AutoCompleteTextView stations_entry = (AutoCompleteTextView) findViewById(id);
		stations_entry.setThreshold(1);
		stations_entry.setAdapter(stations);
	}

	private void update_agent_autocomplete(int id)
	{
		ArrayAdapter<String> agents = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_agents();
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			agents.add(c.getString(c.getColumnIndex(DbAdapter.KEY_AGENT)));
		} while (c.moveToNext());
		AutoCompleteTextView agents_entry = (AutoCompleteTextView) findViewById(id);
		agents_entry.setThreshold(1);
		agents_entry.setAdapter(agents);
	}

	private void update_operator_autocomplete(int id)
	{
		ArrayAdapter<String> operators = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_operators();
		startManagingCursor(c);
		if (c.moveToFirst()) do {
			operators.add(c.getString(c.getColumnIndex(DbAdapter.KEY_OPERATOR)));
		} while (c.moveToNext());
		AutoCompleteTextView operators_entry = (AutoCompleteTextView) findViewById(id);
		operators_entry.setThreshold(1);
		operators_entry.setAdapter(operators);
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
				submit();
			}
		});
	}

	private void init_map_buttons()
	{
		Button from_map_button = (Button) findViewById(R.id.from_map_button);
		from_map_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RbmMapActivity.class);
				Bundle b = new Bundle();
				b.putBoolean("set_result", true);
				intent.putExtras(b);
				startActivityForResult(intent, ACTIVITY_FROM);
			}
		});

		Button to_map_button = (Button) findViewById(R.id.to_map_button);
		to_map_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RbmMapActivity.class);
				Bundle b = new Bundle();
				b.putBoolean("set_result", true);
				intent.putExtras(b);
				startActivityForResult(intent, ACTIVITY_TO);
			}
		});
	}

	private void init_cancel_button()
	{
		Button cancel_button = (Button)findViewById(R.id.cancel_button);
		cancel_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDbHelper.clear_tmp_complaint_table();
				init_vars(mData);
				init_entries();
				init_date_time_buttons();
			}
		});
	}

	private void submit()
	{
		String sched_time = format_time(mData.sched_time);
		String agent = ((AutoCompleteTextView) findViewById(R.id.agent_entry)).getText().toString();
		String operator = ((AutoCompleteTextView) findViewById(R.id.operator_entry)).getText().toString();
		String from_city = ((AutoCompleteTextView) findViewById(R.id.from_city_entry)).getText().toString();
		String from_station = ((AutoCompleteTextView) findViewById(R.id.from_station_entry)).getText().toString();
		String to_city = ((AutoCompleteTextView) findViewById(R.id.to_city_entry)).getText().toString();
		String to_station = ((AutoCompleteTextView) findViewById(R.id.to_station_entry)).getText().toString();
		String counter_num = ((AutoCompleteTextView) findViewById(R.id.counter_num_entry)).getText().toString();
		String comment = ((EditText) findViewById(R.id.comment_entry)).getText().toString();
		String reg = ((EditText) findViewById(R.id.reg_entry)).getText().toString();

		send_email(agent, operator, from_city,
			from_station, to_city, to_station, sched_time,
			counter_num, comment, reg);
	}

	private void send_email(String agent, String operator,
			String from_city, String from_station, String to_city,
			String to_station, String scheduled_departure,
			String counter, String comment, String reg)
	{
		final String msg = format_email(agent, operator, from_city,
				from_station, to_city, to_station, scheduled_departure,
				counter, comment, reg);

		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {EMAIL_ADDRESS} );
		intent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
		intent.putExtra(Intent.EXTRA_TEXT, msg);
		intent.setType("text/plain");
		startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
	}

	private String format_email(String agent, String operator,
			String from_city, String from_station, String to_city,
			String to_station, String scheduled_departure,
			String counter, String comment, String reg)
	{
		String msg = getResources().getString(R.string.email_intro);

		if (scheduled_departure.length() != 0) {
			msg += '\n' + scheduled_departure;
		}

		if (reg.length() != 0) {
			msg += '\n' + getResources().getString(R.string.email_reg) + ": " + reg;
		}

		if (from_city.length() != 0 || from_station.length() != 0) {
			msg += '\n' + getResources().getString(R.string.email_from) + ": ";
			if (from_station.length() != 0) {
				msg += from_station;
			}
			if (from_city.length() != 0 && !from_city.equals(from_station)) {
				if (from_station.length() != 0) {
					msg += ", ";
				}
				msg += from_city;
			}
		}

		if (to_city.length() != 0 || to_station.length() != 0) {
			msg += '\n' + getResources().getString(R.string.email_to) + ": ";
			if (to_station.length() != 0) {
				msg += to_station;
			}
			if (to_city.length() != 0 && !to_city.equals(to_station)) {
				if (to_station.length() != 0) {
					msg += ", ";
				}
				msg += to_city;
			}
		}

		if (agent.length() != 0) {
			msg += '\n' + getResources().getString(R.string.email_agent) + ": " + agent;
		}

		if (counter.length() != 0) {
			msg += '\n' + getResources().getString(R.string.email_counter) + ": " + counter;
		}

		if (operator.length() != 0) {
			msg += '\n' + getResources().getString(R.string.email_operator) + ": " + operator;
		}

		if (comment.length() != 0) {
			msg += '\n' + getResources().getString(R.string.email_offence) + ": " + comment;
		}

		return msg;
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

		switch (id) {
		case SCHED_DATE_DIALOG_ID:
			return new DatePickerDialog(this, sched_date_listener, mData.sched_time.year,
					mData.sched_time.month, mData.sched_time.day);
		case SCHED_TIME_DIALOG_ID:
			return new TimePickerDialog(this, sched_time_listener, mData.sched_time.hour,
					mData.sched_time.minute, false);
		}

		return null;
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
					String station = b.getString("station");
					String city = b.getString("city");
					((AutoCompleteTextView) findViewById(R.id.from_city_entry)).setText(city);
					((AutoCompleteTextView) findViewById(R.id.from_station_entry)).setText(station);
				}
			}
			break;
		case ACTIVITY_TO:
			if (result_code == RESULT_OK) {
				Bundle b = data.getExtras();
				if (b != null) {
					String station = b.getString("station");
					String city = b.getString("city");
					((AutoCompleteTextView) findViewById(R.id.to_city_entry)).setText(city);
					((AutoCompleteTextView) findViewById(R.id.to_station_entry)).setText(station);
				}
			}
			break;
		}
	}
}

