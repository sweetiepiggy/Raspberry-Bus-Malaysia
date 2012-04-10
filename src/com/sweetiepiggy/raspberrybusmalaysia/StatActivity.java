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
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class StatActivity extends Activity {
	private DbAdapter mDbHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stat);
		mDbHelper = new DbAdapter(this);
		mDbHelper.open();
		init_data();
		fill_data();
	}

	private void init_data() {
		mDbHelper.clear();
		mDbHelper.create_trip("Kuala Lumpur", "Penang");
		mDbHelper.create_trip("Penang", "Kuala Lumpur");
	}

	private void fill_data() {
		Cursor c = mDbHelper.fetch_from_cities();
		startManagingCursor(c);
		SimpleCursorAdapter from_cities = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_item,
				c, new String[] {DbAdapter.KEY_FROM_CITY},
				new int[] {android.R.id.text1});
		Spinner from_spinner = (Spinner) findViewById(R.id.from_spinner);
		from_spinner.setAdapter(from_cities);

		from_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent,
					View selected_item, int pos,
					long id) {
				String from_city = ((Cursor)parent.getItemAtPosition(pos)).getString(1);
				Cursor c = mDbHelper.fetch_to_cities(from_city);
				//Toast.makeText(getApplicationContext(), Integer.toString(c.getCount()), Toast.LENGTH_SHORT).show();
				startManagingCursor(c);
				SimpleCursorAdapter to_cities = new SimpleCursorAdapter(getApplicationContext(),
						android.R.layout.simple_spinner_item,
						c, new String[] {DbAdapter.KEY_TO_CITY},
						new int[] {android.R.id.text1});
				Spinner to_spinner = (Spinner) findViewById(R.id.to_spinner);
				to_spinner.setAdapter(to_cities);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}

		});
	}
}

