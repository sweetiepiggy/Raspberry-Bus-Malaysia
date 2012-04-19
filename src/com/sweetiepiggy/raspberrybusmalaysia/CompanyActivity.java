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

public class CompanyActivity extends Activity {
	private String m_company = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.company);

		DbAdapter dbHelper = new DbAdapter();
		dbHelper.open(this);
		fill_data(dbHelper);
//		dbHelper.close();

		init_submit_button();
	}

	private void fill_data(DbAdapter dbHelper) {
		Cursor c = dbHelper.fetch_companies();
		startManagingCursor(c);
		SimpleCursorAdapter companies = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_item,
				c, new String[] {DbAdapter.KEY_CTR_NAME},
				new int[] {android.R.id.text1});
		Spinner company_spinner = (Spinner) findViewById(R.id.company_spinner);
		company_spinner.setAdapter(companies);

		company_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent,
					View selected_item, int pos,
					long id) {
				m_company = ((Cursor)parent.getItemAtPosition(pos)).getString(1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}

		});
	}

	private void init_submit_button()
	{
		Button submit_button = (Button) findViewById(R.id.submit_button);
		submit_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (m_company.length() == 0) {
					Toast.makeText(getApplicationContext(), "Select Company", Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent(getApplicationContext(), CompanyResultActivity.class);
					Bundle b = new Bundle();
					b.putString("company", m_company);
					intent.putExtras(b);
					startActivity(intent);
				}
			}
		});
	}
}

