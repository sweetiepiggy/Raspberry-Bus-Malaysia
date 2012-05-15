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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class RaspberryBusMalaysiaActivity extends Activity
{

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TextView route = (TextView) findViewById(R.id.route);
		route.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
				startActivity(intent);
			}
		});

		TextView company = (TextView) findViewById(R.id.company);
		company.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				Intent intent = new Intent(getApplicationContext(), CompanyActivity.class);
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

