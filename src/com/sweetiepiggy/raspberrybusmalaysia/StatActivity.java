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
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class StatActivity extends Activity
{
	private String m_from_city = "";
	private String m_to_city = "";

	DbAdapter mDbHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stat);

		mDbHelper = new DbAdapter();
		mDbHelper.open(this);
		fill_data();

		init_submit_button();
	}

//	@Override
//	protected void onDestroy()
//	{
//		super.onDestroy();
//		if (mDbHelper != null) {
//			mDbHelper.close();
//		}
//	}

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

				to_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View selected_item, int pos,
							long id) {
						m_to_city = ((Cursor)parent.getItemAtPosition(pos)).getString(1);
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

	private void init_submit_button()
	{
		Button submit_button = (Button) findViewById(R.id.submit_button);
		submit_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				if (m_from_city.length() == 0) {
					Toast.makeText(getApplicationContext(), "Select \"From City\"", Toast.LENGTH_SHORT).show();
				} else if (m_to_city.length() == 0) {
					Toast.makeText(getApplicationContext(), "Select \"To City\"", Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent(getApplicationContext(), StatResultActivity.class);
					Bundle b = new Bundle();
					b.putString("from_city", m_from_city);
					b.putString("to_city", m_to_city);
					intent.putExtras(b);
					startActivity(intent);
				}
			}
		});
	}
}

