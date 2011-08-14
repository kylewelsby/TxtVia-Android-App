package com.txtvia.android.txtvia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

//import com.wdowka.apps.spotminder.client.MyRequestFactory;
//import com.wdowka.apps.spotminder.client.MyRequestFactory.RegistrationInfoRequest;
//import com.wdowka.apps.spotminder.shared.RegistrationInfoProxy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DeviceRegistrar {
	public static final String STATUS_EXTRA = "Status";

	public static final int REGISTERED_STATUS = 1;

	public static final int UNREGISTERED_STATUS = 2;

	public static final int ERROR_STATUS = 3;

	private static final String TAG = "DeviceRegistrar";

	public static final int DEVICE_READY = 4;

	public static void register(final Context context,
			final String deviceRegistrationId, final boolean register) {
		final Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);

		SharedPreferences prefs = Util.getSharedPreferences(context);
//		String accountName = prefs.getString(Util., null);
		TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

		String authenticationToken = prefs.getString(Util.AUTH_TOKEN, null);;
		if (register) {

			try {
				Log.i(TAG, "got registration number:" + deviceRegistrationId
						+ "  for account:" + authenticationToken);
				
            	HttpClient httpclient = new DefaultHttpClient();
            	HttpPost httppost = new HttpPost(Setup.PROD_URL + "/devices");
            	List <NameValuePair> deviceValues = new ArrayList <NameValuePair>();
            	deviceValues.add(new BasicNameValuePair("registration_id",deviceRegistrationId));
            	deviceValues.add(new BasicNameValuePair("unique_id",manager.getDeviceId()));
            	deviceValues.add(new BasicNameValuePair("type","android"));
            	deviceValues.add(new BasicNameValuePair("name",android.os.Build.MANUFACTURER +" "+ android.os.Build.MODEL));
            	deviceValues.add(new BasicNameValuePair("carrier",manager.getNetworkOperatorName()));

//            	deviceValues.add(new BasicNameValuePair("auth_token",authenticationToken));
            	deviceValues.add(new BasicNameValuePair("email",prefs.getString(Util.ACCOUNT_NAME, null)));
            	deviceValues.add(new BasicNameValuePair("api_key", Setup.API_KEY));
            	
            	httppost.setEntity(new UrlEncodedFormEntity(deviceValues, HTTP.UTF_8));
            	httppost.setHeader("Accept", "application/json");
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
				if (statusLine.getStatusCode() == HttpStatus.SC_OK || statusLine.getStatusCode() == HttpStatus.SC_CREATED) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					String responseString = out.toString();
					Log.i(TAG, "got response from server:" + responseString);

					SharedPreferences.Editor editor = prefs.edit();
					JSONObject jObject = new JSONObject(responseString);
					JSONObject device = jObject.getJSONObject("device");
					Integer device_id = device.getInt("id");
					editor.putInt(Util.DEVICE_ID, device_id);
					editor.commit();
					
					updateUIIntent.putExtra(STATUS_EXTRA, REGISTERED_STATUS);
					context.sendBroadcast(updateUIIntent);

				} else {
					updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
				context.sendBroadcast(updateUIIntent);

			} catch (Exception e) {
				Log.w(TAG,"Something went wrong while sending registration ID to server",e);
			}
		} else {

		}

	}
	
	public static void unregister(final Context context){
		Log.i(TAG,"Unregistering Device");
		final Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);
		SharedPreferences prefs = Util.getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
		try{
			int deviceID = prefs.getInt(Util.DEVICE_ID, 0);
			String authenticationToken = prefs.getString(Util.AUTH_TOKEN, null);
			HttpClient httpclient = new DefaultHttpClient();
			HttpDelete httpdelete = new HttpDelete(Setup.PROD_URL + "/devices/"+deviceID+"&?auth_token="+authenticationToken+"&api_key="+Setup.API_KEY);
			httpdelete.setHeader("Accept", "application/json");
			HttpResponse response = httpclient.execute(httpdelete);
			if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
				Log.w(TAG, "Unregistration error" + response.getStatusLine().getStatusCode());	
			}
		} catch (Exception e) {
            Log.w(TAG, "Unregistration error " + e.getMessage());	
		}finally{
			Log.w(TAG, "Unregistration Removing elements");
			editor.remove(Util.DEVICE_REGISTRATION_ID);
			editor.remove(Util.AUTH_TOKEN);
			editor.remove(Util.DEVICE_ID);
			editor.commit();
			updateUIIntent.putExtra(STATUS_EXTRA, UNREGISTERED_STATUS);
		}
		context.sendBroadcast(updateUIIntent);
	}

//	private static RegistrationInfoRequest getRequest(Context context) {
//		MyRequestFactory requestFactory = Util.getRequestFactory(context,
//				MyRequestFactory.class);
//		RegistrationInfoRequest request = requestFactory
//				.registrationInfoRequest();
//		return request;
//	}
}
