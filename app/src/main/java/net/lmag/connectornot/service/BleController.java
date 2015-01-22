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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import net.lmag.connectornot.model.BeaconsToSend;
import net.lmag.connectornot.model.BleModel;
import net.lmag.connectornot.model.MyLog;
import net.lmag.connectornot.model.PlayModeModelAPI;
import net.lmag.connectornot.utils.BLEManager;
import net.lmag.connectornot.utils.ScanFrequency;
import net.lmag.connectornot.utils.ScanInterval;
import net.lmag.connectornot.utils.TimeInterval;
import net.lmag.connectornot.utils.TimeIntervalException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import org.pocketcampus.android.platform.sdk.core.PluginController;
//import org.pocketcampus.plugin.ble.android.iface.IbleController;
//import org.pocketcampus.android.platform.sdk.core.PluginController;
//import org.pocketcampus.android.platform.sdk.core.PluginModel;
//import org.pocketcampus.plugin.ble.android.com.GetMaskRequest;
//import org.pocketcampus.plugin.ble.android.com.PostBeaconRequest;
//import org.pocketcampus.plugin.ble.android.iface.IbleController;
//import org.pocketcampus.plugin.ble.android.utils.BLEManager;
//import org.pocketcampus.plugin.ble.android.utils.BeaconsToSend;
//import org.pocketcampus.plugin.ble.android.utils.MyLog;
//import org.pocketcampus.plugin.ble.android.utils.ScanFrequency;
//import org.pocketcampus.plugin.ble.android.utils.ScanInterval;
//import org.pocketcampus.plugin.ble.android.utils.TimeInterval;
//import org.pocketcampus.plugin.ble.android.utils.TimeIntervalException;
//import org.pocketcampus.plugin.ble.shared.BeaconRequest;
//import org.pocketcampus.plugin.ble.shared.BeaconState;
//import org.pocketcampus.plugin.ble.shared.MaskRequest;
//import org.pocketcampus.plugin.ble.shared.bleService.Client;
//import org.pocketcampus.plugin.ble.shared.bleService.Iface;



public class BleController extends Service {

	/**
	 *  This name must match given in the Server.java file in plugin.launcher.server.
	 *  It's used to route the request to the right server implementation.
	 */
	private String mPluginName = "ble";
	

	/**
	 * Stores reference to the Model associated with this plugin.
	 */
	private BleModel mModel;
	
	/**
	 * HTTP Clients used to communicate with the PocketCampus server.
	 * Use thrift to transport the data.
	 */
//	private Iface mClient;
	

//	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Integer MAJOR_VALUE = Integer.valueOf(28945); //random

//	private static final String FOOD_PLUGIN = "food";
	
	private int mSendAttempt = 10 * 1000;
	
	
	
	

//	
	
	private Handler mMaskHandler = new Handler();
	

	
	private Handler mSendHandler = new Handler();
	
	private Runnable mSendAllBeacons = new Runnable() {
		
		@Override
		public void run() {
//			MyLog.d("bleController", "sending beacons");
			List<Integer> toSend = BeaconsToSend.getAll();
			if(toSend != null && toSend.size() > 0) {
				MyLog.d("bleController", "Got " + toSend.size() + " beacons");
//				Map<String, Object> data = new HashMap<String, Object>();
				int mostSig = getSignificantBeacons(toSend);
				PlayModeModelAPI.setMinor(mostSig);
//				data.put("estimote", getSignificantBeacons(toSend));
//				OSCApi.broadcastMsg(data, new OSCApi.OSCHandler() {
//					//TODO update DB with data sent.
//					public void onError(Error error) {
//						MyLog.e("DEBUG", "============================\n" +
//								"NetCounterService: Could not send.\n" +
//								"============================");
//						setNextSendAllAttempt();
//						
//					}
//					
//					public void callback(String struct) {
//						try {
//							JSONObject jo = new JSONObject(struct);
////							NewDataModel.sentBytes(Integer.valueOf(jo.getInt("length")));
//							
////							mSentBytes = Integer.valueOf(jo.getInt("length"));
//							MyLog.d("bleController", "Beacons were sent!");
//							
//							PlayModeModelAPI.setSentBytes(Integer.valueOf(jo.getInt("length")));
//							
//						} catch (JSONException e) {
//							MyLog.e("", "", e);
//						}
//						
//						MyLog.d("DEBUG", "============================\n" +
//								"SENT OSC PACKET: \"" + struct + "\".\n" +
//								"============================");
//						setNextSendAllAttempt();
//						
//					}
//				});
//				new PostBeaconRequest().start(BleController.this, mClient, toSend);
			} 
			setNextSendAllAttempt();
		}
		
		private int getSignificantBeacons(List<Integer> list) {
			Map<Integer, Integer> frequencies = new HashMap<Integer, Integer>();
			
			int currentMaxKey = list.get(0);
			int currentMax = 0;
			
			frequencies.put(currentMaxKey, currentMax);
			
			int count;
			
			for(int i: list) {
				if(!frequencies.containsKey(i)) {
					frequencies.put(i, i);
				} 
				count = frequencies.get(i);
                //More recent beacons are more important.
				count += i;
				frequencies.put(i, count);
				if(count > currentMax) {
					currentMax = count;
					currentMaxKey = i;
				}
			}
			
			MyLog.d("bleController", "the most frequent beacon was " + currentMaxKey);
			
			return currentMaxKey;
		}
		
	};
	
	
	
    @Override
    public IBinder onBind(Intent i) { return null; }

	@Override
	public void onCreate() {
		
		MyLog.d("bleController", "Creating BleController instance.");

        if(!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            MyLog.d("BleController", "BLE not supported");
            return;
        }
		
		mModel = new BleModel(getApplicationContext());
//		mClient = (Iface) getClient(new Client.Factory(), mPluginName);
		
//		mFetchMask.run();
		startScanner(getApplicationContext(), "food", null, mRangingListener);
		
		setNextSendAllAttempt();

		
//		ByteBuffer buf = ByteBuffer.allocate(180);
//		for(int i = 0; i < 85; i++) {
//			buf.put((byte)0);
//		}
//		for(int i = 0; i < 20; i++) {
//			buf.put((byte)255);
//		}
//		for(int i = 0; i < 75; i++) {
//			buf.put((byte)0);
//		}
//		
//		MyLog.d("bleController", buf.toString());
//		
//		ArrayList<ScanInterval> ret = bitmaskToIntervals(buf);
//		
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		MyLog.d("bleController", "bitmask has been parsed.");
//		MyLog.d("bleController", "size = " + ret.size());
//		MyLog.d("bleController", "inter = ");
		
	}

	
	@Override public void onDestroy() {
		MyLog.d("BleController", "getting destroyed!");
		stopScanner("food");
	}
	
//	public void setMask(String pluginName, byte[] mask) {
//		MyLog.d("bleController", "Setting up-mask");
//		mModel.storeBitMask(mask);
//		BLEManager mgr = mModel.getManager(pluginName);
//		if(mgr == null) {
//			startScanner(getApplicationContext(), pluginName, bitmaskToIntervals(mask), mFoodMonitoringListener);
//		}
////		setNewInter(mgr, bitmaskToIntervals(mask));
//		
//	}
	
	public void setNextAttemptTime(int delay) {
		mSendAttempt = delay;
		
		
		
		setNextSendAllAttempt();
	}
//	
//	public void sendFail(List<BeaconRequest> evs) {
//		BeaconsToSend.addAll(new LinkedList<BeaconRequest>(evs));
//		
//		setNextSendAllAttempt();
//	}
	
	private void setNextSendAllAttempt() {
//		MyLog.d("bleController", "Next attempt at sending beacons in: " + mSendAttempt);
		
		mSendHandler.removeCallbacks(mSendAllBeacons);
		mSendHandler.postDelayed(mSendAllBeacons, mSendAttempt);
	}
	
	private void setNewInter(BLEManager mgr, ArrayList<ScanInterval> inters) {
		mgr.removeIntervals();
		if(inters != null) {
			mgr.insertIntervals(inters);
		}
		mgr.startScanner();
	}
	
	public void stopScanner(String pluginName) {
        if(mModel != null) {
            BLEManager oldManager = mModel.removeManager(pluginName);
            if (oldManager != null) {
                oldManager.stopScanner();
            }
        }
	}
	
	private void startScanner(Context ctx,
			String pluginName,
			ArrayList<ScanInterval> intervals,
			BeaconManager.RangingListener cb) {
		BLEManager oldManager = mModel.getManager(pluginName);
		if (oldManager != null) {
//			oldManager.stopScanner();
			setNewInter(oldManager, intervals);
		} else {
			BLEManager newManager = new BLEManager(ctx, cb, this,
					new Region("rid", null, null/*Integer.valueOf(MAJOR_VALUE)*/, null), mModel);
			setNewInter(newManager, intervals);
			mModel.addManager(pluginName, newManager);
		}
	}
	
//	public void startFoodScanner(Context ctx) {
//		try {
//			ScanInterval myInt;
//			
//			myInt = new ScanInterval(new TimeInterval("11:30:00", "23:50:00"),
//					new ScanFrequency(5000, 1000));
//
//			ArrayList<ScanInterval> list = new ArrayList<ScanInterval>();
//			list.add(myInt);
//
//			startScanner(ctx, "food", list, mFoodMonitoringListener);
//					/* new BeaconManager.RangingListener() {
//				
//				@Override
//				public void onBeaconsDiscovered(Region arg0, List<Beacon> arg1) {
//					if (arg1 != null && arg1.size() > 0 ) {
//						for(Beacon b: arg1) {
//							MyLog.d("bleController", "got beacon major = " + b.getMajor() + " minor = " + b.getMinor());
//							postBeacon(b, PLUGIN_MINOR_MAP.get(b.getMinor()));
////							if (b.getMajor() == MAJOR_VALUE) {
////								postBeacon(b, PLUGIN_MINOR_MAP.get(b.getMinor()));
////							}
//						}
//					}
//					
//				}
//
////				@Override
////				public void onEnteredRegion(Region arg0, List<Beacon> arg1) {
////					if (arg1 != null && arg1.size() > 0 ) {
////						for(Beacon b: arg1) {
////							MyLog.d("bleController", "got beacon major = " + b.getMajor() + " minor = " + b.getMinor());
////							postBeacon(b, PLUGIN_MINOR_MAP.get(b.getMinor()));
//////							if (b.getMajor() == MAJOR_VALUE) {
//////								postBeacon(b, PLUGIN_MINOR_MAP.get(b.getMinor()));
//////							}
////						}
////					}
////					
////				}
////
////				@Override
////				public void onExitedRegion(Region arg0) {
////					// TODO Auto-generated method stub
////					
////				}
//			});*/
//			
//		} catch (TimeIntervalException e) {
//			
//		}
//		
//	}
	
	private final BeaconManager.RangingListener mRangingListener = new BeaconManager.RangingListener() {

//		int mMinor;

		@Override
		public void onBeaconsDiscovered(Region arg0, List<Beacon> arg1) {
//			MyLog.d("bleController", "Entered region of " + arg1.size() + " beacons.");

			
			for(Beacon b: arg1) {
//				MyLog.d("bleController", "minor = " + b.getMinor());
				BeaconsToSend.add(Integer.valueOf(b.getMinor()));
			}
			//			for()

//			if(arg1 != null && arg1.size() > 0) {
//				mMinor = arg1.get(0).getMinor();
//				BeaconsToSend.add(Integer.valueOf(mMinor));
//				//				BeaconsToSend.add(
//				//						new BeaconRequest(mMinor,
//				//								mModel.getID(), DateTime.now().getMillis(), BeaconState.IN, /*"food",*/ Integer.valueOf(arg0.getMajor())));
//			}

		}

	};
	
	private final BeaconManager.MonitoringListener mFoodMonitoringListener = 
			new BeaconManager.MonitoringListener() {
		
//		LinkedBlockingQueue<Integer> mMinors = new LinkedBlockingQueue<Integer>();
		
		int mMinor;
		
		@Override
		public void onExitedRegion(Region arg0) {
			MyLog.d("bleController", "Exited region.");
//			BeaconsToSend.add(Integer.valueOf(mMinor));
			//TODO
//			BeaconsToSend.add(
//					new BeaconRequest(mMinor,
//							mModel.getID(), DateTime.now().getMillis(), BeaconState.OUT, /*"food",*/ Integer.valueOf(arg0.getMajor())));
		}
		
		@Override
		public void onEnteredRegion(Region arg0, List<Beacon> arg1) {
			MyLog.d("bleController", "Entered region of " + arg1.size() + " beacons.");
			
//			for()
			
			if(arg1 != null && arg1.size() > 0) {
				mMinor = arg1.get(0).getMinor();
				BeaconsToSend.add(Integer.valueOf(mMinor));
//				BeaconsToSend.add(
//						new BeaconRequest(mMinor,
//								mModel.getID(), DateTime.now().getMillis(), BeaconState.IN, /*"food",*/ Integer.valueOf(arg0.getMajor())));
			}
			
		}
	};
	
	
//	private void postBeacon(Beacon beacon, String pluginName) {
//		if (beacon == null || pluginName == null) {
//			return;
//		}
//		MyLog.d("bleController DEBUG", "posting new beacon with attributes:\n" +
//				"deviceID = " + beacon.getDeviceId() + "\n" + 
//				"tagName = " + beacon.getTagName() + "\n" +
//				"timeStamp = " + beacon.getTimestamp());
//		SendBeacon sb = new SendBeacon(Integer.toString(beacon.getMinor()), mModel.getID(), st);
//		new PostBeaconRequest().start(this,
//				(Iface) getClient(new Client.Factory(), mPluginName),
//				new BeaconRequest(sb, pluginName));
//	}
	
	private ArrayList<ScanInterval> bitmaskToIntervals(byte[] buf) {
//		buf.rewind();
		
		StringBuilder sb = new StringBuilder();
		
		for(byte b: buf) {
			sb.append(b);
		}
		
		MyLog.d("bleController", "Converting bitmask to intervals..." + sb.toString());
		
		
		ArrayList<ScanInterval> ret = new ArrayList<ScanInterval>();
		
		ScanInterval myInt;
//		int i = 0;
		boolean inInterval = false;
		String begin = null;
		String end = null;
		ScanFrequency freq = new ScanFrequency(5000, 1000);
		int k = 0;
		
		for(int i = 0; i < buf.length; i++) {
			byte b = buf[i];
			for(int j = 7; j >= 0; --j) {
				k = i * 8 + 7 - j;
				if(((b >>> j) & ((byte)1)) == ((byte)1)) {
					if (!inInterval) {
						inInterval = true;
						begin = minuteToTime(k);
						MyLog.d("bleController", "now in interval, begin = " + begin);
					}
				} else {
					if(inInterval) {
						inInterval = false;
						end = minuteToTime(k);
						MyLog.d("bleController", "exiting interval, end = " + end);
						try {
							myInt = new ScanInterval(new TimeInterval(begin, end), freq);
							ret.add(myInt);
						} catch (TimeIntervalException e) {
							
						}
					}
				}
			}
			
//			i++;
		}
		
		//just to be sure.
		if(inInterval) {
			try {
				myInt = new ScanInterval(new TimeInterval(begin, "23:59:59"), freq);
				ret.add(myInt);
			} catch (TimeIntervalException e) {
				
			}
		}
		
		for(ScanInterval i : ret) {
			MyLog.d("bleController", "interval begins at " + i.getBegin() + " ms");
		}
		
		return ret;
	}
	
	private String minuteToTime(int i) {
		return (i / 60) + ":" + (i % 60) + ":00";
	}

}
