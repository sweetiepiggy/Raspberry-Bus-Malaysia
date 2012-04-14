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

public class StatResultActivity extends Activity {

	private DbAdapter mDbHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stat_result);
		Bundle b = getIntent().getExtras();
		String from_city = (b == null) ? "<NULL>" : b.getString("from_city");
		String to_city = (b == null) ? "<NULL>" : b.getString("to_city");
		((TextView) findViewById(R.id.title)).setText(from_city + " to " + to_city);

		mDbHelper = new DbAdapter(this);
		mDbHelper.open();

		Cursor c = mDbHelper.fetch_avg_by_company(from_city, to_city);
		startManagingCursor(c);
		String results = c.getColumnName(0) + " " + c.getColumnName(1) + " " + c.getColumnName(2) + "\n";
		for (int i=0; i < c.getCount(); ++i) {
			c.moveToPosition(i);
			String company = c.getString(0);
			String avg = c.getString(1);
			String count = c.getString(2);
			results += company + "\t" + avg +  "\t" + count + "\n";
		}
		((TextView) findViewById(R.id.title)).setText(results);
	}

}
