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

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CompanyActivity extends ListActivity
{
	private DbAdapter mDbHelper;
	private boolean m_is_operator = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle b = getIntent().getExtras();
		m_is_operator = (b == null) ? false : b.getBoolean("is_operator");

		Intent intent = getIntent();
		String query = "";
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
		}

		mDbHelper = new DbAdapter();
		mDbHelper.open(this);
		fill_data(mDbHelper, query);

		init_click();
	}

	@Override
	protected void onDestroy() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
		super.onDestroy();
	}

	private void fill_data(DbAdapter dbHelper, String company_query)
	{
		Cursor c = m_is_operator ?
			dbHelper.fetch_operators('%' + company_query + '%') :
			dbHelper.fetch_agents('%' + company_query + '%');
		startManagingCursor(c);
		String key = m_is_operator ?
			DbAdapter.KEY_OPERATOR : DbAdapter.KEY_AGENT;
		SimpleCursorAdapter companies = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1,
				c, new String[] {key},
				new int[] {android.R.id.text1});
		setListAdapter(companies);
	}

	private void init_click()
	{
		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int pos, long id) {
				String company = ((TextView) v).getText().toString();
				Intent intent = new Intent(getApplicationContext(),
					CompanyResultActivity.class);
				Bundle b = new Bundle();
				b.putString("company", company);
				b.putBoolean("is_operator", m_is_operator);
				intent.putExtras(b);
				startActivity(intent);
			}
		});
	}
}

