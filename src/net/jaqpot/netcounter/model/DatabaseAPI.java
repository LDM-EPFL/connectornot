/*
 * Copyright (C) 2009 Cyril Jaquier
 *
 * This file is part of NetCounter.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package net.jaqpot.netcounter.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * @author Louis
 *
 */
public class DatabaseAPI  extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "network.db";

	private static final int DATABASE_VERSION = 2;
	
//	private static final DateFormat DF_DATETIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//
//	private static final DateFormat DF_DATE = new SimpleDateFormat("yyyy-MM-dd");
//
//	private static final DateFormat DF_LOCALE = DateFormat.getDateTimeInstance();
//
//	private static final DateFormat DF_DATE_LOCALE = DateFormat.getDateInstance();
	
	public static final class MyCounter implements BaseColumns {
		// This class cannot be instantiated
		private MyCounter() {
		}

		public static final String TABLE_NAME = "data_entry";
		public static final String BYTES = "bytes";
		public static final String CALL_DURATION = "call_duration";
		public static final String SMS_SENT = "sms_sent";
		public static final String SIGNAL_STRENGTH = "signal_strength";
		public static final String CELL_DISTANCE = "cell_distance";
		public static final String VALUE_TS = "value_ts";
		
	}
	
	public DatabaseAPI(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		//FIXME ugly hack.
		onCreate(getWritableDatabase());
	}
	
 	@Override
	public void onCreate(SQLiteDatabase db) {
// 		Log.d("DEBUG", "CREATING THE TABLE!");
 		if (db == null) {
			return;
		}
		db.execSQL("CREATE TABLE IF NOT EXISTS " + MyCounter.TABLE_NAME + " (" +
				MyCounter._ID + " INTEGER PRIMARY KEY," +
				MyCounter.VALUE_TS + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
				MyCounter.BYTES + " INTEGER," + 
				MyCounter.CALL_DURATION + " INTEGER," +
				MyCounter.SMS_SENT + " INTEGER," +
				MyCounter.SIGNAL_STRENGTH + " INTEGER," +
				MyCounter.CELL_DISTANCE + " FLOAT);");
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (db == null) {
			return;
		}
		db.execSQL("DROP TABLE IF EXISTS " + MyCounter.TABLE_NAME);
	    onCreate(db);
	}
	
//	publi

}
