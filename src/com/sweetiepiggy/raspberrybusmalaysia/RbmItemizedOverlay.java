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

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.OverlayItem;

public class RbmItemizedOverlay extends ItemizedOverlay
{
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private boolean mSetResult;

	public RbmItemizedOverlay(Drawable defaultMarker, Context context, boolean set_result) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		mSetResult = set_result;
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		String station = item.getTitle();
		String city = item.getSnippet();

		prompt_confirm(station, city, mSetResult);
		return true;
	}

	private void prompt_confirm(final String station, final String city, final boolean set_result)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		if (station.equals(city)) {
			builder.setTitle(station);
		} else {
			builder.setTitle(station + ", " + city);
		}
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (set_result) {
					Bundle b = new Bundle();
					b.putString("station", station);
					b.putString("city", city);

					Intent i = new Intent();
					i.putExtras(b);

					MapActivity ma = (MapActivity) mContext;
					ma.setResult(Activity.RESULT_OK, i);
					ma.finish();
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}

