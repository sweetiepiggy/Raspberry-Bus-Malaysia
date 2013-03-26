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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
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
import android.widget.Toast;

public class SubmitTripActivity extends Activity
{
	private DataWrapper mData;
	private DbAdapter mDbHelper;

	private static final int SCHED_DATE_DIALOG_ID = 0;
	private static final int SCHED_TIME_DIALOG_ID = 1;
	private static final int DEPART_DATE_DIALOG_ID = 2;
	private static final int DEPART_TIME_DIALOG_ID = 3;
	private static final int ARRIVAL_DATE_DIALOG_ID = 4;
	private static final int ARRIVAL_TIME_DIALOG_ID = 5;

	private static final int ACTIVITY_FROM = 0;
	private static final int ACTIVITY_TO = 1;

	/* TODO: move this to Constants.java */
	private static final String EMAIL_ADDRESS = "sweetiepiggyapps@gmail.com";
	private static final String EMAIL_SUBJECT = "Raspberry Bus Malaysia Trip Submission";
	private static final String POST_WEBSITE = "http://raspberrybusmalaysia.appspot.com/submit_trip";

	private class DataWrapper
	{
		public Calendar sched_time;
		public Calendar depart_time;
		public Calendar arrival_time;

		public DataWrapper()
		{
			sched_time = new GregorianCalendar();
			depart_time = new GregorianCalendar();
			arrival_time = new GregorianCalendar();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submit_trip);

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
		String depart_time = format_time(mData.depart_time);
		String arrival_time = format_time(mData.arrival_time);
		String agent = ((AutoCompleteTextView) findViewById(R.id.agent_entry)).getText().toString();
		String operator = ((AutoCompleteTextView) findViewById(R.id.operator_entry)).getText().toString();
		String from_city = ((AutoCompleteTextView) findViewById(R.id.from_city_entry)).getText().toString();
		String from_station = ((AutoCompleteTextView) findViewById(R.id.from_station_entry)).getText().toString();
		String to_city = ((AutoCompleteTextView) findViewById(R.id.to_city_entry)).getText().toString();
		String to_station = ((AutoCompleteTextView) findViewById(R.id.to_station_entry)).getText().toString();
		int safety = (int) ((RatingBar) findViewById(R.id.safety_bar)).getRating();
		int comfort = (int) ((RatingBar) findViewById(R.id.comfort_bar)).getRating();
		int overall = (int) ((RatingBar) findViewById(R.id.overall_bar)).getRating();
		String comment = ((EditText) findViewById(R.id.comment_entry)).getText().toString();

		if (mDbHelper != null) {
			mDbHelper.save_tmp(agent, operator, from_city,
					from_station, to_city, to_station, sched_time,
					depart_time, arrival_time, safety,
					comfort, overall, comment);
			mDbHelper.close();
		}
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putInt("sched_year", mData.sched_time.get(Calendar.YEAR));
		savedInstanceState.putInt("sched_month", mData.sched_time.get(Calendar.MONTH));
		savedInstanceState.putInt("sched_day", mData.sched_time.get(Calendar.DAY_OF_MONTH));
		savedInstanceState.putInt("sched_hour", mData.sched_time.get(Calendar.HOUR_OF_DAY));
		savedInstanceState.putInt("sched_minute", mData.sched_time.get(Calendar.MINUTE));

		savedInstanceState.putInt("depart_year", mData.depart_time.get(Calendar.YEAR));
		savedInstanceState.putInt("depart_month", mData.depart_time.get(Calendar.MONTH));
		savedInstanceState.putInt("depart_day", mData.depart_time.get(Calendar.DAY_OF_MONTH));
		savedInstanceState.putInt("depart_hour", mData.depart_time.get(Calendar.HOUR_OF_DAY));
		savedInstanceState.putInt("depart_minute", mData.depart_time.get(Calendar.MINUTE));

		savedInstanceState.putInt("arrival_year", mData.arrival_time.get(Calendar.YEAR));
		savedInstanceState.putInt("arrival_month", mData.arrival_time.get(Calendar.MONTH));
		savedInstanceState.putInt("arrival_day", mData.arrival_time.get(Calendar.DAY_OF_MONTH));
		savedInstanceState.putInt("arrival_hour", mData.arrival_time.get(Calendar.HOUR_OF_DAY));
		savedInstanceState.putInt("arrival_minute", mData.arrival_time.get(Calendar.MINUTE));

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
		int year = savedInstanceState.getInt("sched_year");
		int month = savedInstanceState.getInt("sched_month");
		int day = savedInstanceState.getInt("sched_day");
		int hour = savedInstanceState.getInt("sched_hour");
		int minute = savedInstanceState.getInt("sched_minute");
		mData.sched_time.set(year, month, day, hour, minute);

		year = savedInstanceState.getInt("depart_year");
		month = savedInstanceState.getInt("depart_month");
		day = savedInstanceState.getInt("depart_day");
		hour = savedInstanceState.getInt("depart_hour");
		minute = savedInstanceState.getInt("depart_minute");
		mData.depart_time.set(year, month, day, hour, minute);

		year = savedInstanceState.getInt("arrival_year");
		month = savedInstanceState.getInt("arrival_month");
		day = savedInstanceState.getInt("arrival_day");
		hour = savedInstanceState.getInt("arrival_hour");
		minute = savedInstanceState.getInt("arrival_minute");
		mData.arrival_time.set(year, month, day, hour, minute);
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return mData;
	}

	private void init_date_time_buttons()
	{
		init_date_button(R.id.sched_date_button, SCHED_DATE_DIALOG_ID);
		init_date_button(R.id.depart_date_button, DEPART_DATE_DIALOG_ID);
		init_date_button(R.id.arrival_date_button, ARRIVAL_DATE_DIALOG_ID);

		init_time_button(R.id.sched_time_button, SCHED_TIME_DIALOG_ID);
		init_time_button(R.id.depart_time_button, DEPART_TIME_DIALOG_ID);
		init_time_button(R.id.arrival_time_button, ARRIVAL_TIME_DIALOG_ID);

		init_now_buttons();

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
		Button time_button = (Button) findViewById(button_id);
		time_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(dialog_id);
			}
		});
	}

	private void init_now_buttons()
	{
		Button sched_now_button = (Button) findViewById(R.id.sched_now_button);
		sched_now_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mData.sched_time = new GregorianCalendar();

				update_date_label(R.id.sched_date_button, mData.sched_time);
				update_time_label(R.id.sched_time_button, mData.sched_time);
			}
		});

		Button depart_now_button = (Button) findViewById(R.id.depart_now_button);
		depart_now_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mData.depart_time = new GregorianCalendar();

				update_date_label(R.id.depart_date_button, mData.depart_time);
				update_time_label(R.id.depart_time_button, mData.depart_time);
			}
		});

		Button arrival_now_button = (Button) findViewById(R.id.arrival_now_button);
		arrival_now_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mData.arrival_time = new GregorianCalendar();

				update_date_label(R.id.arrival_date_button, mData.arrival_time);
				update_time_label(R.id.arrival_time_button, mData.arrival_time);
			}
		});
	}

	private void init_vars(DataWrapper data)
	{
		Cursor c_sched_time = mDbHelper.fetch_tmp_sched_time();
		init_time(c_sched_time, data.sched_time);

		Cursor c_depart_time = mDbHelper.fetch_tmp_depart_time();
		init_time(c_depart_time, data.depart_time);

		Cursor c_arrival_time = mDbHelper.fetch_tmp_arrival_time();
		init_time(c_arrival_time, data.arrival_time);
	}

	private void init_time(Cursor c, Calendar cal)
	{
		/* restore time from database */
		if (c.moveToFirst()) {
			int year = Integer.parseInt(c.getString(1));
			int month = Integer.parseInt(c.getString(2)) - 1;
			int day = Integer.parseInt(c.getString(3));
			int hour = Integer.parseInt(c.getString(4));
			int minute = Integer.parseInt(c.getString(5));

			cal.set(year, month, day, hour, minute);
		/* use current time */
		} else {
			cal = new GregorianCalendar();
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

		String from_city = mDbHelper.fetch_tmp(DbAdapter.KEY_FROM_CITY);
		String from_station = mDbHelper.fetch_tmp(DbAdapter.KEY_FROM_STN);
		String to_city = mDbHelper.fetch_tmp(DbAdapter.KEY_TO_CITY);
		String to_station = mDbHelper.fetch_tmp(DbAdapter.KEY_TO_STN);
		String agent = mDbHelper.fetch_tmp(DbAdapter.KEY_AGENT);
		String operator = mDbHelper.fetch_tmp(DbAdapter.KEY_OPERATOR);
		String comment = mDbHelper.fetch_tmp(DbAdapter.KEY_COMMENT);
		int safety = mDbHelper.fetch_safety();
		int comfort = mDbHelper.fetch_comfort();
		int overall = mDbHelper.fetch_overall();

		((AutoCompleteTextView) findViewById(R.id.from_city_entry)).setText(from_city);
		((AutoCompleteTextView) findViewById(R.id.from_station_entry)).setText(from_station);
		((AutoCompleteTextView) findViewById(R.id.to_city_entry)).setText(to_city);
		((AutoCompleteTextView) findViewById(R.id.to_station_entry)).setText(to_station);
		((AutoCompleteTextView) findViewById(R.id.agent_entry)).setText(agent);
		((AutoCompleteTextView) findViewById(R.id.operator_entry)).setText(operator);

		((RatingBar) findViewById(R.id.safety_bar)).setRating(safety);
		((RatingBar) findViewById(R.id.comfort_bar)).setRating(comfort);
		((RatingBar) findViewById(R.id.overall_bar)).setRating(overall);
		((EditText) findViewById(R.id.comment_entry)).setText(comment);
	}

	private void update_city_autocomplete(int id)
	{
		ArrayAdapter<String> cities = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_cities();
		if (c.moveToFirst()) do {
			cities.add(c.getString(c.getColumnIndex(DbAdapter.KEY_CITY)));
		} while (c.moveToNext());
		c.close();
		AutoCompleteTextView cities_entry = (AutoCompleteTextView) findViewById(id);
		cities_entry.setThreshold(2);
		cities_entry.setAdapter(cities);
	}

	private void update_station_autocomplete(int id)
	{
		ArrayAdapter<String> stations = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_stations();
		if (c.moveToFirst()) do {
			stations.add(c.getString(c.getColumnIndex(DbAdapter.KEY_STN)));
		} while (c.moveToNext());
		c.close();
		AutoCompleteTextView stations_entry = (AutoCompleteTextView) findViewById(id);
		stations_entry.setThreshold(1);
		stations_entry.setAdapter(stations);
	}

	private void update_agent_autocomplete(int id)
	{
		ArrayAdapter<String> agents = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_agents();
		if (c.moveToFirst()) do {
			agents.add(c.getString(c.getColumnIndex(DbAdapter.KEY_AGENT)));
		} while (c.moveToNext());
		c.close();
		AutoCompleteTextView agents_entry = (AutoCompleteTextView) findViewById(id);
		agents_entry.setThreshold(1);
		agents_entry.setAdapter(agents);
	}

	private void update_operator_autocomplete(int id)
	{
		ArrayAdapter<String> operators = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_operators();
		if (c.moveToFirst()) do {
			operators.add(c.getString(c.getColumnIndex(DbAdapter.KEY_OPERATOR)));
		} while (c.moveToNext());
		c.close();
		AutoCompleteTextView operators_entry = (AutoCompleteTextView) findViewById(id);
		operators_entry.setThreshold(1);
		operators_entry.setAdapter(operators);
	}

	private void update_date_label(int button_id, Calendar cal)
	{
		Button date_button = (Button) findViewById(button_id);

		String date = translate_day_of_week(DateFormat.format("EEEE", cal.getTime()).toString()) +
			" " + DateFormat.getLongDateFormat(getApplicationContext()).format(cal.getTime());
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

	private void update_time_label(int button_id, Calendar cal)
	{
		Button time_button = (Button)findViewById(button_id);
		String time = DateFormat.getTimeFormat(getApplicationContext()).format(cal.getTime());
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
				} else if (!mData.arrival_time.after(mData.depart_time)) {
					results_complete = false;
					incomplete_msg = getResources().getString(R.string.depart_before_arrival);
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
				mDbHelper.clear_tmp_table();
				init_vars(mData);
				/* TODO: why aren't these set correctly by init_vars() ? */
				mData.sched_time = new GregorianCalendar();
				mData.depart_time = new GregorianCalendar();
				mData.arrival_time = new GregorianCalendar();
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
		String agent = ((AutoCompleteTextView) findViewById(R.id.agent_entry)).getText().toString();
		String operator = ((AutoCompleteTextView) findViewById(R.id.operator_entry)).getText().toString();
		String from_city = ((AutoCompleteTextView) findViewById(R.id.from_city_entry)).getText().toString();
		String from_station = ((AutoCompleteTextView) findViewById(R.id.from_station_entry)).getText().toString();
		String to_city = ((AutoCompleteTextView) findViewById(R.id.to_city_entry)).getText().toString();
		String to_station = ((AutoCompleteTextView) findViewById(R.id.to_station_entry)).getText().toString();
		String safety = Integer.toString((int) ((RatingBar) findViewById(R.id.safety_bar)).getRating());
		String comfort = Integer.toString((int) ((RatingBar) findViewById(R.id.comfort_bar)).getRating());
		String overall = Integer.toString((int) ((RatingBar) findViewById(R.id.overall_bar)).getRating());
		String comment = ((EditText) findViewById(R.id.comment_entry)).getText().toString();

		String msg = format_email(agent, operator, from_city,
			from_station, to_city, to_station, sched_time,
			depart_time, arrival_time, safety, comfort, overall,
			comment);

		new PostTask(this, msg).execute();
	}

	private void send_email(String msg)
	{
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {EMAIL_ADDRESS} );
		intent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
		intent.putExtra(Intent.EXTRA_TEXT, msg);
		intent.setType("text/plain");
		startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
	}

	private class PostTask extends AsyncTask<Void, Void, Void>
	{
		private Context mCtx;
		private String mMsg;
		private int mStatusCode = 0;

		public PostTask(Context ctx, String msg)
		{
			mCtx = ctx;
			mMsg = msg;
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			HttpClient hc = new DefaultHttpClient();
			HttpPost hp = new HttpPost(POST_WEBSITE);
			List<NameValuePair> l = new ArrayList<NameValuePair>(2);
			l.add(new BasicNameValuePair("msg", mMsg));

			try {
				hp.setEntity(new UrlEncodedFormEntity(l, "UTF-8"));
				HttpResponse response = hc.execute(hp);
				mStatusCode = response.getStatusLine().getStatusCode();

			} catch (ClientProtocolException e) {
				throw new Error(e);
			} catch (UnsupportedEncodingException e) {
				throw new Error(e);
			/* probably no internet connection? */
			} catch (IOException e) {
				mStatusCode = 1;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			if (200 <= mStatusCode && mStatusCode < 300) {
				Toast.makeText(mCtx,
						mCtx.getResources().getString(R.string.submit_trip_success),
						Toast.LENGTH_SHORT).show();
			} else {
				send_email(mMsg);
			}
		}
	}

	private String format_email(String agent, String operator,
			String from_city, String from_station, String to_city,
			String to_station, String scheduled_departure,
			String actual_departure, String arrival_time,
			String safety, String comfort, String overall,
			String comment)
	{
		return "Agent: " + agent + "\n" +
			"Operator: " + operator + "\n" +
			"From city: " + from_city + "\n" +
			"From station: " + from_station + "\n" +
			"From city: " + to_city + "\n" +
			"To station: " + to_station + "\n" +
			"Scheduled departure: " + scheduled_departure + "\n" +
			"Actual departure: " + actual_departure + "\n" +
			"Arrival time: " + arrival_time + "\n" +
			"Safety: " + safety + "\n" +
			"Comfort: " + comfort + "\n" +
			"Overall: " + overall + "\n" +
			"Comment: " + comment + "\n";
	}

	private String format_time(Calendar cal)
	{
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(cal.getTime());
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		DatePickerDialog.OnDateSetListener sched_date_listener =
			new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					mData.sched_time.set(year, monthOfYear, dayOfMonth);
					update_date_label(R.id.sched_date_button,
							mData.sched_time);
				}
		};

		TimePickerDialog.OnTimeSetListener sched_time_listener =
			new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view,
						int hourOfDay, int minute) {
					int year =  mData.sched_time.get(Calendar.YEAR);
					int month =  mData.sched_time.get(Calendar.MONTH);
					int day =  mData.sched_time.get(Calendar.DAY_OF_MONTH);
					mData.sched_time.set(year, month, day, hourOfDay, minute);
					update_time_label(R.id.sched_time_button,
							mData.sched_time);
				}
		};

		DatePickerDialog.OnDateSetListener depart_date_listener =
			new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					mData.depart_time.set(year, monthOfYear, dayOfMonth);
					update_date_label(R.id.depart_date_button,
							mData.depart_time);
				}
		};

		TimePickerDialog.OnTimeSetListener depart_time_listener =
			new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view,
						int hourOfDay, int minute) {
					int year =  mData.depart_time.get(Calendar.YEAR);
					int month =  mData.depart_time.get(Calendar.MONTH);
					int day =  mData.depart_time.get(Calendar.DAY_OF_MONTH);
					mData.depart_time.set(year, month, day, hourOfDay, minute);
					update_time_label(R.id.depart_time_button,
							mData.depart_time);
				}
		};
		DatePickerDialog.OnDateSetListener arrival_date_listener =
			new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					mData.arrival_time.set(year, monthOfYear, dayOfMonth);
					update_date_label(R.id.arrival_date_button,
							mData.arrival_time);
				}
		};

		TimePickerDialog.OnTimeSetListener arrival_time_listener =
			new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view,
						int hourOfDay, int minute) {
					int year =  mData.arrival_time.get(Calendar.YEAR);
					int month =  mData.arrival_time.get(Calendar.MONTH);
					int day =  mData.arrival_time.get(Calendar.DAY_OF_MONTH);
					mData.arrival_time.set(year, month, day, hourOfDay, minute);
					update_time_label(R.id.arrival_time_button,
							mData.arrival_time);
				}
		};

		switch (id) {
		case SCHED_DATE_DIALOG_ID:
			return new DatePickerDialog(this, sched_date_listener,
					mData.sched_time.get(Calendar.YEAR),
					mData.sched_time.get(Calendar.MONTH),
					mData.sched_time.get(Calendar.DAY_OF_MONTH));
		case SCHED_TIME_DIALOG_ID:
			return new TimePickerDialog(this, sched_time_listener,
					mData.sched_time.get(Calendar.HOUR_OF_DAY),
					mData.sched_time.get(Calendar.MINUTE),
					false);
		case DEPART_DATE_DIALOG_ID:
			return new DatePickerDialog(this, depart_date_listener,
					mData.depart_time.get(Calendar.YEAR),
					mData.depart_time.get(Calendar.MONTH),
					mData.depart_time.get(Calendar.DAY_OF_MONTH));
		case DEPART_TIME_DIALOG_ID:
			return new TimePickerDialog(this, depart_time_listener,
					mData.depart_time.get(Calendar.HOUR_OF_DAY),
					mData.depart_time.get(Calendar.MINUTE),
					false);
		case ARRIVAL_DATE_DIALOG_ID:
			return new DatePickerDialog(this, arrival_date_listener,
					mData.arrival_time.get(Calendar.YEAR),
					mData.arrival_time.get(Calendar.MONTH),
					mData.arrival_time.get(Calendar.DAY_OF_MONTH));
		case ARRIVAL_TIME_DIALOG_ID:
			return new TimePickerDialog(this, arrival_time_listener,
					mData.arrival_time.get(Calendar.HOUR_OF_DAY),
					mData.arrival_time.get(Calendar.MINUTE),
					false);
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

