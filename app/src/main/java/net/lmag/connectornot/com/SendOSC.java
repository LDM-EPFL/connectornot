
package net.lmag.connectornot.com;

import android.os.AsyncTask;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import net.lmag.connectornot.model.MyLog;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

public class SendOSC extends AsyncTask<Void, Void, String> {
		
		private OSCApi.OSCHandler mHandler;
		private Map<String, Object> mPayload;
		
//		private static final String SERVER_IP = "92.243.4.78";
		private static String mIP = "255.255.255.255";
		private static final String TAG = "net.jaqpot.netcounter.com.SendOSC";

		public SendOSC(Map<String, Object> payload, OSCApi.OSCHandler handler) {
			mPayload = payload;
			mHandler = handler;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			OSCPortOut sender1;
			OSCPortOut sender2;
			try {
				
				sender1 = new OSCPortOut(InetAddress.getByName(mIP), 50000);
				//sender2 = new OSCPortOut(InetAddress.getByName(mIP), 49999);
					
				OSCBundle bundle = new OSCBundle();
				
				long bundleSize = 0;
				
				for(String s: mPayload.keySet()) {
					OSCMessage msg = new OSCMessage();
					msg.setAddress(s);
					msg.addArgument(mPayload.get(s));
					bundle.addPacket(msg);
					bundleSize += msg.getByteArray().length;
				}
				
//				OSCMessage msg = new OSCMessage();
//				msg.setAddress("/cellcoordinates");
//				msg.addArgument(Integer.valueOf(1));
//				bundle.addPacket(msg);
				
				
				try {
					MyLog.d("DEBUG", "sending to " + mIP);

					sender1.send(bundle);
					//sender2.send(bundle);
					bundleSize = bundle.getByteArray().length;
					JSONObject jo = new JSONObject();
					jo.put("message", "success");
					jo.put("length", Long.valueOf(bundleSize * 2).toString());
					return jo.toString();
				} catch (Exception e) {
					MyLog.e(TAG, "could not send", e);
				}
			} catch (UnknownHostException e) {
				MyLog.e(TAG, "could not send", e);
			} catch (SocketException e) {
				MyLog.e(TAG, "could not send", e);
			}
			return null;
		}
		
		@Override
	    protected void onPostExecute(String res) {
	        if (res != null) {
	            mHandler.callback(res);
	        } else {
	            mHandler.onError(null);
	        }
	    }
		
		public static SendOSC of(Map<String, Object> payload, OSCApi.OSCHandler handler) {
			return new SendOSC(payload, handler);
		}
		
		public static void setIP(String ip) {
			mIP = ip;
		}
		
		public static String getIP() {
			return mIP;
		}

		
	}