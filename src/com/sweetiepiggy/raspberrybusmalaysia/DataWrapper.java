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
import android.net.Uri;

public class DataWrapper
{
	public class date_and_time
	{
		public int year;
		public int month;
		public int day;
		public int hour;
		public int minute;

		public int cmp(date_and_time b) {
			if (year < b.year) {
				return -1;
			} else if (year > b.year) {
				return 1;
			}

			if (month < b.month) {
				return -1;
			} else if (month > b.month) {
				return 1;
			}

			if (day < b.day) {
				return -1;
			} else if (day > b.day) {
				return 1;
			}

			if (hour < b.hour) {
				return -1;
			} else if (hour > b.hour) {
				return 1;
			}

			if (minute < b.minute) {
				return -1;
			} else if (minute > b.minute) {
				return 1;
			}

			return 0;
		}
	}

	public date_and_time sched_time;
	public date_and_time depart_time;
	public date_and_time arrival_time;
	public boolean[] who_selected;
	public boolean[] submit_selected;
	public ArrayList<Uri> photo_uris;
	public ArrayList<Uri> recording_uris;
	public ArrayList<Uri> video_uris;

	public DataWrapper()
	{
		sched_time = new date_and_time();
		depart_time = new date_and_time();
		arrival_time = new date_and_time();
	}
}

