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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

public class ComplainActivity extends Activity
{

	private DataWrapper mData;
	private DbAdapter mDbHelper;

	private boolean m_youtube_sent;
	private boolean m_email_sent;
	private boolean m_tweet_sent;
	private boolean m_sms_sent;

	private static final int SCHED_DATE_DIALOG_ID = 0;
	private static final int SCHED_TIME_DIALOG_ID = 1;

	private static final int ACTIVITY_FROM = 0;
	private static final int ACTIVITY_TO = 1;
	private static final int ACTIVITY_SUBMIT = 2;
	private static final int ACTIVITY_TAKE_PHOTO = 3;
	private static final int ACTIVITY_RECORD_SOUND = 4;
	private static final int ACTIVITY_TAKE_VIDEO = 5;

	/* TODO: move this to Constants.java */
	private static final String EMAIL_SUBJECT = "Aduan Bas Ekspres";
	private static final String TWITTER_ADDRESS1 = "@aduanSPAD";
	private static final String TWITTER_ADDRESS2 = "@transitmy";
	private static final String TWITTER_ADDRESS3 = "@myAduan";
	private static final String SMS_NUMBER = "15888";
	private static final int MAX_TWEET_LENGTH = 140;

	static final String[] EMAIL_ADDRESSES = {
		"aduan@spad.gov.my; ",
		"aduantrafik@jpj.gov.my; e-aduan@kpdnkk.gov.my; info@motour.gov.my; bahria@miti.gov.my; unitpro@pcb.gov.my; ",
		"klangvalley.transit@gmail.com; nccc@nccc.org.my; ",
		"menteri@mot.gov.my; yenyenng@motour.gov.my; najib@1malaysia.com.my; ",
		"editor@thestar.com.my; metro@thestar.com.my; mmnews@mmail.com.my; syedn@nst.com.my; letters@nst.com.my; streets@nst.com.my; letters@thesundaily.com, editor@malaysiakini.com.my; editor@themalaysianinsider.com; ",
		"rmp@rmp.gov.my; "
	};

	private class DataWrapper
	{
		public Calendar sched_time;
		public boolean[] who_selected;
		public boolean[] submit_selected;
		public ArrayList<Uri> photo_uris;
		public ArrayList<Uri> recording_uris;
		public ArrayList<Uri> video_uris;

		public DataWrapper()
		{
			sched_time = new GregorianCalendar();
		}
	}

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
				init_selected(mData);
				init_entries();
			}
		} else {
			mData = new DataWrapper();
			restore_saved_state(savedInstanceState);
		}

		init_date_time_buttons();
		init_map_buttons();
		init_camera_recorder_buttons();
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
		savedInstanceState.putInt("sched_year", mData.sched_time.get(Calendar.YEAR));
		savedInstanceState.putInt("sched_month", mData.sched_time.get(Calendar.MONTH));
		savedInstanceState.putInt("sched_day", mData.sched_time.get(Calendar.DAY_OF_MONTH));
		savedInstanceState.putInt("sched_hour", mData.sched_time.get(Calendar.HOUR_OF_DAY));
		savedInstanceState.putInt("sched_minute", mData.sched_time.get(Calendar.MINUTE));
		savedInstanceState.putBooleanArray("who_selected", mData.who_selected);
		savedInstanceState.putBooleanArray("submit_selected", mData.submit_selected);
		savedInstanceState.putStringArrayList("photo_uris", uriarr2strarr(mData.photo_uris));
		savedInstanceState.putStringArrayList("recording_uris", uriarr2strarr(mData.recording_uris));
		savedInstanceState.putStringArrayList("video_uris", uriarr2strarr(mData.video_uris));

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

		mData.who_selected = savedInstanceState.getBooleanArray("who_selected");
		mData.submit_selected = savedInstanceState.getBooleanArray("submit_selected");

		mData.photo_uris = strarr2uriarr(savedInstanceState.getStringArrayList("photo_uris"));
		mData.recording_uris = strarr2uriarr(savedInstanceState.getStringArrayList("recording_uris"));
		mData.video_uris = strarr2uriarr(savedInstanceState.getStringArrayList("video_uris"));
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

		data.photo_uris = new ArrayList<Uri>();
		data.recording_uris = new ArrayList<Uri>();
		data.video_uris = new ArrayList<Uri>();
	}

	private void init_selected(DataWrapper data)
	{
		/* TODO: selected defaults should not be hard coded here */
		data.who_selected = new boolean[] {true, true, true, false, false, false};
		data.submit_selected = new boolean[] {false, true, false, false};
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

		String photo_size = mData.photo_uris.size() > 0 ? Integer.toString(mData.photo_uris.size()) : "";
		String video_size = mData.video_uris.size() > 0 ? Integer.toString(mData.video_uris.size()) : "";
		String recording_size = mData.recording_uris.size() > 0 ? Integer.toString(mData.recording_uris.size()) : "";

		((TextView) findViewById(R.id.camera_label)).setText(photo_size);
		((TextView) findViewById(R.id.recorder_label)).setText(recording_size);
		((TextView) findViewById(R.id.video_label)).setText(video_size);
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

	private void update_counter_num_autocomplete(int id)
	{
		ArrayAdapter<String> counter_nums = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		Cursor c = mDbHelper.fetch_counter_nums();
		if (c.moveToFirst()) do {
			counter_nums.add(c.getString(c.getColumnIndex(DbAdapter.KEY_CTR)));
		} while (c.moveToNext());
		c.close();
		AutoCompleteTextView counter_nums_entry = (AutoCompleteTextView) findViewById(id);
		counter_nums_entry.setThreshold(1);
		counter_nums_entry.setAdapter(counter_nums);
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
				submit_menu();
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

	private void init_camera_recorder_buttons()
	{
		Button camera_button = (Button) findViewById(R.id.camera_button);
		Button vidcam_button = (Button) findViewById(R.id.vidcam_button);
		Button recorder_button = (Button) findViewById(R.id.recorder_button);

		//boolean has_camera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
		//boolean has_microphone = getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
		boolean has_camera = true;
		boolean has_microphone = true;

		if (has_camera) {
			camera_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent photo_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(Intent.createChooser(photo_intent,
							getResources().getString(R.string.take_photo)),
						ACTIVITY_TAKE_PHOTO);
				}
			});

			vidcam_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent video_intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					startActivityForResult(Intent.createChooser(video_intent,
							getResources().getString(R.string.record_video)),
						ACTIVITY_TAKE_VIDEO);
				}
			});
		} else {
			camera_button.setVisibility(View.GONE);
			vidcam_button.setVisibility(View.GONE);
		}

		if (has_microphone) {
			recorder_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent recorder_intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
					startActivityForResult(Intent.createChooser(recorder_intent,
							getResources().getString(R.string.record_sound)),
						ACTIVITY_RECORD_SOUND);
				}
			});
		} else {
			recorder_button.setVisibility(View.GONE);
		}

		if (!has_camera && !has_microphone) {
			((TextView) findViewById(R.id.record_label)).setVisibility(View.GONE);
		}
	}

	private void init_cancel_button()
	{
		Button cancel_button = (Button)findViewById(R.id.cancel_button);
		cancel_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDbHelper.clear_tmp_complaint_table();
				init_vars(mData);
				/* TODO: why is this not set correctly by init_vars() ? */
				mData.sched_time = new GregorianCalendar();
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

		String msg = format_msg(agent, operator, from_city,
				from_station, to_city, to_station,
				sched_time, counter_num, comment, reg);

		/* TODO: order of index shouldn't be hard coded like this */
		boolean sms_checked = mData.submit_selected[0];
		boolean email_checked = mData.submit_selected[1];
		boolean tweet_checked = mData.submit_selected[2];
		boolean youtube_checked = mData.submit_selected[3];

		/* send one at a time, repeated call submit()
			until all checked are sent */
		if (sms_checked && !m_sms_sent) {
			send_sms(msg);
		} else if (email_checked && !m_email_sent) {
			send_email(msg, reg);
		} else if (tweet_checked && !m_tweet_sent) {
			send_tweet(agent, operator, from_city, from_station,
					to_city, to_station, sched_time,
					comment, reg);
		} else if (youtube_checked && !m_youtube_sent) {
			send_youtube(msg);
		}
	}

	private void submit_menu()
	{
		final String[] submit_choices = new String[] {
			getResources().getString(R.string.sms),
			getResources().getString(R.string.email),
			getResources().getString(R.string.tweet),
			getResources().getString(R.string.youtube),
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_submit);
		builder.setMultiChoiceItems(submit_choices,
				mData.submit_selected, new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean is_checked) {
				mData.submit_selected[which] = is_checked;
			}
		});
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				m_youtube_sent = false;
				m_email_sent = false;
				m_tweet_sent = false;
				m_sms_sent = false;
				submit();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		AlertDialog alert = builder.create();
		ListView list = alert.getListView();
		for (int i=0; i < mData.submit_selected.length; ++i) {
			list.setItemChecked(i, mData.submit_selected[i]);
		}

		alert.show();
	}

	private void send_sms(String msg)
	{
		String sms_msg = EMAIL_SUBJECT + " " + msg;
		Intent intent = new Intent(Intent.ACTION_VIEW);

		intent.putExtra("address",
				SMS_NUMBER);
		intent.putExtra("sms_body", sms_msg);
		/* TODO: attach files to mms */
		intent.setType("vnd.android-dir/mms-sms");
		m_sms_sent = true;
		startActivityForResult(intent, ACTIVITY_SUBMIT);
	}

	private void send_youtube(String msg)
	{
		String action = mData.video_uris.size() > 1 ?
			Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_SEND;
		Intent youtube_intent = new Intent(action);
		youtube_intent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
		youtube_intent.putExtra(Intent.EXTRA_TEXT, msg);
		youtube_intent.setType("video/*");

		if (mData.video_uris.size() == 1) {
			youtube_intent.putExtra(Intent.EXTRA_STREAM,
					mData.video_uris.get(mData.video_uris.size()-1));
		} else if (mData.video_uris.size() > 1) {
			youtube_intent.putExtra(Intent.EXTRA_STREAM, mData.video_uris);
		}

		m_youtube_sent = true;
		startActivityForResult(Intent.createChooser(youtube_intent,
					getResources().getString(R.string.send_youtube)),
				ACTIVITY_SUBMIT);
	}

	private void send_email(final String msg, final String reg)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.who_email);
		builder.setMultiChoiceItems(R.array.email_choices,
				mData.who_selected, new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean is_checked) {
				mData.who_selected[which] = is_checked;
			}
		});
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String email_msg = getResources().getString(R.string.email_intro) + "\n" + msg;
				String email_addresses = "";

				/* TODO: who_selected and EMAIL_ADDRESSES need to be better linked,
					possible problem if their lengths are not equal */
				for (int i=0; i < mData.who_selected.length; ++i) {
					if (mData.who_selected[i]) {
						email_addresses += EMAIL_ADDRESSES[i];
					}
				}

				String subj = EMAIL_SUBJECT;
				if (reg.length() != 0) {
					subj += ' ' + reg;
				}

				Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

				ArrayList<Uri> uris = new ArrayList<Uri>();
				uris.addAll(mData.photo_uris);
				uris.addAll(mData.recording_uris);
				uris.addAll(mData.video_uris);

				if (uris.size() > 0) {
					intent.putExtra(Intent.EXTRA_STREAM, uris);
				}

				intent.putExtra(Intent.EXTRA_EMAIL, new String[] {email_addresses} );
				intent.putExtra(Intent.EXTRA_SUBJECT, subj);
				intent.putExtra(Intent.EXTRA_TEXT, email_msg);
				intent.setType("text/plain");
				startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
			}
		});

		builder.setNeutralButton(R.string.who_details, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(getApplicationContext(), TextViewActivity.class);
				Bundle b = new Bundle();
				b.putString("text", getResources().getString(R.string.email_details));
				intent.putExtras(b);
				startActivityForResult(intent, ACTIVITY_SUBMIT);
			}
		});

		builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				m_email_sent = true;
				submit();
			}
		});

		AlertDialog alert = builder.create();
		ListView list = alert.getListView();
		for (int i=0; i < mData.who_selected.length; ++i) {
			list.setItemChecked(i, mData.who_selected[i]);
		}

		alert.show();
	}

	private void send_tweet(String agent, String operator,
			String from_city, String from_station, String to_city,
			String to_station, String sched_time,
			String comment, String reg)
	{
		String msg = format_tweet(agent, operator, from_city,
				from_station, to_city, to_station,
				sched_time, comment, reg);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, msg);
		if (!mData.photo_uris.isEmpty()) {
			intent.putExtra(Intent.EXTRA_STREAM,
					mData.photo_uris.get(mData.photo_uris.size()-1));
			intent.setType("image/*");
		} else {
			intent.setType("text/plain");
		}

		m_tweet_sent = true;
		startActivityForResult(Intent.createChooser(intent,
					getResources().getString(R.string.send_tweet)),
				ACTIVITY_SUBMIT);
	}

	private String format_tweet(String agent, String operator,
			String from_city, String from_station, String to_city,
			String to_station, String sched_time,
			String comment, String reg)
	{
		/** map to keep track of which info should be printed and
			which should be dropped to keep tweet under 140 characters */
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		map.put("twitter_address1", true);
		map.put("twitter_address2", false);
		map.put("twitter_address3", false);
		map.put("sched_time", false);
		map.put("reg", reg.length() != 0);
		map.put("operator",false);
		map.put("agent", false);
		map.put("from_city", false);
		map.put("from_station", false);
		map.put("to_city", false);
		map.put("to_station", false);

		int max_length = MAX_TWEET_LENGTH;
		if (mData.photo_uris.size() > 0) {
			max_length -= 21;
		}

		map.put("comment", true);
		if (comment.length() == 0 || build_tweet(map, operator, agent,
					from_city, from_station, to_city,
					to_station, sched_time, comment,
					reg).length() >
				max_length) {
			map.put("comment", false);
		}

		map.put("operator", true);
		if (comment.length() == 0 || build_tweet(map, operator, agent,
					from_city, from_station, to_city,
					to_station, sched_time, comment,
					reg).length() >
				max_length) {
			map.put("operator", false);
		}

		map.put("from_station", true);
		if (from_station.length() == 0 || build_tweet(map, operator,
					agent, from_city, from_station,
					to_city, to_station, sched_time,
					comment, reg).length() >
				max_length) {
			map.put("from_station", false);
		}

		map.put("to_station", true);
		if (to_station.length() == 0 || build_tweet(map, operator,
					agent, from_city, from_station,
					to_city, to_station, sched_time,
					comment, reg).length() >
				max_length) {
			map.put("to_station", false);
		}

		map.put("from_city", true);
		if (from_city.length() == 0 || build_tweet(map, operator,
					agent, from_city, from_station,
					to_city, to_station, sched_time,
					comment, reg).length() >
				max_length) {
			map.put("from_city", false);
		}

		map.put("to_city", true);
		if (to_city.length() == 0 || build_tweet(map, operator,
					agent, from_city, from_station,
					to_city, to_station, sched_time,
					comment, reg).length() >
				max_length) {
			map.put("to_city", false);
		}

		map.put("twitter_address2", true);
		if (build_tweet(map, operator, agent, from_city, from_station,
					to_city, to_station, sched_time,
					comment, reg).length() >
				max_length) {
			map.put("twitter_address2", false);
		}

		map.put("sched_time", true);
		if (sched_time.length() == 0 || build_tweet(map, operator,
					agent, from_city, from_station,
					to_city, to_station, sched_time,
					comment, reg).length() >
				max_length) {
			map.put("sched_time", false);
		}

		map.put("agent", true);
		if (comment.length() == 0 || build_tweet(map, operator,
					agent, from_city, from_station,
					to_city, to_station, sched_time,
					comment, reg).length() >
				max_length) {
			map.put("agent", false);
		}

		map.put("twitter_address3", true);
		if (build_tweet(map, operator, agent, from_city, from_station,
					to_city, to_station, sched_time,
					comment, reg).length() >
				max_length) {
			map.put("twitter_address3", false);
		}

		/* always include additional details, but wait until here to
			set true to avoid cutting down other fields if user
			description won't fit anyway */
		if (comment.length() != 0) {
			map.put("comment", true);
		}

		return build_tweet(map, operator, agent, from_city,
				from_station, to_city, to_station, sched_time,
				comment, reg);
	}

	private String build_tweet(HashMap<String, Boolean> map, String operator,
			String agent, String from_city, String from_station,
			String to_city, String to_station, String sched_time,
			String comment, String reg)
	{
		String ret = "";

		if (map.get("twitter_address1")) {
			ret += TWITTER_ADDRESS1;
		}

		if (map.get("sched_time")) {
			if (ret.length() != 0) {
				ret += ' ';
			}
			ret += sched_time;
		}

		if (map.get("reg")) {
			if (ret.length() != 0) {
				ret += ' ';
			}
			ret += reg;
		}

		if (map.get("operator")) {
			if (ret.length() != 0) {
				ret += ' ';
			}
			ret += operator;
		}

		if (map.get("agent")) {
			if (map.get("operator")) {
				ret += ", ";
			} else if (ret.length() != 0) {
				ret += ' ';
			}
			ret += agent;
		}

		if (map.get("from_city") || map.get("from_station")) {
			if (map.get("operator") || map.get("agent")) {
				ret += ", ";
			} else if (ret.length() != 0) {
				ret += ' ';
			}
			if (map.get("from_station")) {
				ret += from_station;
			}
			if (map.get("from_city") && !from_city.equals(from_station)) {
				if (map.get("from_station")) {
					ret += ", ";
				}
				ret += from_city;
			}
		}

		if (map.get("to_city") || map.get("to_station")) {
			if (ret.length() != 0) {
				ret += ' ';
			}
			if (map.get("from_station") || map.get("from_city")) {
				ret += "-> ";
			}
			if (map.get("to_station")) {
				ret += to_station;
			}
			if (map.get("to_city") && !to_city.equals(to_station)) {
				if (map.get("to_station")) {
					ret += ", ";
				}
				ret += to_city;
			}
		}

		if (map.get("comment")) {
			if (map.get("from_city") || map.get("from_station") ||
					map.get("to_city") ||
					map.get("to_station")) {
				ret += ',';
			}
			if (ret.length() != 0) {
				ret += ' ';
			}
			ret += comment;
		}

		if (map.get("twitter_address2")) {
			if (ret.length() != 0) {
				ret += ' ';
			}
			ret += TWITTER_ADDRESS2;
		}

		if (map.get("twitter_address3")) {
			if (ret.length() != 0) {
				ret += ' ';
			}
			ret += TWITTER_ADDRESS3;
		}

		return ret;
	}

	private String format_msg(String agent, String operator,
			String from_city, String from_station, String to_city,
			String to_station, String sched_time,
			String counter, String comment, String reg)
	{
		String msg = "";

		if (sched_time.length() != 0) {
			msg += '\n' + sched_time;
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

	private String format_time(Calendar cal)
	{
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(cal.getTime());
	}

	private ArrayList<Uri> strarr2uriarr(ArrayList<String> str_arr)
	{
		ArrayList<Uri> ret = new ArrayList<Uri>();
		Iterator<String> itr = str_arr.iterator();
		while (itr.hasNext()) {
			ret.add(Uri.parse(itr.next()));
		}
		return ret;
	}

	private ArrayList<String> uriarr2strarr(ArrayList<Uri> uri_arr)
	{
		ArrayList<String> ret = new ArrayList<String>();
		Iterator<Uri> itr = uri_arr.iterator();
		while (itr.hasNext()) {
			ret.add(itr.next().toString());
		}
		return ret;
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
		case ACTIVITY_TAKE_PHOTO:
			if (result_code == RESULT_OK) {
				mData.photo_uris.add(data.getData());
				((TextView)findViewById(R.id.camera_label)).setText(Integer.toString(mData.photo_uris.size()));
			}
			break;
		case ACTIVITY_TAKE_VIDEO:
			if (result_code == RESULT_OK) {
				mData.video_uris.add(data.getData());
				((TextView)findViewById(R.id.video_label)).setText(Integer.toString(mData.video_uris.size()));
			}
			break;
		case ACTIVITY_RECORD_SOUND:
			if (result_code == RESULT_OK) {
				mData.recording_uris.add(data.getData());
				((TextView)findViewById(R.id.recorder_label)).setText(Integer.toString(mData.recording_uris.size()));
			}
			break;
		case ACTIVITY_SUBMIT:
			/* repeatedly submit until all send_*() functions have been called */
			submit();
			break;
		}
	}
}

