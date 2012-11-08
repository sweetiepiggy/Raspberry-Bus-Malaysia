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
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class RaspberryBusMalaysiaActivity extends Activity
{
	private static final String SOURCE_URL = "https://github.com/sweetiepiggy/Raspberry-Bus-Malaysia";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		int content_view = getResources().getConfiguration().orientation ==
			Configuration.ORIENTATION_LANDSCAPE ?
			R.layout.main_landscape : R.layout.main;
		setContentView(content_view);

		TextView route = (TextView) findViewById(R.id.route);
		route.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
				startActivity(intent);
			}
		});

		TextView agent = (TextView) findViewById(R.id.agent);
		agent.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), CompanyActivity.class);
				startActivity(intent);
			}
		});

		TextView operator = (TextView) findViewById(R.id.operator);
		operator.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), CompanyActivity.class);
				Bundle b = new Bundle();
				b.putBoolean("is_operator", true);
				intent.putExtras(b);
				startActivity(intent);
			}
		});

		TextView submit_trip = (TextView) findViewById(R.id.submit_trip);
		submit_trip.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), SubmitTripActivity.class);
				startActivity(intent);
			}
		});

		TextView map = (TextView) findViewById(R.id.map);
		map.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), RbmMapActivity.class);
				Bundle b = new Bundle();
				b.putBoolean("draw_routes", true);
				intent.putExtras(b);
				startActivity(intent);
			}
		});

		TextView complain = (TextView) findViewById(R.id.complain);
		complain.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), ComplainActivity.class);
				startActivity(intent);
			}
		});

		/* open database only to sync if it has not been created yet */
		DbAdapter dbHelper = new DbAdapter();
		dbHelper.open_readwrite(this);
		dbHelper.check_last_update_and_sync();
		dbHelper.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.about:
			intent = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(intent);
			return true;
		case R.id.sync:
			SyncTask sync = new SyncTask(this);
			sync.execute();
			return true;
		case R.id.source:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(SOURCE_URL), "text/html");
			startActivity(Intent.createChooser(intent, getResources().getString(R.string.open_browser)));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

