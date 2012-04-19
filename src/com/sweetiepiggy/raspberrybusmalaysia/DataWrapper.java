/*
    Copyright (C) 2012 Sweetie Piggy Apps <sweetiepiggyapps@gmail.com>

    This file is part of Report Malaysia Taxi.

    Report Malaysia Taxi is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    Report Malaysia Taxi is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Report Malaysia Taxi; if not, see <http://www.gnu.org/licenses/>.
*/

package com.sweetiepiggy.raspberrybusmalaysia;

public class DataWrapper
{
	public class date_and_time
	{
		public int year;
		public int month;
		public int day;
		public int hour;
		public int minute;

	}

	public date_and_time sched_time;
	public date_and_time depart_time;
	public date_and_time arrival_time;

	public DataWrapper()
	{
		sched_time = new date_and_time();
		depart_time = new date_and_time();
		arrival_time = new date_and_time();
	}
}

