package com.txtvia.android.txtvia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;

/**
 * Display a message as a notification, with an accompanying sound.
 */
@SuppressWarnings("deprecation")
public class MessageDisplay {

    private MessageDisplay() {
    }


    public static void displayMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String message = (String) extras.get("message_body");
            String recipient = (String) extras.get("message_recipient");
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(recipient, null, message, null, null);
            Util.generateNotification(context, "... has sent message to " + recipient + " for you.");

        }
    }
}
