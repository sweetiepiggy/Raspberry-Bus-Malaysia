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
import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class RouteOverlay extends Overlay
{
	private Projection mProjection;
	private Context mContext;
	private ArrayList<GeoPointPair> mGpPairs = new ArrayList<GeoPointPair>();

	private class GeoPointPair
	{
		public GeoPoint from;
		public GeoPoint to;

		public GeoPointPair(GeoPoint f, GeoPoint t)
		{
			from = f;
			to = t;
		}
	}

	public RouteOverlay(Context context, Projection p)
	{
		mContext = context;
		mProjection = p;

		DbAdapter dbHelper = new DbAdapter();
		dbHelper.open(mContext);
		Cursor c_from = dbHelper.fetch_from_stations();
		if (c_from.moveToFirst()) do {
			int from_latitude = c_from.getInt(c_from.getColumnIndex(DbAdapter.KEY_LATITUDE));
			int from_longitude = c_from.getInt(c_from.getColumnIndex(DbAdapter.KEY_LONGITUDE));
			String from_station = c_from.getString(c_from.getColumnIndex(DbAdapter.KEY_STN));

			GeoPoint from_gp = new GeoPoint(from_latitude, from_longitude);

			Cursor c_to = dbHelper.fetch_to_stations(from_station);
			if (c_to.moveToFirst()) do {
				int to_latitude = c_to.getInt(c_to.getColumnIndex(DbAdapter.KEY_LATITUDE));
				int to_longitude = c_to.getInt(c_to.getColumnIndex(DbAdapter.KEY_LONGITUDE));

				GeoPoint to_gp = new GeoPoint(to_latitude, to_longitude);

				mGpPairs.add(new GeoPointPair(from_gp, to_gp));
			} while (c_to.moveToNext());
			c_to.close();
		} while (c_from.moveToNext());
		c_from.close();
		dbHelper.close();
	}

	public void draw(Canvas canvas, MapView mapv, boolean shadow)
	{
		super.draw(canvas, mapv, shadow);

		Paint paint = new Paint();
		paint.setDither(true);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(2);
		paint.setAlpha(50);

		Iterator<GeoPointPair> itr = mGpPairs.iterator();
		while (itr.hasNext()) {
			GeoPointPair gpp = itr.next();
			draw_line(canvas, paint, gpp.from, gpp.to);
		}
	}

	private void draw_line(Canvas canvas, Paint paint, GeoPoint gp1, GeoPoint gp2)
	{
		Point p1 = new Point();
		Point p2 = new Point();
		Path path = new Path();

		mProjection.toPixels(gp1, p1);
		mProjection.toPixels(gp2, p2);

		path.moveTo(p2.x, p2.y);
		path.lineTo(p1.x,p1.y);

		canvas.drawPath(path, paint);
	}
}

