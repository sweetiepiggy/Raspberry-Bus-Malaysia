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

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class RbmMapActivity extends MapActivity
{
	/* center map here */
	private static final GeoPoint CENTER_GEOPOINT = new GeoPoint(3657249,102144600);
	private static final int CENTER_ZOOM = 8;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		Bundle b = getIntent().getExtras();
		boolean draw_routes = (b == null) ? false : b.getBoolean("draw_routes");
		boolean set_result = (b == null) ? false : b.getBoolean("set_result");
		boolean valid_from = (b == null) ? false : b.getBoolean("valid_from");
		boolean valid_to = (b == null) ? false : b.getBoolean("valid_to");
		String from_city = (b == null) ? "" : b.getString("from_city");
		if (from_city == null || from_city.length() == 0) {
			valid_to = false;
		}

		MapView mv = (MapView) findViewById(R.id.mapview);
		mv.setBuiltInZoomControls(true);

		List<Overlay> mapOverlays = mv.getOverlays();
		Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
		RbmItemizedOverlay itemizedoverlay = new RbmItemizedOverlay(drawable, this, set_result);

		DbAdapter dbHelper = new DbAdapter();
		dbHelper.open(this);
		Cursor c = valid_from ? dbHelper.fetch_from_stations() :
			valid_to ? dbHelper.fetch_to_stations_from_city(from_city) :
			dbHelper.fetch_stations();
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

		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				GeoPoint gp = new GeoPoint((int) (location.getLatitude() * 1E6),
						(int) (location.getLongitude() * 1E6));
				OverlayItem oi = new OverlayItem(gp, "last updated", "location");
				Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
				RbmItemizedOverlay itemizedoverlay = new RbmItemizedOverlay(drawable, RbmMapActivity.this, false);
				itemizedoverlay.addOverlay(oi);
				MapView mv = (MapView) findViewById(R.id.mapview);
				List<Overlay> mapOverlays = mv.getOverlays();
				mapOverlays.add(itemizedoverlay);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		//Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		//if (lastKnownLocation != null) {
		//	GeoPoint gp = new GeoPoint((int) (lastKnownLocation.getLatitude() * 1E6),
		//			(int) (lastKnownLocation.getLongitude() * 1E6));
		//	OverlayItem oi = new OverlayItem(gp, "last known", "location");
		//	itemizedoverlay.addOverlay(oi);
		//}

		mapOverlays.add(itemizedoverlay);

		if (draw_routes) {
			mapOverlays.add(new RouteOverlay(this, mv.getProjection()));
		}

		MapController mc = mv.getController();
		mc.setCenter(CENTER_GEOPOINT);
		mc.setZoom(CENTER_ZOOM);
	}


	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}
}

