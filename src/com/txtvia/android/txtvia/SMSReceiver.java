package com.txtvia.android.txtvia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

/*
 * Handles incoming SMS
 */

public class SMSReceiver extends BroadcastReceiver 
{ 
	private static final String TAG = "SMSReceiver";
	
    @Override 
    public void onReceive(Context context, Intent intent) { 
        Log.i(TAG, "SMS received.");
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages = null;
        String str = "";
        if(bundle != null){
        	Object[] pdus = (Object[]) bundle.get("pdus");
        	messages = new SmsMessage[pdus.length];
        	for(int i = 0; i < messages.length; i++){
        		messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
        		String sender = messages[i].getOriginatingAddress();                     
                
                String message = messages[i].getMessageBody().toString();
                Log.i(TAG, "********&&&&&&Received message:'" +message+"' from " + sender );
                
                //get reg id
                SharedPreferences preSettings = Util.getSharedPreferences(context);

                String deviceRegistrationId = preSettings.getString(Util.DEVICE_REGISTRATION_ID, "");
                
                try {
					HttpClient httpclient = new DefaultHttpClient();
					HttpResponse response = httpclient.execute(new HttpGet(
							Setup.PROD_URL + "/create_message?recipient="
									+ sender + "&body="
									+ message + "&device_id="
									+ deviceRegistrationId + "&from_phone=true"));
					StatusLine statusLine = response.getStatusLine();
					if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						response.getEntity().writeTo(out);
						out.close();
						String responseString = out.toString();
						Log.i(TAG, "got response from server:" + responseString);
						boolean success = true;


					} else {
						// Closes the connection.
						response.getEntity().getContent().close();
						throw new IOException(statusLine.getReasonPhrase());
					}
				} catch (Exception e) {
					Log.w(TAG, "we're not so good, heres shit:", e);
				}
                
                

        	}
        }
    }
}