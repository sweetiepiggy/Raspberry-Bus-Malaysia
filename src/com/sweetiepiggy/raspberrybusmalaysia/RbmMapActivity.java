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
	final static GeoPoint CENTER_GEOPOINT = new GeoPoint(3145792,101701098);

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

		GeoPoint sg_nibong_pt = new GeoPoint(5343523,100300453);
		OverlayItem sg_nibong_overlay = new OverlayItem(sg_nibong_pt, "Sg Nibong", "Penang");
		itemizedoverlay.addOverlay(sg_nibong_overlay);

		GeoPoint komtar_pt = new GeoPoint(5414478,100331887);
		OverlayItem komtar_overlay = new OverlayItem(komtar_pt, "Komtar", "Penang");
		itemizedoverlay.addOverlay(komtar_overlay);

		GeoPoint vistana_pt = new GeoPoint(5336945,100291939);
		OverlayItem vistana_overlay = new OverlayItem(vistana_pt, "Vistana Hotel", "Penang");
		itemizedoverlay.addOverlay(vistana_overlay);

		GeoPoint puduraya_pt = new GeoPoint(3145792,101701098);
		OverlayItem puduraya_overlay = new OverlayItem(puduraya_pt, "Puduraya", "Kuala Lumpur");
		itemizedoverlay.addOverlay(puduraya_overlay);

		GeoPoint ktm_pt = new GeoPoint(3140021,101693133);
		OverlayItem ktm_overlay = new OverlayItem(ktm_pt, "KTM", "Kuala Lumpur");
		itemizedoverlay.addOverlay(ktm_overlay);

		GeoPoint hentian_duta_pt = new GeoPoint(3171572,101674013);
		OverlayItem hentian_duta_overlay = new OverlayItem(hentian_duta_pt, "Hentian Duta", "Kuala Lumpur");
		itemizedoverlay.addOverlay(hentian_duta_overlay);

		GeoPoint jalan_duta_pt = new GeoPoint(3176939,101657662);
		OverlayItem jalan_duta_overlay = new OverlayItem(jalan_duta_pt, "Jalan Duta", "Kuala Lumpur");
		itemizedoverlay.addOverlay(jalan_duta_overlay);

		GeoPoint damansara_pt = new GeoPoint(3151031,101615107);
		OverlayItem damansara_overlay = new OverlayItem(damansara_pt, "Damansara", "Kuala Lumpur");
		itemizedoverlay.addOverlay(damansara_overlay);

		GeoPoint puchong_pt = new GeoPoint(3035408,101616259);
		OverlayItem puchong_overlay = new OverlayItem(puchong_pt, "Puchong Tesco", "Puchong");
		itemizedoverlay.addOverlay(puchong_overlay);

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

