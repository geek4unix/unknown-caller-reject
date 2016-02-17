package com.namitservices.ucr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by neil on 07/12/2015.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    final static String TAG = "BootCompletedReceiver";

    @Override public void onReceive(Context context,Intent intent){
        try{
            context.startService(new Intent(context,TestService.class));
            Log.i(TAG,"Starting Service ConnectivityListener");
        }catch(Exception e){
            Log.e(TAG,e.toString());
        }
    }
}
