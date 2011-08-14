package com.txtvia.android.txtvia;


import com.google.android.c2dm.C2DMBaseReceiver;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;


public class C2DMReceiver extends C2DMBaseReceiver {

    private static final String TAG = "C2DMReceiver";
    final Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);

	public C2DMReceiver() {
        super(Setup.SENDER_ID);
    }
    
    /**
     * Called when a registration token has been received.
     * 
     * @param context the Context
     * @param registrationId the registration id as a String
     * @throws IOException if registration cannot be performed
     */
    @Override
    public void onRegistered(Context context, String registration) {
        DeviceRegistrar.register(context, registration, true);
    }

    /**
     * Called when the device has been unregistered.
     * 
     * @param context the Context
     */
    @Override
    public void onUnregistered(Context context) {
    	Log.i(TAG, "Unregistered");
//        SharedPreferences prefs = Util.getSharedPreferences(context);
//        String deviceRegistrationID = prefs.getString(Util.DEVICE_REGISTRATION_ID, null);
        DeviceRegistrar.unregister(context);
    }

    /**
     * Called on registration error. This is called in the context of a Service
     * - no dialog or UI.
     * 
     * @param context the Context
     * @param errorId an error message, defined in {@link C2DMBaseReceiver}
     */
    @Override
    public void onError(Context context, String errorId) {
        context.sendBroadcast(new Intent(Util.UPDATE_UI_INTENT));
        Log.e(TAG, "Error");
    }

    /**
     * Called when a cloud message has been received.
     */
    @Override
    public void onMessage(Context context, Intent intent) {
        /*
         * Replace this with your application-specific code
         */
    	final Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);
    	
    	Bundle extras = intent.getExtras();
    	if(extras.containsKey("message_body") && extras.containsKey("message_recipient")){
    		Log.i(TAG, "message received");
    		MessageDisplay.displayMessage(context, intent);
    	}else if(extras.containsKey("auth_token")){
    		Log.i(TAG, "auth_token received");
			
    		SharedPreferences prefs = Util.getSharedPreferences(context);
    		String token = extras.getString("auth_token");
    		SharedPreferences.Editor editor = prefs.edit();
    		editor.putString(Util.AUTH_TOKEN, token);
    		editor.commit();
    		
    		updateUIIntent.putExtra(DeviceRegistrar.STATUS_EXTRA, DeviceRegistrar.DEVICE_READY);
    		context.sendBroadcast(updateUIIntent);
    		Log.i(TAG, "auth_token received" + token);
    	}
    }
    
}

