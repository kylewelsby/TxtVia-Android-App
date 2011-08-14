package com.txtvia.android.txtvia;

import com.google.android.c2dm.C2DMessaging;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TxtViaActivity extends Activity {
	

    
	/**
     * Tag for logging.
     */
    private static final String TAG = "TxtViaActivity";

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
        
    }
    
    /**
     * The current context.
     */
    private Context mContext = this;

    /**
     * A {@link BroadcastReceiver} to receive the response from a register or
     * unregister request, and to update the UI.
     */
    
    private final OnClickListener onClickDisconnect = new OnClickListener(){
        public void onClick(View v) {
        	Log.i(TAG, "Clicked to Disconnect");
        	final Button button = (Button) findViewById(R.id.connect_device);
        	final ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar1);
            button.setVisibility(4);
            progress.setVisibility(0);
            // Unregister in the background and terminate the activity
            C2DMessaging.unregister(mContext);
            
//            finish();
        }
    };
    
    private final OnClickListener onClickConnect = new OnClickListener(){
    	public void onClick(View v){
    		Log.i(TAG, "Clicked to Connect");
			Intent i = new Intent(TxtViaActivity.this, AccountsActivity.class);
			startActivity(i);
    	}
    };
    
    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	
            final Button button = (Button) findViewById(R.id.connect_device);
            final ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar1);
            final TextView info = (TextView) findViewById(R.id.textInfo);
            int status = intent.getIntExtra(DeviceRegistrar.STATUS_EXTRA, DeviceRegistrar.ERROR_STATUS);
            String message = null;
            if (status == DeviceRegistrar.REGISTERED_STATUS) {
//                message = getResources().getString(R.string.registration_succeeded);
                Log.i(TAG, "Registered");
                button.setText(R.string.disconnect);
                button.setOnClickListener(onClickDisconnect);
            } else if (status == DeviceRegistrar.UNREGISTERED_STATUS) {
                message = getResources().getString(R.string.unregistration_succeeded);
                Log.i(TAG, "Unregistered");
                button.setText(R.string.connect);
                button.setOnClickListener(onClickConnect);
            }else if (status == DeviceRegistrar.DEVICE_READY){
            	info.setVisibility(0);
            	message = getResources().getString(R.string.registration_succeeded);
        	}else{
                message = getResources().getString(R.string.registration_error);
                Log.i(TAG, "Error");
            }
 
        	Log.i(TAG,"Updating The UI to Active");
        	
            button.setVisibility(0);
            progress.setVisibility(4);
            
            if(message != null){
            // Display a notification
            	SharedPreferences prefs = Util.getSharedPreferences(mContext);
            	String accountName = prefs.getString(Util.ACCOUNT_NAME, "Unknown");
            	Util.generateNotification(mContext, String.format(message, accountName));
            }
        }
    };
    
    private final BroadcastReceiver mLoadingUIReceiver = new BroadcastReceiver(){
    	@Override
    	public void onReceive(Context context, Intent intent){
    		final Button button = (Button) findViewById(R.id.connect_device);
            final ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar1);
            
            Log.i(TAG,"Updating The UI to Loading");
    		button.setVisibility(4);
            progress.setVisibility(0);
    	}
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        setContentView(R.layout.main);
        final SharedPreferences prefs = Util.getSharedPreferences(mContext);
        final Button button = (Button) findViewById(R.id.connect_device);
        
        registerReceiver(mUpdateUIReceiver, new IntentFilter(Util.UPDATE_UI_INTENT));
        registerReceiver(mLoadingUIReceiver, new IntentFilter(Util.UPDATE_UI_LOADING));
        
        
        if(prefs.getInt(Util.DEVICE_ID, 0) > 0){
        	Log.i(TAG,"Logged in already, changing the Button Text to Disconnect.");
        	button.setText(R.string.disconnect);
        	button.setOnClickListener(onClickDisconnect);
        }else{
        	button.setText(R.string.connect);
        	button.setOnClickListener(onClickConnect);
        }
    }
    
    /**
     * Shuts down the activity.
     */
    @Override
    public void onDestroy() {
        unregisterReceiver(mUpdateUIReceiver);
        super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        // Invoke the Register activity
        menu.getItem(0).setIntent(new Intent(this, AccountsActivity.class));
        return true;
    }
    

    
}