package com.txtvia.android.txtvia;

import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.web.bindery.requestfactory.shared.RequestFactory;
import com.google.web.bindery.requestfactory.vm.RequestFactorySource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * Utility methods for getting the base URL for client-server communication and
 * retrieving shared preferences.
 */
public class Util {

		
	private static JSONObject jObject;
    /**
     * Tag for logging.
     */
    private static final String TAG = "Util";

    // Shared constants
    
//    private JSONObject jObject;

    /**
     * Key for account name in shared preferences.
     */
	public static final String ACCOUNT_NAME = "accountName";
    
    public static final String AUTH_TOKEN = "authentication_token";

    /**
     * Key for auth cookie name in shared preferences.
     */
    public static final String AUTH_COOKIE = "authCookie";

    /**
     * Key for device registration id in shared preferences.
     */
    public static final String DEVICE_REGISTRATION_ID = "deviceRegistrationID";
    
    public static final String DEVICE_ID = "device_id";

    /*
     * URL suffix for the RequestFactory servlet.
     */
    public static final String RF_METHOD = "/gwtRequest";

    /**
     * An intent name for receiving registration/unregistration status.
     */
    public static final String UPDATE_UI_INTENT = getPackageName() + ".UPDATE_UI";

    // End shared constants

    /**
     * Key for shared preferences.
     */
    private static final String SHARED_PREFS = "spotminder".toUpperCase(Locale.ENGLISH) + "_PREFS";

    /**
     * Cache containing the base URL for a given context.
     */
    private static final Map<Context, String> URL_MAP = new HashMap<Context, String>();
    

    /**
     * Display a notification containing the given string.
     */
    public static void generateNotification(Context context, String message) {
        int icon = R.drawable.status_icon;
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, message, when);
        notification.setLatestEventInfo(context, "TxtVia monkey...", message,
                PendingIntent.getActivity(context, 0, null, PendingIntent.FLAG_CANCEL_CURRENT));
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        SharedPreferences settings = Util.getSharedPreferences(context);
        int notificatonID = settings.getInt("notificationID", 0);

        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificatonID, notification);

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("notificationID", ++notificatonID % 32);
        editor.commit();
    }

    /**
     * Returns the (debug or production) URL associated with the registration
     * service.
     */
    public static String getBaseUrl(Context context) {
        String url = URL_MAP.get(context);
        if (url == null) {
            // if a debug_url raw resource exists, use its contents as the url
            url = getDebugUrl(context);
            // otherwise, use the production url
            if (url == null) {
                url = Setup.PROD_URL;
            }
            URL_MAP.put(context, url);
        }
        return url;
    }

    /**
     * Creates and returns an initialized {@link RequestFactory} of the given
     * type.
     */
    public static <T extends RequestFactory> T getRequestFactory(Context context,
            Class<T> factoryClass) {
        T requestFactory = RequestFactorySource.create(factoryClass);

        SharedPreferences prefs = getSharedPreferences(context);
        String authCookie = prefs.getString(Util.AUTH_COOKIE, null);

        String uriString = Util.getBaseUrl(context) + RF_METHOD;
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            Log.w(TAG, "Bad URI: " + uriString, e);
            return null;
        }
        requestFactory.initialize(new SimpleEventBus(),
                new AndroidRequestTransport(uri, authCookie));

        return requestFactory;
    }

    /**
     * Helper method to get a SharedPreferences instance.
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFS, 0);
    }

    /**
     * Returns true if we are running against a dev mode appengine instance.
     */
    public static boolean isDebug(Context context) {
        // Although this is a bit roundabout, it has the nice side effect
        // of caching the result.
        return !Setup.PROD_URL.equals(getBaseUrl(context));
    }

    /**
     * Returns a debug url, or null. To set the url, create a file
     * {@code assets/debugging_prefs.properties} with a line of the form
     * 'url=http:/<ip address>:<port>'. A numeric IP address may be required in
     * situations where the device or emulator will not be able to resolve the
     * hostname for the dev mode server.
     */
    private static String getDebugUrl(Context context) {
        BufferedReader reader = null;
        String url = null;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open("debugging_prefs.properties");
            reader = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String s = reader.readLine();
                if (s == null) {
                    break;
                }
                if (s.startsWith("url=")) {
                    url = s.substring(4).trim();
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            // O.K., we will use the production server
            return null;
        } catch (Exception e) {
            Log.w(TAG, "Got exception " + e);
            Log.w(TAG, Log.getStackTraceString(e));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "Got exception " + e);
                    Log.w(TAG, Log.getStackTraceString(e));
                }
            }
        }

        return url;
    }

    /**
     * Returns the package name of this class.
     */
    private static String getPackageName() {
        return Util.class.getPackage().getName();
    }
    
    /**
     * Get auth token.
     */
    public static String getAuthToken(Context context){
    	SharedPreferences settings = Util.getSharedPreferences(context);
		String token = settings.getString(Util.AUTH_TOKEN, null);
		if(token != null){
			return token;
		} else {
			try {
				Log.i(TAG, "requestion auth token... ");
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(Setup.PROD_URL + "/users/auth/device.json");
				List <NameValuePair> postBody = new ArrayList <NameValuePair>();
				postBody.add(new BasicNameValuePair("email",settings.getString(ACCOUNT_NAME, null)));
				postBody.add(new BasicNameValuePair("api_key", Setup.API_KEY));
				httppost.setEntity(new UrlEncodedFormEntity(postBody, HTTP.UTF_8));
//				httppost.setHeader("Accept", "application/json");
//            	httppost.setHeader("Content-type", "application/json");
            	HttpResponse response = httpclient.execute(httppost);
            	

				
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					String responseString = out.toString();
					
					
					Log.i(TAG, "got auth response from server:" + responseString);
					boolean success = true;
					if (success) {
						
						jObject = new JSONObject(responseString);
						SharedPreferences.Editor editor = settings.edit();
						
//						String token = jObject.getString("authentication_token");
						token = "7QVrpmIIa71Iy7qwlQrD";
						Log.v(TAG,"ggot token"+token);
						editor.putString(AUTH_TOKEN, token);
//						editor.putString(AUTH_TOKEN, responseString);
						return token;
						
					} 

				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (Exception e) {
				Log.w(TAG, "shit happend when sending registration ID to server", e);
			}
		}
		return token;
    }

	private static Object JSONObject(String responseString) {
		// TODO Auto-generated method stub
		return null;
	}
}
