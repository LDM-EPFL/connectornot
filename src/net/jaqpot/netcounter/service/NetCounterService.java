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

package net.jaqpot.netcounter.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import net.jaqpot.netcounter.HandlerContainer;
import net.jaqpot.netcounter.NetCounterApplication;
import net.jaqpot.netcounter.R;
import net.jaqpot.netcounter.activity.NetCounterActivity;
import net.jaqpot.netcounter.com.OSCApi;
import net.jaqpot.netcounter.model.Counter;
import net.jaqpot.netcounter.model.Device;
import net.jaqpot.netcounter.model.Interface;
import net.jaqpot.netcounter.model.NetCounterModel;
import net.jaqpot.netcounter.model.NewDataModel;
import net.jaqpot.netcounter.model.NewModelAPI;
import net.jaqpot.netcounter.model.PlayModeModelAPI;
import net.jaqpot.netcounter.service.NetCounterAlarm.State;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * {@link NetCounterService} is the service responsible for regular update of the model. We have to
 * update it regularly because we get our data from the underlying Linux system and Android does not
 * provide a "shutdown event" at the moment. So we poll regularly in order not to lose too many
 * information.
 */
public class NetCounterService extends WakefulService {
	
	public static final int NOTIFICATION_DEBUG = -1234;

	private final BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			registerAlarm();
		}
	};

	private final Runnable mUpdateRunnable = new Runnable() {
		public void run() {
			try {
				// Only updates if the model is loaded.
				if (mModel.isLoaded()) {
					updateInterfaceData();
					sendData();
				}
			} finally {
				// Releases the local wake lock.
				releaseLocalLock();
			}
		}
	};

	private int mPollingMode = -1;

	private boolean mWifiUpdate = false;
	
//	private boolean mShareData = true;

	private NetCounterApplication mApp;

	private NetCounterModel mModel;

	private NetCounterAlarm mAlarm;

	private WifiManager mWifiManager;
		
//	private int mSentBytes = 0;
//	private NewDataModel mCurrentModel = null;	
	
//	private final double DEFAULT_LAT = 0.0;
//	private final double DEFAULT_LON = 0.0;
//	private Location DEFAULT_LOC = new Location("");
	
//	private int mSignalStrength = 0;
	

	@Override
	public void onCreate() {
		super.onCreate();
		
//		DEFAULT_LOC.setLatitude(DEFAULT_LAT);
//		DEFAULT_LOC.setLongitude(DEFAULT_LON);

		mApp = (NetCounterApplication) getApplication();
		mModel = mApp.getAdapter(NetCounterModel.class);
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		mAlarm = new NetCounterAlarm(this, OnAlarmReceiver.class);
				
//		NewModelUtils.resetModel(this, DEFAULT_LOC);

		IntentFilter f = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
		registerReceiver(mWifiReceiver, f);
		
		SignalStrengthListener signalStrengthListener = new SignalStrengthListener();	           
		   ((TelephonyManager)getSystemService(TELEPHONY_SERVICE))
		   	.listen(signalStrengthListener,
		   			SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
	}
	
	

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		int p = NetCounterApplication.getUpdatePolicy();

		// Tweak to check if the preference changed. TODO Remove this hack.
		SharedPreferences preferences = mApp.getAdapter(SharedPreferences.class);
		boolean wifiUpdate = preferences.getBoolean("wifiUpdate", false);
		if (mPollingMode != p || wifiUpdate != mWifiUpdate) {
			mPollingMode = p;
			mWifiUpdate = wifiUpdate;
			registerAlarm();
		}
		
//		mShareData = preferences.getBoolean("shareData", false);

		Handler handler = mApp.getAdapter(HandlerContainer.class).getSlowHandler();
		handler.removeCallbacks(mUpdateRunnable);
		handler.post(mUpdateRunnable);

		if (NetCounterApplication.LOG_ENABLED) {
			Log.d(getClass().getName(), "Service onStart -> " + p);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mApp.getAdapter(HandlerContainer.class).getSlowHandler().post(mUpdateRunnable);

		if (NetCounterApplication.LOG_ENABLED) {
			Log.d(getClass().getName(), "Service onDestroy.");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Registers the alarm, changing the refresh rate as appropriate.
	 */
	private void registerAlarm() {
		if (mPollingMode == NetCounterApplication.SERVICE_HIGH) {
			mAlarm.registerAlarm(State.HIGH);
		} else {
			switch (mWifiManager.getWifiState()) {
			case WifiManager.WIFI_STATE_ENABLED:
				// Checks if the WiFi "tweak" if enabled.
				SharedPreferences preferences = mApp.getAdapter(SharedPreferences.class);
				if (preferences.getBoolean("wifiUpdate", false)) {
					mAlarm.registerAlarm(State.LOW);
				} else {
					mAlarm.registerAlarm(State.HIGH);
				}
				break;
			case WifiManager.WIFI_STATE_DISABLED:
				mAlarm.registerAlarm(State.LOW);
				break;
			}
		}
	}
	
//	private NewDataModel getDataToSend() {
//		//Most up to date data
//		NewDataModel newData = new NewDataModel(
//				NewModelUtils.getBytes(this) - mSentBytes,
//				NewModelUtils.getCallDuration(this),
//				NewModelUtils.getSmsCount(this),
//				mSignalStrength,
//				NewModelUtils.getDistance(this));
//		
//		//Most recent entry in database
//		NewDataModel prevData = NewModelUtils.selectLastModel(this);
//		
//		NewDataModel modelToSend = (prevData != null) ? newData.subtract(prevData) : newData;
////		
////		mCurrentModel = newData;
//		
//		NewModelUtils.insertModel(this, newData);
//		
//		return modelToSend;
//	}
	
	private void sendData() {
		
		if (!mApp.getAdapter(SharedPreferences.class).getBoolean("shareData", false)) {
			return;
		}
		

		// Update data
//		mCurrentModel = NewDataModel.getDiffToStart(new NewDataModel((int)(
//				TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()),
//				NewModelUtils.getCallDuration(this),
//				NewModelUtils.getSmsCount(this),
//				(int) 99,
//				NewModelUtils.getDistance(this, DEFAULT_LOC)));
		
		NewDataModel modelToSend = NewModelAPI.getDataToSend(this);
		
		if (modelToSend == null) {
			Log.d("DEBUG", "No Data to send");
			return;
		}
		
		Map <String, Object> content = new HashMap<String, Object>();
		content.put("/conversation", modelToSend.getConvDuration());
		content.put("/sms", modelToSend.getSMS());
		content.put("/data", modelToSend.getBytes());
		content.put("/signal", modelToSend.getSignalStrength());
		content.put("/celldistance", modelToSend.getCellDistance());
		
		Log.d("DEBUG", "Sending:\n" +
				"/conversation " + modelToSend.getConvDuration() +
				"\n/sms " + modelToSend.getSMS() +
				"\n/data " + modelToSend.getBytes());
		
//		Integer[] coordinates = {Integer.valueOf(0), Integer.valueOf(0)};
//		content.put("/cellcoordinates", Integer.valueOf(0));
		
		String debugStr = "Sending:\n" +
				"/conversation " + modelToSend.getConvDuration() +
				"\n/sms " + modelToSend.getSMS() +
				"\n/data " + modelToSend.getBytes() +
				"\n/signal " + modelToSend.getSignalStrength();
		
//		Toast.makeText(this, SendOSC.getIP() + "\n" + debugStr
//							, Toast.LENGTH_LONG).show();
		Notification n = new Notification();
		n.when = System.currentTimeMillis();
		n.icon = R.drawable.icon;
		n.tickerText = debugStr;
		Intent i = new Intent(this, NetCounterActivity.class);
		PendingIntent p = PendingIntent.getActivity(this, 0, i, 0);
		n.setLatestEventInfo(this, "ConnectOrNot debug", debugStr, p);
		mApp.getAdapter(NotificationManager.class).notify(NOTIFICATION_DEBUG, n);
		
		OSCApi.broadcastMsg(content, new OSCApi.Handler() {
			//TODO update DB with data sent.
			public void onError(Error error) {
				Log.e("DEBUG", "============================\n" +
						"NetCounterService: Could not send.\n" +
						"============================");
				
			}
			
			public void callback(String struct) {
				try {
					JSONObject jo = new JSONObject(struct);
//					NewDataModel.sentBytes(Integer.valueOf(jo.getInt("length")));
					
//					mSentBytes = Integer.valueOf(jo.getInt("length"));
					
					PlayModeModelAPI.setSentBytes(Integer.valueOf(jo.getInt("length")));
					
				} catch (JSONException e) {
					Log.e("", "", e);
				}
				
				Log.d("DEBUG", "============================\n" +
						"SENT OSC PACKET: \"" + struct + "\".\n" +
						"============================");
				
				
			}
		});
		
	}

	private void updateInterfaceData() {
		Device device = Device.getDevice();
		String bluetooth = device.getBluetooth();
		String[] interfaces = device.getInterfaces();
		// Here comes the real job.
		mApp.notifyForDebug("Updating database...");
		StringBuilder sbNotify = new StringBuilder();
		for (int i = 0; i < interfaces.length; i++) {
			String inter = interfaces[i];
			// Skip the Bluetooth interface is not available.
			if (inter.equals(bluetooth) && !SysClassNet.isUp(inter)) {
				continue;
			}
			try {
				long t1 = System.currentTimeMillis();
				// Reads the values.
				long rx = SysClassNet.getRxBytes(inter);
				long tx = SysClassNet.getTxBytes(inter);

				long t2 = System.currentTimeMillis();
				
							

				Interface tmp = mModel.getInterface(inter);
				tmp.updateBytes(rx, tx);
				mModel.commit();

				long t3 = System.currentTimeMillis();

				// Logs only in debug mode.
				sbNotify.append(inter + " done in " + (t3 - t1) + " ms.\n");
				if (NetCounterApplication.LOG_ENABLED) {
					Calendar c = Calendar.getInstance();
					StringBuilder sb = new StringBuilder();
					sb.append("[").append(inter).append("] on ").append(c.getTime())
							.append(" rx: ").append(rx).append(" tx: ").append(tx)
							.append(" Read: ").append(t2 - t1).append(" ms Update: ").append(
									t3 - t2).append(" ms");
					Log.d(getClass().getName(), sb.toString());
				}
			} catch (IOException e) {
				Log.e(getClass().getName(), "I/O Error", e);
			}
		}
		// Notification for debugging.
		mApp.notifyForDebug(sbNotify);

		long t1 = System.currentTimeMillis();
		// Checks if alarms have to be triggered.
		checkAlert();
		long t2 = System.currentTimeMillis();

		// Logs only in debug mode.
		if (NetCounterApplication.LOG_ENABLED) {
			StringBuilder sb = new StringBuilder();
			sb.append("Alert: ").append(t2 - t1).append(" ms");
			Log.d(getClass().getName(), sb.toString());
		}
	}

	private void checkAlert() {
		NotificationManager notify = mApp.getAdapter(NotificationManager.class);
		for (Interface provider : mModel.getInterfaces()) {
			for (Counter c : provider.getCounters()) {
				int id = (int) ((provider.getId() << 10) + c.getId());
				String a = c.getProperty(Counter.ALERT_BYTES);
				long alert = 0;
				if (a != null) {
					alert = Long.valueOf(a);
				}
				if (alert > 0) {
					long[] bytes = c.getBytes();
					long total = bytes[0] + bytes[1];
					if (total > alert) {
						String inter = provider.getPrettyName();

						Notification n = new Notification();
						n.when = System.currentTimeMillis();
						n.icon = R.drawable.icon;
						n.flags = Notification.FLAG_NO_CLEAR;
						n.tickerText = getResources().getString(R.string.notifyExceedTitle, inter);

						// The PendingIntent to launch our activity if the user
						// selects this notification
						PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
								new Intent(this, NetCounterActivity.class), 0);

						String av = c.getProperty(Counter.ALERT_VALUE);
						String au = c.getProperty(Counter.ALERT_UNIT);

						if (av != null && au != null) {
							String s = getResources().getString(R.string.notifyExceed, inter, av,
									NetCounterApplication.BYTE_UNITS[Integer.valueOf(au)],
									c.getTypeAsString());

							n.setLatestEventInfo(this, getText(R.string.appName), s, contentIntent);

							notify.notify(id, n);
						}
					} else {
						notify.cancel(id);
					}
				} else {
					notify.cancel(id);
				}
			}
		}
	}
	
	private class SignalStrengthListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {

//			mSignalStrength = signalStrength.getGsmSignalStrength();
			
			PlayModeModelAPI.setSignalStrength(signalStrength.getGsmSignalStrength());

			super.onSignalStrengthsChanged(signalStrength);
		}
	}

}
