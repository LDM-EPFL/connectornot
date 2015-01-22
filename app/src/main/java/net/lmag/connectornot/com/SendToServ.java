
package net.lmag.connectornot.com;

import android.net.TrafficStats;
import android.os.AsyncTask;

import net.lmag.connectornot.model.MyLog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SendToServ extends AsyncTask<Void, Void, String> {
	
		private static final String ENCODING = "UTF-8";
	    private static final int CONNECT_TIMEOUT = 30 * 1000; // In ms.
	    private static final int READ_TIMEOUT = 30 * 1000; // In ms.
		
		private ServApi.Handler mHandler = null;
		private Map<String, Object> mPayload = null;
		
		private static final String SERVER_ADDRESS = "http://kucjica.kucjica.org/androidtesting/testlog.php";
		private static final String TAG = "net.jaqpot.netcounter.com.SendOSC";

		public SendToServ(Map<String, Object> payload, ServApi.Handler handler) {
			mPayload = payload;
			mHandler = handler;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			String responseContent = null;
	        HttpResponse response = null;
	        StatusLine responseStatusLine = null;
	        HttpRequestBase request = new HttpPost();

	        try {
	            request = prepareRequest(request);
//	            MyLog.i(TAG, String.format("%s request to %s", request.getMethod(), request.getURI()));
                MyLog.d(TAG, "Request prepared, try to send");
                try {
                    long bytes = TrafficStats.getTotalTxBytes();
	                // Get the response as a string.
	                response = HttpClientFactory.getInstance().execute(request);
	                responseStatusLine = response.getStatusLine();
	                MyLog.d(TAG, "status = " + responseStatusLine.toString());
	                responseContent = EntityUtils.toString(response.getEntity());
	                MyLog.d(TAG, "received: " + responseContent);
	                
	                JSONObject jo = new JSONObject();
					jo.put("message", "success");
					jo.put("length", Long.toString(
                            TrafficStats.getTotalTxBytes() - bytes));
					return jo.toString();
	            } catch (IOException ioe) {
	                // Happens when the server returns an error status code.
                    MyLog.e(TAG, "got IO while sending", ioe);
	                responseContent = responseStatusLine.getReasonPhrase();
	            }



	        } catch (Exception e) {

	            MyLog.e(TAG, "caught exception while handling request", e);

	        }
			
			
			
			
			return null;
		}
		
		private HttpRequestBase prepareRequest(HttpRequestBase request)
	            throws UnsupportedEncodingException {
	        try {
	            request.setURI(new URI(/*"http:/" + InetAddress.getByName(SERVER_ADDRESS).toString()*/SERVER_ADDRESS));
	        } catch (URISyntaxException e) {
	            throw new RuntimeException(e);
	        } 

	        request.getParams().setParameter(
	                "http.connection.timeout", Integer.valueOf(CONNECT_TIMEOUT));
	        request.getParams().setParameter(
	                "http.socket.timeout", Integer.valueOf(READ_TIMEOUT));


	        if (mPayload != null && request instanceof HttpEntityEnclosingRequestBase) {
	            // Write out the request body (i.e. the form data).
	            ((HttpEntityEnclosingRequestBase) request).setEntity(
	                    new UrlEncodedFormEntity(generateQueryNVP(this.mPayload), ENCODING));
//	            ((HttpEntityEnclosingRequestBase) request).
	        }

	        return request;
	    }
		
		private static List<NameValuePair> generateQueryNVP(Map<String, Object> data) {
	        List<NameValuePair> nvp = new ArrayList<NameValuePair>();
	        for (Map.Entry<String, Object> entry : data.entrySet()) {
	            nvp.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
	        }
	        return nvp;
	    }
		
		@Override
	    protected void onPostExecute(String res) {
			if (mHandler != null) {
		        if (res != null) {
		            mHandler.callback(res);
		        } else {
		            mHandler.onError(null);
		        }
			}
	    }
		
		public static SendToServ of(Map<String, Object> payload, ServApi.Handler handler) {
			return new SendToServ(payload, handler);
		}
		

		
	}