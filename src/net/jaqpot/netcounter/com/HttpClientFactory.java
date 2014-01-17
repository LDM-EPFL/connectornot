package net.jaqpot.netcounter.com;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * TODO(louismagarshack) Write the javadoc for this class.
 * 
 * @author louismagarshack
 */
public final class HttpClientFactory {

    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;

    private static final RedirectHandler REDIRECT_NO_FOLLOW = new RedirectHandler() {

        @Override
        public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
            return false;
        }

        @Override
        public URI getLocationURI(HttpResponse response, HttpContext context)
                throws org.apache.http.ProtocolException {
            return null;
        }
    };

    private static HttpClient sHttpClient;

    /** Hide the Constructor. */
    private HttpClientFactory() {
    };

    public static synchronized HttpClient getInstance() {
        if (sHttpClient == null) {
            sHttpClient = create();
        }

        return sHttpClient;
    }

    private static HttpClient create() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", PlainSocketFactory.getSocketFactory(), HTTP_PORT));
        schemeRegistry.register(
                new Scheme("https", SSLSocketFactory.getSocketFactory(), HTTPS_PORT));
        HttpParams params = new BasicHttpParams();
        ThreadSafeClientConnManager connManager =
                new ThreadSafeClientConnManager(params, schemeRegistry);
        AbstractHttpClient result = new DefaultHttpClient(connManager, params);
        result.setRedirectHandler(REDIRECT_NO_FOLLOW);
        return result;
    }

    // For tests since Mockito cannot mock AbstractHttpClient.
    public static void setInstance(HttpClient instance) {
        sHttpClient = instance;
    }
}
