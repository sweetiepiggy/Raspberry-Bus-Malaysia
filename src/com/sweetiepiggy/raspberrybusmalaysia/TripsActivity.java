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
	}

}
