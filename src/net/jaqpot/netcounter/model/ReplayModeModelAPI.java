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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

/**
 * @author Louis
 *
 */
public class ReplayModeModelAPI {

	private static DatabaseAPI sDbAPI = null;
	
	private static Location DEFAULT_LOC = new Location("");
	
//	private static int sSignalStrength = 0;
//	
//	private static int sSentBytes = 0;
	
	private static NewDataModel mCurrentModel = null;
	
	private static long mCurrentLineNumber = 0;
	
	private static boolean mFwdMode = true;
	
	public static void clearDB(Context ctx) {
		getDbAPI(ctx).onUpgrade(getDbAPI(ctx).getWritableDatabase(), 1, 2);
//		sDbAPI = null;
		mCurrentModel = null;
		mCurrentLineNumber = 0;
	}
	
	public static void setFwdMode(boolean mode) {
		mFwdMode = mode;
	}
	
	public static NewDataModel getDataToSend(Context ctx) { 
		
		NewDataModel old = mCurrentModel;
		
		updateCurrentModel(ctx);
		
		return (mCurrentModel != null) ? mCurrentModel.subtract(old) : null;
		
	}
	
	
	private static DatabaseAPI getDbAPI(Context ctx) {
		if (sDbAPI == null) {
			sDbAPI = new DatabaseAPI(ctx);
			DEFAULT_LOC.setLatitude(0.0);
			DEFAULT_LOC.setLongitude(0.0);
		}
		
		return sDbAPI;
	}
	
	public static void setDbAPI(DatabaseAPI api) {
		sDbAPI = api;
	}
	
	private static void updateCurrentModel(Context ctx) {
		mCurrentLineNumber += mFwdMode ? 1 : -1;
		
		if (mCurrentLineNumber < 0) { 
			mCurrentModel = null;
		} else {

			SQLiteDatabase db = getDbAPI(ctx).getReadableDatabase();
			Cursor c = db.rawQuery("SELECT * FROM " + DatabaseAPI.MyCounter.TABLE_NAME +
					" ORDER BY " + DatabaseAPI.MyCounter.VALUE_TS + " LIMIT " + mCurrentLineNumber + ",1", null);

			if (c.moveToFirst()) {
				mCurrentModel = new NewDataModel(
						c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.BYTES)),
						c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.CALL_DURATION)),
						c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.SMS_SENT)),
						c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.SIGNAL_STRENGTH)),
						(float) c.getDouble(c.getColumnIndex(DatabaseAPI.MyCounter.CELL_DISTANCE)));
				Log.d("Debug", "Selected value among " + c.getCount() + " in DB where BYTES = " + mCurrentModel.getBytes()
						+ " TS = " + c.getString(c.getColumnIndex(DatabaseAPI.MyCounter.VALUE_TS))
						+ " (offset = " + mCurrentLineNumber + ")");
			} else {
				mCurrentModel = null;
			}

			c.close();
		}
		
	}
	
	public static SQLiteDatabase getReadableAPI(Context ctx) {
		return getDbAPI(ctx).getReadableDatabase();
	}
	
	public static SQLiteDatabase getWritableAPI(Context ctx) {
		return getDbAPI(ctx).getWritableDatabase();
	}
	
//	public static String[] modelToStringArray(NewDataModel model) {
//		return null;
//	}
}
