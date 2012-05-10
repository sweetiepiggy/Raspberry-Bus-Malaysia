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
import android.widget.TextView;

public class TripsActivity extends Activity {
	/* TODO: put this in Constants.java? */
	private static final String UNKNOWN = "<Unknown Company>";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trips);

		Bundle b = getIntent().getExtras();
		String from_city = (b == null) ? "<NULL>" : b.getString("from_city");
		String to_city = (b == null) ? "<NULL>" : b.getString("to_city");
		String company = (b == null) ? "<NULL>" : b.getString("company");
		String company_display = company.length() == 0 ? UNKNOWN : company;

		((TextView) findViewById(R.id.company)).setText(company_display);
		((TextView) findViewById(R.id.route)).setText(from_city + " to " + to_city);

		DbAdapter dbHelper = new DbAdapter();
		dbHelper.open(this);

		Cursor c_comp = dbHelper.fetch_avg_delay(company);
		startManagingCursor(c_comp);
		if (c_comp.moveToFirst()) do {
			String avg_delay = format_time_min(c_comp.getInt(c_comp.getColumnIndex(DbAdapter.AVG_DELAY)));
			((TextView) findViewById(R.id.total_avg_delay)).setText(avg_delay);
		} while (c_comp.moveToNext());
	}

	/* TODO: move format_time() and format_time_min() to their own class */
	private String format_time_min(int time)
	{
		String negative = "";
		if (time < 0) {
			negative = "-";
			time *= -1;
		}

		int min = time / 60;
		return String.format("%s%dmin", negative, min);
	}
}

