package com.txtvia.android.txtvia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

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
                String deviceId = preSettings.getString(Util.DEVICE_ID, "");
                String authenticationToken = Util.getAuthToken(context);
                
                try {
                	HttpClient httpclient = new DefaultHttpClient();
                	HttpPost httppost = new HttpPost(Setup.PROD_URL + "/messages/");
                	List <NameValuePair> messageValues = new ArrayList <NameValuePair>();
                	messageValues.add(new BasicNameValuePair("sender",sender));
                	messageValues.add(new BasicNameValuePair("body",message));
                	messageValues.add(new BasicNameValuePair("device_id",deviceId));
                	messageValues.add(new BasicNameValuePair("authentication",authenticationToken));
                	messageValues.add(new BasicNameValuePair("api_key", Setup.API_KEY));
                	httppost.setEntity(new UrlEncodedFormEntity(messageValues, HTTP.UTF_8));
                	httppost.setHeader("Accept", "application/json");
                	httppost.setHeader("Content-type", "application/json");
                	
                	HttpResponse response = httpclient.execute(httppost);
                	
                	int statusCode = response.getStatusLine().getStatusCode();
                    Log.i(TAG, "ErrorHandler post status code: " + statusCode);
                	
//					HttpClient httpclient = new DefaultHttpClient();
//					HttpResponse response = httpclient.execute(new HttpGet(
//							Setup.PROD_URL + "/messages/create_message?recipient="
//									+ sender + "&body="
//									+ message + "&device_id="
//									+ deviceRegistrationId + "&from_phone=true"+ "&authentication_token="
//									+ authenticationToken));
//					StatusLine statusLine = response.getStatusLine();
//					if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
//						ByteArrayOutputStream out = new ByteArrayOutputStream();
//						response.getEntity().writeTo(out);
//						out.close();
//						String responseString = out.toString();
//						Log.i(TAG, "got response from server:" + responseString);
//					} else {
//						response.getEntity().getContent().close();
//						throw new IOException(statusLine.getReasonPhrase());
//					}
				} catch (Exception e) {
					Log.w(TAG, "we're not so good, heres shit:", e);
				}
                
                

        	}
        }
    }
}