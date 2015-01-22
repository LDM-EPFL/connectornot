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

package net.lmag.connectornot.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import net.lmag.connectornot.NetCounterApplication;
import net.lmag.connectornot.R;
import net.lmag.connectornot.activity.NetCounterActivity;
import net.lmag.connectornot.com.OSCApi;
import net.lmag.connectornot.com.ServApi;
import net.lmag.connectornot.model.MyLog;
import net.lmag.connectornot.model.NewDataModel;
import net.lmag.connectornot.model.NewModelAPI;
import net.lmag.connectornot.model.PlayModeModelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link NetCounterService} is the service responsible for regular update of the model. We have to
 * update it regularly because we get our data from the underlying Linux system and Android does not
 * provide a "shutdown event" at the moment. So we poll regularly in order not to lose too many
 * information.
 */
public class NetCounterService extends Service {
	
	public static final int NOTIFICATION_DEBUG = -1234;

    private Handler mSendHandler = new Handler();

//	private final BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			registerAlarm();
//		}
//	};

    private final Runnable mUpdateRunnable;

    {
        mUpdateRunnable = new Runnable() {
            public void run() {
                try {
                    sendData();
                } finally {
                    mSendHandler.postDelayed(mUpdateRunnable,
                            Long.valueOf(mApp.getAdapter(SharedPreferences.class).getLong("inter", 10)) * 1000);
                }

            }
        };
    }

//	private int mPollingMode = -1;

//	private boolean mWifiUpdate = false;
	
//	private boolean mShareData = true;

	private NetCounterApplication mApp;

//	private NetCounterModel mModel;

//	private NetCounterAlarm mAlarm;

//	private WifiManager mWifiManager;
		
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
//		mModel = mApp.getAdapter(NetCounterModel.class);
//		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
//		mAlarm = new NetCounterAlarm(this, OnAlarmReceiver.class);
				
//		NewModelUtils.resetModel(this, DEFAULT_LOC);

//		IntentFilter f = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
//		registerReceiver(mWifiReceiver, f);
		
		SignalStrengthListener signalStrengthListener = new SignalStrengthListener();	           
		   ((TelephonyManager)getSystemService(TELEPHONY_SERVICE))
		   	.listen(signalStrengthListener,
		   			SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
	}
	
//	@Override
//	protected void onPause() {
//		
//	}
	
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		int p = NetCounterApplication.getUpdatePolicy();


		MyLog.d("NetCounterService", "Service started");
		registerAlarm();
		
//		mShareData = preferences.getBoolean("shareData", false);


//		Handler handler = mApp.getAdapter(HandlerContainer.class).getSlowHandler();


		MyLog.d(getClass().getName(), "Service onStart -> " + p);

		
		return -1;
	}

	@Override
	public void onDestroy() {
//        unregisterReceiver(mWifiReceiver);
//        mAlarm.unregisterAlarm();

        mSendHandler.removeCallbacks(mUpdateRunnable);
		super.onDestroy();
		


		//mApp.getAdapter(HandlerContainer.class).getSlowHandler().post(mUpdateRunnable);

		
//		unregi

		MyLog.d(getClass().getName(), "Service onDestroy.");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Registers the alarm, changing the refresh rate as appropriate.
	 */
	private void registerAlarm() {
        SharedPreferences prefs = mApp.getAdapter(SharedPreferences.class);
		if (prefs.getBoolean("shareData", false)) {
            mSendHandler.removeCallbacks(mUpdateRunnable);
            mSendHandler.postDelayed(mUpdateRunnable,
                    Long.valueOf(prefs.getLong("inter", 10)) * 1000);
//			mAlarm.registerAlarm(State.HIGH);
		} else {
            mSendHandler.removeCallbacks(mUpdateRunnable);
//            mSendHandler.postDelayed(mUpdateRunnable, 10 * 60 * 1000);
//            mAlarm.unregisterAlarm();
        }
//        else {
//			mAlarm.registerAlarm(State.LOW);
//		}
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
            MyLog.d("NetCounterService", "Not sharing.");
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
			MyLog.d("DEBUG", "No Data to send");
			return;
		}
		
		String debugStr = "Sending:\n" +
                "\n/fingerprint " + modelToSend.getRPLoc() +
				"\n/sms " + modelToSend.getSMS() +
				"\n/data " + modelToSend.getBytes() +
				"\n/signal " + modelToSend.getSignalStrength();

		MyLog.d("DEBUG", debugStr);
		
//		Integer[] coordinates = {Integer.valueOf(0), Integer.valueOf(0)};
//		content.put("/cellcoordinates", Integer.valueOf(0));
		
		
//		Toast.makeText(this, SendOSC.getIP() + "\n" + debugStr
//							, Toast.LENGTH_LONG).show();
//		Notification n = new Notification();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);

        Notification n = builder.setContentIntent(
                PendingIntent.getActivity(this, 0, new Intent(this, NetCounterActivity.class), 0))
                .setSmallIcon(R.drawable.iconnc).setTicker(debugStr).setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle("ConnectOrNot").build();


		mApp.getAdapter(NotificationManager.class).notify(NOTIFICATION_DEBUG, n);
		
		
		int id = 0;
		JSONObject jo = new JSONObject();
        String idStr = Integer.toString(id);
		
		try {
			jo.accumulate(idStr, new JSONObject().put("timestamp", modelToSend.getTS()));
			jo.accumulate(idStr, new JSONObject().put("bytes", modelToSend.getBytes()));
			jo.accumulate(idStr, new JSONObject().put("conversation", modelToSend.getConvDuration()));
			jo.accumulate(idStr, new JSONObject().put("sms", modelToSend.getSMS()));
			jo.accumulate(idStr, new JSONObject().put("signalstrength", modelToSend.getSignalStrength()));
			jo.accumulate(idStr, new JSONObject().put("distance", modelToSend.getCellDistance()));
			jo.accumulate(idStr, new JSONObject().put("estimote", modelToSend.getMinor()));
			jo.accumulate(idStr, new JSONObject().put("phoneID", modelToSend.getPhoneID()));
            jo.accumulate(idStr, new JSONObject().put("fingerprint", modelToSend.getRPLoc()));

			Map<String, Object> content = new HashMap<String, Object>();
			content.put("dodo", jo);

//            MyLog.d("NetCounterService", "fingerprint = " + modelToSend.getRPLoc());

			ServApi.sendMsg(content, servHandler);


            if(mApp.getAdapter(SharedPreferences.class).getBoolean("enableOSC", false)) {
                OSCApi.broadcastMsg(content, new OSCApi.OSCHandler() {
                    //
                    public void onError(Error error) {
                        MyLog.e("DEBUG", "============================\n" +
                                "NetCounterService: Could not send.\n" +
                                "============================");

                    }

                    public void callback(String struct) {
                        try {
                            JSONObject jo = new JSONObject(struct);

                            PlayModeModelAPI.setSentBytes(Integer.valueOf(jo.getInt("length")));

                        } catch (JSONException e) {
                            MyLog.e("", "", e);
                        }

                        MyLog.d("DEBUG", "============================\n" +
                                "SENT OSC PACKET: \"" + struct + "\".\n" +
                                "============================");


                    }
                });
            }
        } catch (JSONException e) {
            MyLog.d("NetCounterService", "Malformed JSON");
        }
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
				
				PlayModeModelAPI.setSentBytes(jo.getInt("length"));
				
			} catch (JSONException e) {
				MyLog.e("", "", e);
			}
			
			MyLog.d("DEBUG", "============================\n" +
					"SENT HTTP POST PACKET: \"" + struct + "\".\n" +
					"============================");
			
			
		}
	};
	
	private class SignalStrengthListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {

//			mSignalStrength = signalStrength.getGsmSignalStrength();
			
			PlayModeModelAPI.setSignalStrength(signalStrength.getGsmSignalStrength());

			super.onSignalStrengthsChanged(signalStrength);
		}
	}

}
