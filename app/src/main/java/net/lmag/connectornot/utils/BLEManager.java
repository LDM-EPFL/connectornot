package net.lmag.connectornot.utils;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import net.lmag.connectornot.model.BleModel;
import net.lmag.connectornot.model.MyLog;
import net.lmag.connectornot.service.BleController;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
//import org.pocketcampus.plugin.ble.android.BleController;
//import org.pocketcampus.plugin.ble.android.BleModel;

public class BLEManager {
	
	
	private class RunnableScanner implements Runnable {

		private List<ScanInterval> mScans;

		public RunnableScanner(List<ScanInterval> intervals) {
			mScans = intervals;
		}

		@Override
		public void run() {
			performScan(mScans);
		}

	}
	
	//TODO saturday is 6, this is for testing.
	private final int SATURDAY = 6;
	
	private final int SUNDAY = 0;
	

	
	//Need an ArrayList for random accesses.
	private ArrayList<ScanInterval> mIntervals;
	
	private Handler mScanHandler = new Handler();
	

	
//	private boolean mBLESupported = false;
//	private final BluetoothManager mBluetoothManager;
//	
//	private final BluetoothAdapter mBluetoothAdapter;
//	
	private boolean mScanning = false;
	
//	private String mPluginName;
	
//	private BeaconManager.RangingListener mRangingCallback;
//	
//	private bleController mController;
	
	
	private Region mRegion;
	

	

	
	private Runnable mDelayedRunnable;
	
	private Runnable mScanScheduler = new Runnable() {
	    @Override 
	    public void run() {
//	    	DateTime now = DateTime.now();
//	    	int weekDay = now.getDayOfWeek();
//	    	
	    	
//	    	if (weekDay != SUNDAY && duringRushHour(now)) {
//	    		performScan();
//	    	}
	    	
//	    	MyLog.d("BLEManager DEBUG", "Starting Scheduler");
	    	scheduleNextScan();
	    	
	    	
	    	//TODO this is for debugging
//	    	performScan();
	    	
	    	
	    }
	  };
	  

		
	  private BeaconManager mBeaconManager;
	  
	  private BleModel mModel;
	  
	  private static final List<Integer> MINORS = new LinkedList<Integer>();
	  
	  static {
		  MINORS.add(Integer.valueOf(0));
		  MINORS.add(Integer.valueOf(1));
		  MINORS.add(Integer.valueOf(2));
		  MINORS.add(Integer.valueOf(3));
	  }
	  

      
      
     
	public BLEManager(Context ctx, BeaconManager.RangingListener cb, BleController controller, Region region, BleModel model) {
		
//		mRangingCallback = cb;
		
		mModel = model;


		mBeaconManager = new BeaconManager(ctx);
//		bleSupported(ctx);
		
//		mBluetoothManager = !bleSupported(ctx) ? null : 
//			(BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
//
//		mBluetoothAdapter = mBluetoothManager == null ? null : mBluetoothManager.getAdapter();
		
		
//		mBeaconManager.setMonitoringListener(cb);
		
		mBeaconManager.setRangingListener(cb);
		
		
		
		mIntervals = new ArrayList<ScanInterval>();
		
		mRegion = region;
		

		
//		mController = controller;
		
		
		
	}
	
	
	
//	private boolean bleSupported(Context ctx) {
////		mBLESupported =  (ctx == null ? false : ctx.getPackageManager().hasSystemFeature(
////				PackageManager.FEATURE_BLUETOOTH_LE)); 
//		return mBeaconManager.hasBluetooth();
//		
////		if (mBLESupported) {
////			mUI.showToast("BLE is supported.");
////		} else {
////			mUI.showToast("BLE is not supported.");
////		}
////		return mBLESupported;
//	}
//	
	public void insertIntervals(ArrayList<ScanInterval> newInter) {
		mIntervals.addAll(newInter);
		sortScans();

		if (mScanning) {
			stopScanner();
			startScanner();
		}
	}
	
	
	private void performScan(List<ScanInterval> inter) {
		MyLog.d("BLEManager", "perform scan start");

		
//		int max = ScanInterval.getMaxScan(inter);
		
		mBeaconManager.setBackgroundScanPeriod(1000, 1000);
		mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
    		@Override public void onServiceReady() {
    			try {
    				MyLog.d("BLEManager", "Started ranging region.");
    				//TODO monitor all regions.
    				mBeaconManager.startRanging(mRegion);
    			} catch (RemoteException e) {
    				MyLog.e("BLEManager", "Cannot start ranging", e);
    			}
    		}
    	});
      
        
//        setDelayedRunnable(new Runnable() {
//            @Override
//            public void run() {
////                mBluetoothAdapter.stopLeScan(mLeScanCallback/*callback*/);
////                mScanScheduler.run();
//            	try {
//            	    mBeaconManager.stopMonitoring(mRegion);
//            	    mScanScheduler.run();
//            	  } catch (RemoteException e) {
//            	    MyLog.e("BLEManager", "Cannot stop but it does not matter now", e);
//            	  }
//            }
//        });
//        
//        int msUntilEnd = ScanInterval.getMsUntilEnd(DateTime.now(), inter);
//        
//        
//        if (mBeaconManager.hasBluetooth() && mBeaconManager.isBluetoothEnabled() && mModel.getBleEnabled()) {		
//        	mScanHandler.postDelayed(mDelayedRunnable, msUntilEnd);
//        	MyLog.d("BLEManager", "will scan for: " + ScanInterval.getMsUntilEnd(DateTime.now(), inter) + " ms");
//        	mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
//        		@Override public void onServiceReady() {
//        			try {
//        				mBeaconManager.startMonitoring(mRegion);
//        			} catch (RemoteException e) {
//        				MyLog.e("BLEManager", "Cannot start ranging", e);
//        			}
//        		}
//        	});
//        } else {
//        	mScanHandler.postDelayed(mDelayedRunnable, 5 * 1000); //5s
//
//        	MyLog.d("BLEManager", "Could not perform scan");
//        }
//		
		
	}
	
	public void removeIntervals() {
//		Iterator<ScanInterval> iter = mIntervals.iterator();
//		while(iter.hasNext()) {
//			ScanInterval i = iter.next();
//			if (i.getPluginName().equals(pluginName)) {
//				iter.remove();
//			}
//		}
		mIntervals = new ArrayList<ScanInterval>();
		
		if (mScanning) {
			stopScanner();
			startScanner();
		}
	}
	
	private void weekendSchedule(DateTime now) {
		int weekDay = now.getDayOfWeek();
		
		ArrayList<ScanInterval> subSet = new ArrayList<ScanInterval>();
		ScanInterval first = mIntervals.get(0);
		subSet.add(first);
		DateTime future = now.plusDays(weekDay == SATURDAY ? 2 : 1);
		future = future.withMillisOfDay(first.getBegin());
		
		
		setDelayedRunnable(new RunnableScanner(subSet));
		mScanHandler.postDelayed(mDelayedRunnable, (long)
				Math.abs(future.getMillis() - now.getMillis()));
	}
	
	private void weekSchedule(DateTime now) {
		ArrayList<ScanInterval> subSet = ScanInterval.getCurrentIntervals(now, mIntervals);
		
		if (!subSet.isEmpty()) {
			MyLog.d("BLEManager", "Currently in interval");
			
			int minRest = ScanInterval.getMinRest(subSet);
			
			setDelayedRunnable(new RunnableScanner(subSet));
			mScanHandler.postDelayed(mDelayedRunnable,
					minRest);
		} else {
			MyLog.d("BLEManager", "Not in interval, schedule for next one");
			ScanInterval nextInterval = ScanInterval.getNextInterval(now, mIntervals);
			
			if (nextInterval != null) {
				subSet.add(nextInterval);
				
				setDelayedRunnable(new RunnableScanner(subSet));
				mScanHandler.postDelayed(mDelayedRunnable,
						(long) Math.abs(nextInterval.getBegin() - now.getMillisOfDay()));
			} else {
				MyLog.d("BLEManager", "Schedule for next day.");
				ScanInterval first = mIntervals.get(0);
				subSet.add(first);
				
				DateTime future = now.plusDays(1);
				future = future.withMillisOfDay(first.getBegin());
				
				setDelayedRunnable(new RunnableScanner(subSet));
				mScanHandler.postDelayed(mDelayedRunnable, (long)
						Math.abs(future.getMillis() - now.getMillis()));
			}
		}
		
		
	}
	
	private void scheduleNextScan() {
		MyLog.d("BLEManager", "Schedule next scan");
		
		performScan(null);
		
//		if (mIntervals.size() == 0) {
//			MyLog.d("BLEManager DEBUG", "No scan Intervals");
//			return;
//		}
		
//		DateTime now = DateTime.now();
//		int weekDay = now.getDayOfWeek();
//		int nowMilis = now.getMillisOfDay();
		
		
//		if (weekDay == SATURDAY || weekDay == SUNDAY) {
//			MyLog.d("BLEManager DEBUG", "No scans on weekends");
//			weekendSchedule(now);
//		} else {
//			weekSchedule(now);
//		}


	}
	
	private void setDelayedRunnable(Runnable r) {
		if (mDelayedRunnable != null) {
			mScanHandler.removeCallbacks(mDelayedRunnable);
		}
		mDelayedRunnable = r;
	}
	
	private void sortScans() {
		Collections.sort(mIntervals, new Comparator<ScanInterval>() {

			@Override
			public int compare(ScanInterval lhs, ScanInterval rhs) {
				long lhsBegin = lhs.getBegin();
				long rhsBegin = rhs.getBegin();
				return lhsBegin < rhsBegin ? -1
						: lhsBegin > rhsBegin ? 1
						: 0;
			}
		});
	}
	  
	public void startScanner() {
			
			mScanScheduler.run();
			mScanning = true;
		
	}
	  
	  
      public void stopScanner() {
		try {
			mScanHandler.removeCallbacks(mDelayedRunnable);

			try {
				mBeaconManager.stopRanging(mRegion);
			} catch (RemoteException e) {
				MyLog.e("BLEManager", "Cannot stop but it does not matter now", e);
			}

			mScanning = false;
//			mScanJobHandler.removeCallbacks(mScanScheduler);
		} catch (NullPointerException e) {
			//nothing special to do, it just means that mScanner was not initialized.
		}
	}


}

