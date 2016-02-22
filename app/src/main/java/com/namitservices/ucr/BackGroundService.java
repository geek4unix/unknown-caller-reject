package com.namitservices.ucr;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by neil on 09/12/2015.
 */
public class BackGroundService extends Service {

    final static String TAG = "BackGroundService";

    int mStartMode; // will help us deal with exiting

    IBinder mBinder;  // interface that clients bind
    boolean mAllowRebind; // indicates whether onRebind should be used

    public boolean ucrnotification(boolean state) {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        if (state == true) {

            Notification n = new Notification.Builder(this)
                    .setContentTitle("UCR Active")
                    .setContentText("Unknown Caller Reject service is active")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pIntent)
                    .setAutoCancel(false)
                    .build();

            int notification_number = n.number;


            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            n.flags = Notification.FLAG_ONGOING_EVENT;

            notificationManager.notify(0, n);
            return (true);

        } else {
            // remove notification


            NotificationManager notifManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancelAll();

            return (false);

        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate method called ...");
        // tm bits


        // Retrieve the state of the last toggle set
        Log.i(TAG,"Getting previous toggle state from Sharedprefs");
        final SharedPreferences TOGGLE_STATUS = getSharedPreferences("TOGGLE_STATUS", MODE_PRIVATE);

        // If neccesary, remove the notification icon
        if(!TOGGLE_STATUS.getBoolean("isChecked", true)){
            // Cancel notification
            Log.i(TAG,"Cancelling notification for UCR background ... ");
            ucrnotification(false);
        }
        else
        //Set the notification
        {
            // Set notification
            Log.i(TAG,"Settings notification for UCR background blocking ON");
            ucrnotification(true);
        }

        //Based on the toggle, only setup a callstatelistener, when the service is enabled/running
        if(TOGGLE_STATUS.getBoolean("isChecked", true)){

         final TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);


         final PhoneStateListener callStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {

                Log.i(TAG,"onCallStateChanged Listener has activated");

                String caller_number;

                // If call state is RINGING
                if (state == TelephonyManager.CALL_STATE_RINGING)
                {
                    if (incomingNumber == null || incomingNumber.isEmpty() || incomingNumber.trim()=="+67") {
                        Log.i(TAG,"Phone is ringing, incoming call either has no number (PRIVATE,UNKNOWN) or is the special string \"+67\"");
                        caller_number = "UNKNOWN";
                    }
                    else {caller_number = incomingNumber;}

                    if (caller_number.equals("UNKNOWN") && TOGGLE_STATUS.getBoolean("isChecked",true)) {


                        AudioManager am;
                        am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                        Log.i(TAG,"Getting the current ringer mode ...");
                        Log.i(TAG,"Silencing the ringer ...");

                        // Get current ringtone,vibrate mode

                        String CURRENT_VIBRATE = am.EXTRA_RINGER_MODE.toString();
                        Log.i(TAG, String.format("CURRENT_VIBRATE: %s", CURRENT_VIBRATE));
                        Integer CURRENT_RING = am.getRingerMode();
                        // Set ring tone volume to silent
                        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                        // TODO disable vibration (if enabled) before call is disconnected
                        // HERE

                        Log.i(TAG,"calling disconnectCall() ... ");
                        disconnectCall();

                        Log.i(TAG,"Replacing the previos (CURRENT_RING) ringer mode...");
                        am.setRingerMode(CURRENT_RING);


                        Log.i(TAG,"Current vibrate setting is: "+CURRENT_VIBRATE);
                        // TODO - replace vibrate mode as captured in CURRENT_VIBRATE
                        // Log CURRENT_VIBRATE string ...

                        String RejectMessage="Caller "+" - "+caller_number+" - Call rejected";
                        Log.i(TAG,RejectMessage);


                    }
                }
            }
         };

        telephonyManager.listen(callStateListener,PhoneStateListener.LISTEN_CALL_STATE);



        // end tm bits

        // end onCreate
        }
    }
    /**
     * A client is binding to the service with bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"IBinder method called");
        return mBinder;
    }

    /**
     * Called when all clients have unbound with unbindService()
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG,"onUnbind method called");
        return mAllowRebind;
    }

    /**
     * Called when a client is binding to the service with bindService()
     */
    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG,"onRebind method called");

    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy method called");


    }

    /**
     * The service is starting, due to a call to startService()
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return mStartMode;


    }

    public void disconnectCall() {
        Log.i(TAG,"Attempting to start disconnectCall");
        try {

            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";

            Class<?> telephonyClass;
            Class<?> telephonyStubClass;
            Class<?> serviceManagerClass;
            Class<?> serviceManagerNativeClass;

            Method telephonyEndCall;
            Object telephonyObject;
            Object serviceManagerObject;

            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);

            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.invoke(telephonyObject);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


