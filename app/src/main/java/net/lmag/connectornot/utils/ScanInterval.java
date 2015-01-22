package net.lmag.connectornot.utils;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class ScanInterval /*<T>*/ {
	private TimeInterval mInterval;
	private ScanFrequency mFrequency;
//	private DeviceFoundCallback mCallback;
	
	//This can be used to remove intervals from the list.
	//It is important that it should not be respected.
//	private String mPluginName;
	
	
	
	public ScanInterval(TimeInterval interval, ScanFrequency frequency/*,
			DeviceFoundCallback cb*/) {
		mInterval = interval;
		mFrequency = frequency;
//		mCallback = cb;
//		mPluginName = pluginName;
		
	}
	
	public int inInterval(DateTime time) throws TimeIntervalException {
		return mInterval.inInterval(time);
	}
	
	public int getBegin() {
		return mInterval.getBegin();
	}
	
	public int getScanDuration() {
		return mFrequency.getScanDuration();
	}
	
	public int getRestDuration() {
		return mFrequency.getRestDuration();
	}
	
//	public void executeCallback(final BluetoothDevice device, int rssi,
//			  byte[] scanRecord) {
//		mCallback.execute(device, rssi, scanRecord);
//	}
	
//	public DeviceFoundCallback getCallback() {
//		return mCallback;
//	}
	
//	public String getPluginName() {
//		return mPluginName;
//	}
	
	public static ArrayList<ScanInterval> getCurrentIntervals(DateTime now, List<ScanInterval> intervals) {
		ArrayList<ScanInterval> subSet = new ArrayList<ScanInterval>();
		int inInter = 1;
		for(ScanInterval i: intervals) {
			try {
				inInter = i.inInterval(now);
			} catch (TimeIntervalException e) {
				inInter = 1;
			}
			
			if (inInter == 0) {
				subSet.add(i);
			} else if (inInter > 0) {
				//This means we have gone too far
				break;
			}
		}
		
		return subSet;
	}
	
	public static int getMinRest(List<ScanInterval> intervals) {
		int min = 60 * 60 * 1000; //1hr in millis
		
		for(ScanInterval i: intervals) {
			if (i.getRestDuration() < min) {
				min = i.getRestDuration();
			}
		}
		
		return min;
	}
	
	public static int getMaxScan(List<ScanInterval> intervals) {
		int max = 0;
		for(ScanInterval i: intervals) {
			if (i.getScanDuration() > max) {
				max = i.getScanDuration();
			}
		}
		
		return max;
		
	}
	
	public static ScanInterval getNextInterval(DateTime now, List<ScanInterval> intervals) {
		int inInter = 0;
		for(ScanInterval i: intervals) {
			try {
				inInter = i.inInterval(now);
			} catch (TimeIntervalException e) {
				inInter = 0;
			}
			if (inInter > 0) {
				return i;
			}
		}
		
		return null;
	}
	
	public static int getMsUntilEnd(DateTime now, List<ScanInterval> intervals) {
		int max = 0;
		for(ScanInterval i: intervals) {
			if (i.getScanDuration() > max) {
				max = i.mInterval.getEnd();
			}
		}
		
		return max - now.getMillisOfDay();
	}

}
