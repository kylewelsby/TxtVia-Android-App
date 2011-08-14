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

@SuppressWarnings("deprecation")
public class SMSReceiver extends BroadcastReceiver 
{ 
	private static final String TAG = "SMSReceiver";
	
    @Override 
    public void onReceive(Context context, Intent intent) { 
        Log.i(TAG, "SMS received.");
        Bundle bundle = intent.getExtras();
		SmsMessage[] messages = null;
        if(bundle != null){
        	Object[] pdus = (Object[]) bundle.get("pdus");
        	messages = new SmsMessage[pdus.length];
        	for(int i = 0; i < messages.length; i++){
        		messages[i] = (SmsMessage)pdus[i];
        		String sender = messages[i].getOriginatingAddress();                     
                
                String message = messages[i].getMessageBody().toString();
                String received_at = "" + messages[i].getTimestampMillis() +"";
                Log.i(TAG, "********&&&&&&Received message:'" +message+"' from " + sender );
                
                //get reg id
                SharedPreferences preSettings = Util.getSharedPreferences(context);

//                String deviceRegistrationId = preSettings.getString(Util.DEVICE_REGISTRATION_ID, "");
                String deviceId = preSettings.getString(Util.DEVICE_ID, "");
                String authenticationToken = preSettings.getString(Util.AUTH_TOKEN, null);;
                
                try {
                	HttpClient httpclient = new DefaultHttpClient();
                	HttpPost httppost = new HttpPost(Setup.PROD_URL + "/messages/");
                	List <NameValuePair> messageValues = new ArrayList <NameValuePair>();
                	messageValues.add(new BasicNameValuePair("sender",sender));
                	messageValues.add(new BasicNameValuePair("body",message));
                	messageValues.add(new BasicNameValuePair("received_at",received_at));
                	messageValues.add(new BasicNameValuePair("device_id",deviceId));
                	messageValues.add(new BasicNameValuePair("auth_token",authenticationToken));
                	messageValues.add(new BasicNameValuePair("api_key", Setup.API_KEY));
                	httppost.setEntity(new UrlEncodedFormEntity(messageValues, HTTP.UTF_8));
                	httppost.setHeader("Accept", "application/json");
                	
                	HttpResponse response = httpclient.execute(httppost);
                	
                	StatusLine statusLine = response.getStatusLine();
                	
                	int statusCode = statusLine.getStatusCode();
                    Log.i(TAG, "ErrorHandler post status code: " + statusCode);
                	if(statusCode == HttpStatus.SC_CREATED){
                		ByteArrayOutputStream out = new ByteArrayOutputStream();
						response.getEntity().writeTo(out);
						out.close();
						String responseString = out.toString();
						Log.i(TAG, "got response from server:" + responseString);
                	}else{
                		response.getEntity().getContent().close();
						throw new IOException(statusLine.getReasonPhrase());
                	}
				} catch (Exception e) {
					Log.w(TAG, "we're not so good:", e);
				}
                
                

        	}
        }
    }
}