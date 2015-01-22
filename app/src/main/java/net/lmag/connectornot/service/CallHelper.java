package net.lmag.connectornot.service;

import net.lmag.connectornot.model.MyLog;
import net.lmag.connectornot.model.PlayModeModelAPI;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Time;

public class CallHelper {
	
	

	/**
	 * Listener to detect incoming calls. 
	 */
	private class CallStateListener extends PhoneStateListener {
		
		private Time mLastStart = null;
		private boolean mInCall = false;
		
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
				case TelephonyManager.CALL_STATE_OFFHOOK:
					// called when someone is ringing to this phone
					MyLog.d("CallHelper", "We are now in a call!");
					mLastStart = new Time();
					mLastStart.setToNow();
					mInCall = true;
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					MyLog.d("CallHelper", "We are not in a call!");
					if (mInCall) {
						mInCall = false;
						Time now = new Time();
						now.setToNow();
						int diff = Time.compare(now, mLastStart == null ? now : mLastStart);
						PlayModeModelAPI.setCallDuration(diff);
						mLastStart = null;
					}
					break;
			}
		}
	}
	
	/**
	 * Broadcast receiver to detect the outgoing calls.
	 */
//	public class OutgoingReceiver extends BroadcastReceiver {
//	    public OutgoingReceiver() {
//	    }
//
//	    @Override
//	    public void onReceive(Context context, Intent intent) {
//	        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
//	        
//	        Toast.makeText(ctx, 
//	        		"Outgoing: "+number, 
//	        		Toast.LENGTH_LONG).show();
//	    }
//  
//	}

	private Context ctx;
	private TelephonyManager tm;
	private CallStateListener callStateListener;
	
//	private OutgoingReceiver outgoingReceiver;

	public CallHelper(Context ctx) {
		this.ctx = ctx;
		
		callStateListener = new CallStateListener();
//		outgoingReceiver = new OutgoingReceiver();
	}
	
	/**
	 * Start calls detection.
	 */
	public void start() {
		tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		
//		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
//		ctx.registerReceiver(outgoingReceiver, intentFilter);
	}
	
	/**
	 * Stop calls detection.
	 */
	public void stop() {
		tm.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
//		ctx.unregisterReceiver(outgoingReceiver);
	}

}
