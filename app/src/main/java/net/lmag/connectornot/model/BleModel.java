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


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.util.Base64;

import net.lmag.connectornot.utils.BLEManager;

import org.joda.time.DateTime;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
//import org.pocketcampus.android.platform.sdk.core.IView;
//import org.pocketcampus.android.platform.sdk.core.PluginModel;
//import org.pocketcampus.plugin.ble.android.iface.IbleModel;
//import org.pocketcampus.plugin.ble.android.iface.IbleView;
//import org.pocketcampus.plugin.ble.android.utils.BLEManager;


public class BleModel {
	
	/**
	 * Reference to the Views that need to be notified when the stored data changes.
	 */
//	IbleView mListeners = (IbleView) getListeners();
	
	Map<String, BLEManager> mPluginToBLE;
	
	BLEManager mManager;
	
	private static final String PLUGIN_PREF = "blePlugin";
	private static final String MASK_PREF = "bleBitmask";
	private static final String MASK_DATE_PREF = "bleMaskDate";
	private static final String ID_PREF = "bleID";
	private static final String ID_DATE_PREF = "bleIDDate";
	private static final String BLE_ENABLED_PREF = "bleEnabled";
	
	
	private SharedPreferences mSharedPrefs;
	
	private DailyID mID;
	
	/**
	 * Constructor with reference to the context.
	 * 
	 * We need the context to be able to instantiate
	 * the SharedPreferences object in order to use
	 * persistent storage.
	 * 
	 * @param context is the Application Context.
	 */
	public BleModel(Context context) {
		mPluginToBLE = new HashMap<String, BLEManager>();
		mSharedPrefs = context.getSharedPreferences(PLUGIN_PREF, Context.MODE_PRIVATE);
		
		checkID();
	}

	
	private void retrieveID() {
		String id = mSharedPrefs.getString(ID_PREF, null);
		if(id == null) {
			mID = null;
		} else {
			mID = new DailyID(id, new DateTime(mSharedPrefs.getLong(MASK_DATE_PREF, 0)));
		}
		
	}
	
	private void storeID() {
		Editor edit = mSharedPrefs.edit();
		
		edit.putString(ID_PREF, mID.getID());
		edit.putLong(ID_DATE_PREF, mID.getCreationDate().getMillis());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            edit.apply();
        } else {
            edit.commit();
        }
	}
	
	private void checkID() {
		retrieveID();
		if (mID == null || !mID.isFresh()) {
			mID = generateID();
			storeID();
		}
	}
	
	public String getID() {
		checkID();
		return mID.getID();
	}


	public BLEManager getManager(String pluginName) {
		return mPluginToBLE.get(pluginName);
	}
	
	public void addManager(String pluginName, BLEManager newManager) { 
			mPluginToBLE.put(pluginName, newManager);
	}
	
	public BLEManager removeManager(String pluginName) {
		return mPluginToBLE.remove(pluginName);
		
	}
	
	public ByteBuffer getCurrentBitMask() {
		String content = mSharedPrefs.getString(MASK_PREF, null);
		
		if(content == null) {
			return null;
		} else {
			byte[] array = Base64.decode(content, Base64.DEFAULT);
			return ByteBuffer.wrap(array);
		}
	}
	
	public long getMaskTS() {
		long content = mSharedPrefs.getLong(MASK_DATE_PREF, 0);
		
		return content;
	}
	
	public void storeBitMask(byte[] array) {
		String content = Base64.encodeToString(array, Base64.DEFAULT);
		
		Editor edit = mSharedPrefs.edit();
		edit.putString(MASK_PREF, content);
		edit.putLong(MASK_DATE_PREF, DateTime.now().getMillis());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            edit.apply();
        } else {
            edit.commit();
        }
	}
	
	private DailyID generateID() {
		return new DailyID(UUID.randomUUID().toString(), DateTime.now());
	}
	
	
	
//	public BLEManager createFoodManager(Context ctx, String pluginName,
//			bleController controller, ArrayList<ScanInterval> list) {
//		return ManagerFactory.getFoodManager(ctx, pluginName, controller, list);
//	}
	
	private class DailyID {
		private String mID;
		private DateTime mCreationDate;
		
		private DailyID(String id, DateTime creation) {
			mID = id;
			mCreationDate = creation;
		}
		
		
		
		public DateTime getCreationDate() {
			return mCreationDate;
		}
		
		public String getID() {
			return mID;
		}
		
		public boolean isFresh() {
//			MyLog.d("DailyID DEBUG", msg)
//			return mCreationDate.getDayOfYear() == DateTime.now().getDayOfYear();
			return true;
		}
	}
	
	public boolean getBleEnabled() {
		return mSharedPrefs.getBoolean(BLE_ENABLED_PREF, false);
	}

	
}
