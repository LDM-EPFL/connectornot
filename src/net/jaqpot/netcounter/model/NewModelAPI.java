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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.jaqpot.netcounter.NetCounterApplication;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;
import android.util.Log;

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
		SQLiteDatabase db = getReadableDB(ctx);
		
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
				sb.append(distance).append(FIELD_SEPARATOR);
				sb.append(strength).append(FIELD_SEPARATOR);
				sb.append(sms).append('\n');
				writer.append(sb.toString());
			}
		} finally {
			c.close();
			// db.close();
//			operationEnded();
		}
	}
	
	public static void importFromCsv(Context ctx, File file) {
		//Clear data and overwrite.
		//If writing fails we are left with an empty db.
		Log.d("DEBUG", "Starting import from csv");
		clearDB(ctx);
		SQLiteDatabase db = getWritableDB(ctx);
		
		db.beginTransaction();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String headers = br.readLine(); //This only contains the headers.
			String[] v = { "", "", "", "", "", "" };
			v = headers.split(Character.toString(FIELD_SEPARATOR));


			if (v[0].equals(DatabaseAPI.MyCounter.VALUE_TS) &&
					v[1].equals(DatabaseAPI.MyCounter.BYTES) &&
					v[2].equals(DatabaseAPI.MyCounter.CALL_DURATION) &&
					v[3].equals(DatabaseAPI.MyCounter.CELL_DISTANCE) &&
					v[4].equals(DatabaseAPI.MyCounter.SIGNAL_STRENGTH) &&
					v[5].equals(DatabaseAPI.MyCounter.SMS_SENT)) {

				String line;

				while ((line = br.readLine()) != null) {
					insertLineIntoDB(line, db);
				}
				db.setTransactionSuccessful();
			} 

		} catch (FileNotFoundException e) {
			Log.e("NewModelAPI", "csv file not found", e);
		} catch (IOException e) {
			Log.e("NewModelAPI", "IO in reading csv", e);
		} finally {
			db.endTransaction();
			db.close();
		}
	}
	
	private static void insertLineIntoDB(String line, SQLiteDatabase db) {
		if (line == null || db == null) {
			Log.d("DEBUG", "Failed to insert line into DB");
			return;
		}
		
		String[] v = { "", "", "", "", "", "" };
		v = line.split(Character.toString(FIELD_SEPARATOR));
		
		
		ContentValues values = new ContentValues();
		values.put(DatabaseAPI.MyCounter.VALUE_TS, v[0]);
		values.put(DatabaseAPI.MyCounter.BYTES, v[1]);
		values.put(DatabaseAPI.MyCounter.CALL_DURATION, v[2]);
		values.put(DatabaseAPI.MyCounter.CELL_DISTANCE, v[3]);
		values.put(DatabaseAPI.MyCounter.SIGNAL_STRENGTH, v[4]);
		values.put(DatabaseAPI.MyCounter.SMS_SENT, v[5]);
		
		long row = db.insert(DatabaseAPI.MyCounter.TABLE_NAME, null, values);
		Log.d("DEBUG", "inserted at row " + row);
	}
	
	public static void setRepDirection(Context ctx, boolean fwd) {
		if (!getPlayMode(ctx)) {
			ReplayModeModelAPI.setFwdMode(fwd);
		}
	}
	
	private static SQLiteDatabase getReadableDB(Context ctx) {
		SQLiteDatabase db;
		
		if (getPlayMode(ctx)) {
			db = PlayModeModelAPI.getReadableAPI(ctx);
		} else {
			db = ReplayModeModelAPI.getReadableAPI(ctx);
		}
		
		return db;
	}
	
	private static SQLiteDatabase getWritableDB(Context ctx) {
		SQLiteDatabase db;
		
		if (getPlayMode(ctx)) {
			db = PlayModeModelAPI.getWritableAPI(ctx);
		} else {
			db = ReplayModeModelAPI.getWritableAPI(ctx);
		}
		
		return db;
	}
}
