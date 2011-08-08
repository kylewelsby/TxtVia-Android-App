package com.txtvia.android.txtvia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.wdowka.apps.spotminder.client.MyRequestFactory;
import com.wdowka.apps.spotminder.client.MyRequestFactory.RegistrationInfoRequest;
//import com.wdowka.apps.spotminder.shared.RegistrationInfoProxy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.provider.Settings.Secure;
import android.util.Log;

public class DeviceRegistrar {
	public static final String STATUS_EXTRA = "Status";

	public static final int REGISTERED_STATUS = 1;

	public static final int UNREGISTERED_STATUS = 2;

	public static final int ERROR_STATUS = 3;

	private static final String TAG = "DeviceRegistrar";

	public static void registerOrUnregister(final Context context,
			final String deviceRegistrationId, final boolean register) throws IllegalStateException, IOException {
		final Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);

		SharedPreferences prefs = Util.getSharedPreferences(context);
//		String accountName = prefs.getString(Util., null);
		String authenticationToken = Util.getAuthToken(context);
		if (register) {

			try {
				Log.i(TAG, "got registration number:" + deviceRegistrationId
						+ "  for account:" + authenticationToken);
				
            	HttpClient httpclient = new DefaultHttpClient();
            	HttpPost httppost = new HttpPost(Setup.PROD_URL + "/devices.json");
            	List <NameValuePair> deviceValues = new ArrayList <NameValuePair>();
            	deviceValues.add(new BasicNameValuePair("registration_id",deviceRegistrationId));
            	deviceValues.add(new BasicNameValuePair("auth_token",authenticationToken));
            	deviceValues.add(new BasicNameValuePair("api_key", Setup.API_KEY));
            	httppost.setEntity(new UrlEncodedFormEntity(deviceValues, HTTP.UTF_8));
//            	httppost.setEntity(new UrlEncodedFormEntity("reqistration_id="+deviceRegistrationId+"&auth_token="+Util.AUTH_TOKEN+"&api_key="+Setup.API_KEY, HTTP.UTF_8));
//            	httppost.setHeader("Accept", "application/json");
//            	httppost.setHeader("Content-type", "application/json");
            	
            	HttpResponse response = httpclient.execute(httppost);
//            	HttpEntity entity = response.getEntity();
            	
//				HttpClient httpclient = new DefaultHttpClient();
//				HttpResponse response = httpclient.execute(new HttpGet(
//						Setup.PROD_URL + "/register_device?account_name="
//								+ accountName + "&registration_id="
//								+ deviceRegistrationId
//								+ "&authentication_token="
//								+ authenticationToken));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					String responseString = out.toString();
					Log.i(TAG, "got response from server:" + responseString);
					boolean success = true;
					if (success) {
						SharedPreferences settings = Util
								.getSharedPreferences(context);
						SharedPreferences.Editor editor = settings.edit();

						if (register) {
							editor.putString(Util.DEVICE_REGISTRATION_ID,
									deviceRegistrationId);
							
						} else {
							editor.remove(Util.DEVICE_REGISTRATION_ID);
							editor.remove(Util.AUTH_TOKEN);
						}
						editor.commit();
						updateUIIntent.putExtra(STATUS_EXTRA,
								register ? REGISTERED_STATUS
										: UNREGISTERED_STATUS);
						context.sendBroadcast(updateUIIntent);
					} else {
						updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
						context.sendBroadcast(updateUIIntent);
					}

				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (Exception e) {
				Log.w(TAG,
						"Something went wrong while sending registration ID to server",
						e);
			}
		} else {

		}

	}

	private static RegistrationInfoRequest getRequest(Context context) {
		MyRequestFactory requestFactory = Util.getRequestFactory(context,
				MyRequestFactory.class);
		RegistrationInfoRequest request = requestFactory
				.registrationInfoRequest();
		return request;
	}
}
