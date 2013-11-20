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

import java.io.FileWriter;
import java.io.IOException;

import net.jaqpot.netcounter.NetCounterApplication;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;

/**
 * @author Louis
 *
 */
public class NewModelAPI {
	
//	private static DatabaseAPI sDbAPI = null;
	
//	public static void resetModel(Context ctx, Location defaultLocation) {
//		if (getFwdMode(ctx)) {
//			PlayModeModelAPI.
//		}
//	}
	
	private static final char FIELD_SEPARATOR = ',';
	
	public static void clearDB(Context ctx) {
		if (getPlayMode(ctx)) {
			PlayModeModelAPI.clearDB(ctx);
		} else {
			ReplayModeModelAPI.clearDB(ctx);
		}
	}
	
	public static NewDataModel getDataToSend(Context ctx) {
		return getPlayMode(ctx) ? PlayModeModelAPI.getDataToSend(ctx) : ReplayModeModelAPI.getDataToSend(ctx);
	}
	
	
	private static boolean getPlayMode(Context ctx) {
		return !((NetCounterApplication) ctx.getApplicationContext())
				.getAdapter(SharedPreferences.class).getBoolean("recMode", false);
	}
	
	public static String exportToCsv(Context ctx) throws IOException {
			final StringBuilder sb = new StringBuilder();
			sb.append(Environment.getExternalStorageDirectory());
			sb.append("/connectornot");
//			sb.append(System.currentTimeMillis());
			sb.append(".csv");
			// Runs the export.
			FileWriter writer = null;
			String s = sb.toString();
			try {
				writer = new FileWriter(s);
				exportDataToCsv(writer, ctx);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						// Ignore.
					}
				}
			}
			return s;
	}
	
	private static void exportDataToCsv(FileWriter writer, Context ctx) throws IOException {
//		operationStarted();
		SQLiteDatabase db;
		
		if (getPlayMode(ctx)) {
			db = PlayModeModelAPI.getReadableAPI(ctx);
		} else {
			db = ReplayModeModelAPI.getReadableAPI(ctx);
		}
		
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		query.setTables(DatabaseAPI.MyCounter.TABLE_NAME);
		Cursor c = query.query(db, null, null, null, null, null, DatabaseAPI.MyCounter.VALUE_TS);
		try {
			// Writes headers.
			StringBuilder sb = new StringBuilder();
			sb.append(DatabaseAPI.MyCounter.VALUE_TS).append(FIELD_SEPARATOR);
			sb.append(DatabaseAPI.MyCounter.BYTES).append(FIELD_SEPARATOR);
			sb.append(DatabaseAPI.MyCounter.CALL_DURATION).append(FIELD_SEPARATOR);
			sb.append(DatabaseAPI.MyCounter.CELL_DISTANCE).append(FIELD_SEPARATOR);
			sb.append(DatabaseAPI.MyCounter.SIGNAL_STRENGTH).append(FIELD_SEPARATOR);
			sb.append(DatabaseAPI.MyCounter.SMS_SENT).append('\n');
			writer.append(sb.toString());
			// Writes data.
			for (int i = 0; i < c.getCount(); i++) {
				c.moveToNext();
//				String inter = c.getString(c.getColumnIndex(DailyCounter.INTERFACE));
//				String day = c.getString(c.getColumnIndex(DailyCounter.DAY));
//				long rx = c.getLong(c.getColumnIndex(DailyCounter.RX));
//				long tx = c.getLong(c.getColumnIndex(DailyCounter.TX));
				
				String ts = c.getString(c.getColumnIndex(DatabaseAPI.MyCounter.VALUE_TS));
				int bytes = c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.BYTES));
				int duration = c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.CALL_DURATION));
				int sms = c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.SMS_SENT));
				int strength = c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.SIGNAL_STRENGTH));
				float distance = (float) c.getDouble(c.getColumnIndex(DatabaseAPI.MyCounter.CELL_DISTANCE));
				
				sb = new StringBuilder();
				sb.append(ts).append(FIELD_SEPARATOR);
				sb.append(bytes).append(FIELD_SEPARATOR);
				sb.append(duration).append(FIELD_SEPARATOR);
				sb.append(sms).append(FIELD_SEPARATOR);
				sb.append(strength).append(FIELD_SEPARATOR);
				sb.append(distance).append('\n');
				writer.append(sb.toString());
			}
		} finally {
			c.close();
			// db.close();
//			operationEnded();
		}
	}
}
