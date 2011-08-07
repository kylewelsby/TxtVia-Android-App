package com.txtvia.android.txtvia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import com.wdowka.apps.spotminder.client.MyRequestFactory;
import com.wdowka.apps.spotminder.client.MyRequestFactory.RegistrationInfoRequest;
import com.wdowka.apps.spotminder.shared.RegistrationInfoProxy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;

public class DeviceRegistrar {
	public static final String STATUS_EXTRA = "Status";

	public static final int REGISTERED_STATUS = 1;

	public static final int UNREGISTERED_STATUS = 2;

	public static final int ERROR_STATUS = 3;

	private static final String TAG = "DeviceRegistrar";

	public static void registerOrUnregister(final Context context,
			final String deviceRegistrationId, final boolean register) {
		final Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);

		SharedPreferences prefs = Util.getSharedPreferences(context);
		String accountName = prefs.getString(Util.ACCOUNT_NAME, null);

		RegistrationInfoRequest request = getRequest(context);

		try {
			Log.i(TAG, "got registration number:" + deviceRegistrationId
					+ "  for account:" + accountName);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(
					"http://10.59.2.51:1234/register_device?account_name="
							+ accountName + "&registration_id="
							+ deviceRegistrationId));
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
					}
					editor.commit();
					updateUIIntent.putExtra(STATUS_EXTRA, register ? REGISTERED_STATUS : UNREGISTERED_STATUS);
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
			Log.w(TAG, "shit happend when sending registration ID to server", e);
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
