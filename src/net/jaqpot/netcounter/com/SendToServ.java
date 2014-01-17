
package net.jaqpot.netcounter.com;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import android.os.AsyncTask;
import android.util.Log;

public class SendToServ extends AsyncTask<Void, Void, String> {
	
		private static final String ENCODING = "UTF-8";
	    private static final int CONNECT_TIMEOUT = 30 * 1000; // In ms.
	    private static final int READ_TIMEOUT = 30 * 1000; // In ms.
		
		private OSCApi.Handler mHandler;
		private Map<String, Object> mPayload;
		
		private static final String SERVER_IP = "92.243.4.78";
		private static String mIP = "255.255.255.255";
		private static final String TAG = "net.jaqpot.netcounter.com.SendOSC";

		public SendToServ(Map<String, Object> payload, OSCApi.Handler handler) {
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
//	            Log.i(TAG, String.format("%s request to %s", request.getMethod(), request.getURI()));
	            try {
	                // Get the response as a string.
	                response = HttpClientFactory.getInstance().execute(request);
	                responseStatusLine = response.getStatusLine();
//	                Log.d(TAG, "status = " + responseStatusLine.toString());
	                responseContent = EntityUtils.toString(response.getEntity());
	                Log.d(TAG, "received: " + responseContent);
	                
	                JSONObject jo = new JSONObject();
					jo.put("message", "success");
					jo.put("length", /*Long.valueOf(request.get).toString()*/ 0);
					return jo.toString();
	            } catch (IOException ioe) {
	                // Happens when the server returns an error status code.
	                responseContent = responseStatusLine.getReasonPhrase();
	            }

//	            int status = responseStatusLine.getStatusCode();
//	            if (status < SUCCESS_RANGE_START || status > SUCCESS_RANGE_STOP) {
//	                // We didn't receive a 2xx status code - we treat it as an
//	                // error.
//	                JsonStruct.Error jsonError = GSON.fromJson(responseContent, JsonStruct.Error.class);
//	                return new Result<T>(new UnisonAPI.Error(status,
//	                        responseStatusLine.toString(), responseContent, jsonError));
//	            } else {
//	                // Success.
//	                T jsonStruct = GSON.fromJson(responseContent, this.mClassOfT);
//	                return new Result<T>(jsonStruct);
//	            }

	        } catch (Exception e) {
	            // Under this catch-all, we mean:
	            // - IOException, thrown by most HttpURLConnection methods,
	            // - NullPointerException. if there's not InputStream nor
	            // ErrorStream,
	            // - JsonSyntaxException, if we fail to decode the server's
	            // response.
	            Log.e(TAG, "caught exception while handling request", e);
	            int statusCode = 0;
	            String statusMessage = "";
	            try {
	                statusCode = responseStatusLine.getStatusCode();
	                statusMessage = responseStatusLine.getReasonPhrase();
	            } catch (Exception foobar) {
	                Log.i(TAG, "execute(): couldn't even get status code or reason phrase", foobar);
	            }

//	            return new Result<T>(new UnisonAPI.Error(
//	                    statusCode, statusMessage, responseContent, e));
	        }
			
			
			
			
			return null;
		}
		
		private HttpRequestBase prepareRequest(HttpRequestBase request)
	            throws UnsupportedEncodingException {
	        try {
	            request.setURI(new URI("http:/" + InetAddress.getByName(SERVER_IP).toString()));
	        } catch (URISyntaxException e) {
	            throw new RuntimeException(e);
	        } catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        // Configure some sensible defaults. Timeout setting could also be done
	        // in client,
	        // we choose to keep it here for better readability.
	        request.getParams().setParameter(
	                "http.connection.timeout", Integer.valueOf(CONNECT_TIMEOUT));
	        request.getParams().setParameter(
	                "http.socket.timeout", Integer.valueOf(READ_TIMEOUT));


	        if (mPayload != null && request instanceof HttpEntityEnclosingRequestBase) {
	            // Write out the request body (i.e. the form data).
	            ((HttpEntityEnclosingRequestBase) request).setEntity(
	                    new UrlEncodedFormEntity(generateQueryNVP(this.mPayload), ENCODING));
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
	        if (res != null) {
	            mHandler.callback(res);
	        } else {
	            mHandler.onError(null);
	        }
	    }
		
		public static SendToServ of(Map<String, Object> payload, OSCApi.Handler handler) {
			return new SendToServ(payload, handler);
		}
		
		public static void setIP(String ip) {
			mIP = ip;
		}
		
		public static String getIP() {
			return mIP;
		}

		
	}