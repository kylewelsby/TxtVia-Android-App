package com.txtvia.android.txtvia;


import com.google.android.c2dm.C2DMBaseReceiver;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


public class C2DMReceiver extends C2DMBaseReceiver {

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
        DeviceRegistrar.registerOrUnregister(context, registration, true);
    }

    /**
     * Called when the device has been unregistered.
     * 
     * @param context the Context
     */
    @Override
    public void onUnregistered(Context context) {
        SharedPreferences prefs = Util.getSharedPreferences(context);
        String deviceRegistrationID = prefs.getString(Util.DEVICE_REGISTRATION_ID, null);
        DeviceRegistrar.registerOrUnregister(context, deviceRegistrationID, false);
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
    }

    /**
     * Called when a cloud message has been received.
     */
    @Override
    public void onMessage(Context context, Intent intent) {
        /*
         * Replace this with your application-specific code
         */
        MessageDisplay.displayMessage(context, intent);
    }
    
}

