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

package net.lmag.connectornot;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import net.lmag.connectornot.activity.NetCounterActivity;
import net.lmag.connectornot.model.MyLog;
import net.lmag.connectornot.service.CallDetectService;
import net.lmag.connectornot.service.NetCounterService;

public class NetCounterApplication extends Application {

	public static final boolean LOG_ENABLED = false;

	public static final int NOTIFICATION_DEBUG = -1234;


	public static final String SERVICE_POLLING = "polling";

	public static final int SERVICE_LOW = 0;

	public static final int SERVICE_HIGH = 1;

	public static final String INTENT_EXTRA_INTERFACE = "interface";


	private NotificationManager mNotification;


	private SharedPreferences mPreferences;

	private static int sUpdatePolicy = SERVICE_LOW;
	
//	private long mInter = 10 * 1000;


	public synchronized <T> T getAdapter(Class<T> clazz) {
        if (SharedPreferences.class == clazz) {
			if (mPreferences == null) {
				mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			}
			return clazz.cast(mPreferences);
		} else if (NotificationManager.class == clazz) {
			if (mNotification == null) {
				mNotification = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			}
			return clazz.cast(mNotification);
		}
		return null;
	}

	@Override
	public void onCreate() {


		super.onCreate();


		MyLog.d(getClass().getName(), "Application created");

	}

	@Override
	public void onTerminate() {

		MyLog.d(getClass().getName(), "Application terminated");
        super.onTerminate();
	}

	public void startService() {
//		WakefulService.acquireStaticLock(this);
//		Intent intent = new Intent(this, NetCounterService.class);
        MyLog.d("NetCounterApplication", "startService()");
		stopService(new Intent(this, NetCounterService.class));
		stopService(new Intent(this, CallDetectService.class));
		
		startService(new Intent(this, NetCounterService.class));
		startService(new Intent(this, CallDetectService.class));
	}
	
	public void stopService() {
		stopService(new Intent(this, NetCounterService.class));
		stopService(new Intent(this, CallDetectService.class));
	}

	public static synchronized void setUpdatePolicy(int updatePolicy) {
		sUpdatePolicy = updatePolicy;
	}

	public static synchronized int getUpdatePolicy() {
		return sUpdatePolicy;
	}
	
//	public long getInterUpdate() {
//		return mInter;
//	}
//	
//	public void setInterUpdate(long inter) {
//		mInter = inter;
//	}

	public void toast(int message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	public void toast(CharSequence message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	public void notifyForDebug(CharSequence message) {
		SharedPreferences preferences = getAdapter(SharedPreferences.class);
		if (preferences.getBoolean("debug", false)) {
			Notification n = new Notification();
			n.when = System.currentTimeMillis();
			n.icon = R.drawable.iconnc;
			n.tickerText = message;
			Intent i = new Intent(this, NetCounterActivity.class);
			PendingIntent p = PendingIntent.getActivity(this, 0, i, 0);
			n.setLatestEventInfo(this, "NetCounter debug", message, p);
			getAdapter(NotificationManager.class).notify(NOTIFICATION_DEBUG, n);
		}
	}

}
