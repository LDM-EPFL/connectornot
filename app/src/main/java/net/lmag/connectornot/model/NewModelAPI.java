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

package net.lmag.connectornot.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;

import net.lmag.connectornot.NetCounterApplication;
import net.lmag.connectornot.com.ServApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
	private static final String ID_PREF = "bleID";
	private static final String PLUGIN_PREF = "blePlugin";
	
	private static final char FIELD_SEPARATOR = ',';
	
	public static void clearDB(Context ctx) {
		if (getPlayMode(ctx)) {
			PlayModeModelAPI.clearDB(ctx);
		} else {
			ReplayModeModelAPI.clearDB(ctx);
		}
	}
	
	public static NewDataModel getDataToSend(Context ctx) {
		String phoneID = retrieveID(ctx);
        NewDataModel data = null;
        if(getPlayMode(ctx)) {
            data = PlayModeModelAPI.getDataToSend(ctx);
        } else {
            data = ReplayModeModelAPI.getDataToSend(ctx);
        }
        if(data != null) {
            data.setPhoneID(phoneID);
        }

        return data;
	}
	
	public static Map <String, Object> getDataAsMap(Context ctx) {
		return modelToMap(getDataToSend(ctx));
	}
	
	public static Map <String, Object> modelToMap(NewDataModel model) {
		if (model == null) {
			return null;
		}
		
		Map <String, Object> content = new HashMap<String, Object>();
		content.put("/conversation", model.getConvDuration());
		content.put("/sms", model.getSMS());
		content.put("/data", model.getBytes());
		content.put("/signal", model.getSignalStrength());
		content.put("/celldistance", model.getCellDistance());
		
		return content;
	}
	
	
	private static boolean getPlayMode(Context ctx) {
		return ((NetCounterApplication) ctx.getApplicationContext())
				.getAdapter(SharedPreferences.class).getString("mode", "0").equals("0");
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
					writer.close();
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
			sb.append(DatabaseAPI.MyCounter.SMS_SENT);
			sb.append(DatabaseAPI.MyCounter.BEACON).append(FIELD_SEPARATOR);
            sb.append(DatabaseAPI.MyCounter.RP_LOCATION).append('\n');
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
				int beacon = c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.BEACON));
                String rpLoc = c.getString(c.getColumnIndex(DatabaseAPI.MyCounter.RP_LOCATION));
				
				sb = new StringBuilder();
				sb.append(ts).append(FIELD_SEPARATOR);
				sb.append(bytes).append(FIELD_SEPARATOR);
				sb.append(duration).append(FIELD_SEPARATOR);
				sb.append(distance).append(FIELD_SEPARATOR);
				sb.append(strength).append(FIELD_SEPARATOR);
				sb.append(sms);
				sb.append(beacon);
                sb.append(rpLoc).append('\n');
				writer.append(sb.toString());
				writer.flush();
			}
		} finally {
			c.close();
			// db.close();
//			operationEnded();
		}
	}

//    public static void set
	
	private static String retrieveID(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PLUGIN_PREF, Context.MODE_PRIVATE);
		return prefs.getString(ID_PREF, "noID");
	}

    private static void storeID(Context ctx, String newID) {
        SharedPreferences prefs = ctx.getSharedPreferences(PLUGIN_PREF, Context.MODE_PRIVATE);
        prefs.edit().putString(ID_PREF, newID).commit();
    }
	
	private static ServApi.Handler servHandler = new ServApi.Handler() {
		//TODO update DB with data sent.
		public void onError(Error error) {
			MyLog.e("DEBUG", "============================\n" +
					"NetCounterService: Could not send to serv.\n" +
					"============================");
			
		}
		
		public void callback(String struct) {
			try {
				JSONObject jo = new JSONObject(struct);
//				NewDataModel.sentBytes(Integer.valueOf(jo.getInt("length")));
				
//				mSentBytes = Integer.valueOf(jo.getInt("length"));
				
				PlayModeModelAPI.setSentBytes(Integer.valueOf(jo.getInt("length")));
				
			} catch (JSONException e) {
				MyLog.e("", "", e);
			}
			
			MyLog.d("DEBUG", "============================\n" +
					"SENT HTTP POST PACKET: \"" + struct + "\".\n" +
					"============================");
			
			
		}
	};
	
	public static void exportDataToServ(Context ctx) {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("dodo", dataToJSON(ctx));
		
		ServApi.sendMsg(payload, servHandler);
	}
	
	private static JSONObject dataToJSON(Context ctx) {
		
		String phoneID = retrieveID(ctx);

        if("noID".equals(phoneID)) {
            phoneID = UUID.randomUUID().toString();
            storeID(ctx, phoneID);
        }
		
		SQLiteDatabase db = getReadableDB(ctx);
		
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		query.setTables(DatabaseAPI.MyCounter.TABLE_NAME);
		Cursor c = query.query(db, null, null, null, null, null, DatabaseAPI.MyCounter.VALUE_TS);
		JSONObject jo = new JSONObject();
//		JSONArray ja;
		try {
			
			// Writes data.
			for (int i = 0; i < c.getCount(); i++) {
				c.moveToNext();
				
				String ts = c.getString(c.getColumnIndex(DatabaseAPI.MyCounter.VALUE_TS));
				int bytes = c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.BYTES));
				int duration = c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.CALL_DURATION));
				int sms = c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.SMS_SENT));
				int strength = c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.SIGNAL_STRENGTH));
				float distance = (float) c.getDouble(c.getColumnIndex(DatabaseAPI.MyCounter.CELL_DISTANCE));
				int minor = c.getInt(c.getColumnIndex(DatabaseAPI.MyCounter.BEACON));
                String rpLoc = c.getString(c.getColumnIndex(DatabaseAPI.MyCounter.RP_LOCATION));
				
//				ArrayList<Object> al = new ArrayList<Object>();
//				al.add(ts);
//				al.add(bytes);
//				al.add(duration);
//				al.add(sms);
//				al.add(distance);
				
//				ja = new JSONArray(al);
                String countStr = Integer.toString(i);
				jo.accumulate(countStr, new JSONObject().put("timestamp", ts));
				jo.accumulate(countStr, new JSONObject().put("bytes", bytes));
				jo.accumulate(countStr, new JSONObject().put("conversation", duration));
				jo.accumulate(countStr, new JSONObject().put("sms", sms));
				jo.accumulate(countStr, new JSONObject().put("signalstrength", strength));
				jo.accumulate(countStr, new JSONObject().put("distance", distance));
				jo.accumulate(countStr, new JSONObject().put("estimote", minor));
				jo.accumulate(countStr, new JSONObject().put("phoneID", phoneID));
                jo.accumulate(countStr, new JSONObject().put("redpin_location", rpLoc));


			}
		} catch (JSONException e) {
			MyLog.d("NewModelAPI", "Malformed JSON");
		} finally {
			c.close();
			// db.close();
//			operationEnded();
		}
		
		try {
			MyLog.d("NewModelAPI", "created a JSONObject that looks like:\n" + jo.toString(2));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jo;
	}
	
	public static void importFromCsv(Context ctx, File file) throws IOException {
		//Clear data and overwrite.
		//If writing fails we are left with an empty db.
		MyLog.d("DEBUG", "Starting import from csv");
		clearDB(ctx);
		SQLiteDatabase db = getWritableDB(ctx);
		
		db.beginTransaction();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String headers = br.readLine(); //This only contains the headers.
			String[] v = { "", "", "", "", "", "", "", "" };
			v = headers.split(Character.toString(FIELD_SEPARATOR));


			if (v[0].equals(DatabaseAPI.MyCounter.VALUE_TS) &&
					v[1].equals(DatabaseAPI.MyCounter.BYTES) &&
					v[2].equals(DatabaseAPI.MyCounter.CALL_DURATION) &&
					v[3].equals(DatabaseAPI.MyCounter.CELL_DISTANCE) &&
					v[4].equals(DatabaseAPI.MyCounter.SIGNAL_STRENGTH) &&
					v[5].equals(DatabaseAPI.MyCounter.SMS_SENT) &&
					v[6].equals(DatabaseAPI.MyCounter.BEACON) &&
                    v[7].equals(DatabaseAPI.MyCounter.RP_LOCATION)) {

				String line;

				while ((line = br.readLine()) != null) {
					insertLineIntoDB(line, db);
				}
				db.setTransactionSuccessful();
			} 

		} finally {
			db.endTransaction();
			db.close();
		}
	}
	
	private static void insertLineIntoDB(String line, SQLiteDatabase db) {
		if (line == null || db == null) {
			MyLog.d("DEBUG", "Failed to insert line into DB");
			return;
		}

        String[] v = line.split(Character.toString(FIELD_SEPARATOR));

        if(v.length != 8) {
            MyLog.d("DEBUG", "Failed to insert line into DB");
            return;
        }
		
		ContentValues values = new ContentValues();
		values.put(DatabaseAPI.MyCounter.VALUE_TS, v[0]);
		values.put(DatabaseAPI.MyCounter.BYTES, v[1]);
		values.put(DatabaseAPI.MyCounter.CALL_DURATION, v[2]);
		values.put(DatabaseAPI.MyCounter.CELL_DISTANCE, v[3]);
		values.put(DatabaseAPI.MyCounter.SIGNAL_STRENGTH, v[4]);
		values.put(DatabaseAPI.MyCounter.SMS_SENT, v[5]);
		values.put(DatabaseAPI.MyCounter.BEACON, v[6]);
        values.put(DatabaseAPI.MyCounter.RP_LOCATION, v[7]);
		
		long row = db.insert(DatabaseAPI.MyCounter.TABLE_NAME, null, values);
		MyLog.d("DEBUG", "inserted at row " + row);
	}
	
	public static void setRepDirection(Context ctx, boolean fwd) {
		if (!getPlayMode(ctx)) {
			ReplayModeModelAPI.setFwdMode(fwd);
		}
	}

    public static void setLocationID(Context ctx, long id) {
        if(getPlayMode(ctx)) {
            PlayModeModelAPI.setLocationID(id);
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
