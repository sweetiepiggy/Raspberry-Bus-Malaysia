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

import java.util.List;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class RbmMapActivity extends MapActivity {
	/* center map on Puduraya */
	private static final GeoPoint CENTER_GEOPOINT = new GeoPoint(3145792,101701098);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		MapView mv = (MapView) findViewById(R.id.mapview);
		mv.setBuiltInZoomControls(true);

		List<Overlay> mapOverlays = mv.getOverlays();
		Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
		RbmItemizedOverlay itemizedoverlay = new RbmItemizedOverlay(drawable, this);

		DbAdapter dbHelper = new DbAdapter();
		dbHelper.open(this);
		Cursor c = dbHelper.fetch_stations();
		if (c.moveToFirst()) do {
			int latitude = c.getInt(c.getColumnIndex(DbAdapter.KEY_LATITUDE));
			int longitude = c.getInt(c.getColumnIndex(DbAdapter.KEY_LONGITUDE));
			String station = c.getString(c.getColumnIndex(DbAdapter.KEY_STN));
			String city = c.getString(c.getColumnIndex(DbAdapter.KEY_CITY));

			GeoPoint gp = new GeoPoint(latitude, longitude);
			OverlayItem oi = new OverlayItem(gp, station, city);
			itemizedoverlay.addOverlay(oi);
		} while (c.moveToNext());
		c.close();
		dbHelper.close();

		mapOverlays.add(itemizedoverlay);

		MapController mc = mv.getController();
		mc.setCenter(CENTER_GEOPOINT);
		mc.setZoom(8);
	}


	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}

