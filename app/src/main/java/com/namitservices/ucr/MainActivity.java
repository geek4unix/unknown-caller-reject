package com.namitservices.ucr;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;


public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public boolean ucrnotification(boolean state) {
        // prepare intent which is triggered if the
// notification is selected

        Intent intent = new Intent(this, MainActivity.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

// build notification

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
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ToggleButton ToggleStatus = (ToggleButton) findViewById(R.id.toggleButton);

        //prep the admob banner
        //AdView mAdView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);


        // Retrieve the state of the last toggle set
        Log.i(TAG, "Getting previous toggle state from Sharedprefs");
        SharedPreferences TOGGLE_STATUS = getSharedPreferences("TOGGLE_STATUS", MODE_PRIVATE);
        if (TOGGLE_STATUS.getBoolean("isChecked", false) != ToggleStatus.isChecked())
            ToggleStatus.setChecked(TOGGLE_STATUS.getBoolean("isChecked", false));


        // Check if the service is already running
        if (isMyServiceRunning()) {
            Log.i(TAG, "Service is running and was already running");
            // Set notification
            Log.i(TAG, "Settings notification for UCR background blocking ON");
            ucrnotification(true);
        } else {
            Log.i(TAG, "Service is NOT present");

            if (ToggleStatus.isChecked() && !isMyServiceRunning()) {
                startService(new Intent(getApplicationContext(), TestService.class));
                if (isMyServiceRunning()) {
                    Log.i(TAG, "Service needed to be restarted and is NOW running ...");
                    // Set notification
                    Log.i(TAG, "Settings notification for UCR background blocking ON");
                    ucrnotification(true);
                }
            } else if (!ToggleStatus.isChecked() && isMyServiceRunning()) {
                stopService(new Intent(getApplicationContext(), TestService.class));
                if (!isMyServiceRunning()) {
                    Log.i(TAG, "Service needed to be stopped ...");
                    // Set notification
                    Log.i(TAG, "Settings notification for UCR background blocking OFF");
                    ucrnotification(true);
                }
            }
        }

        final ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);


        // attach an OnClickListener
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (toggleButton.isChecked()) {

                    Log.i(TAG, "Toggle pressed = now ON ...");
                    startService(new Intent(getApplicationContext(), TestService.class));

                    if (isMyServiceRunning())
                        Log.i(TAG, "Attempt to start service - SUCCESS ! ");
                    else
                        Log.i(TAG, "Attempt to start service - FAILED :/ ");

                    Log.i(TAG, "Setting toggle state (ON) from Sharedprefs");

                    // commit out TOGGLE_STATUS save data! - ON VERSION
                    SharedPreferences TOGGLE_STATE = getSharedPreferences("TOGGLE_STATUS", MODE_PRIVATE);
                    SharedPreferences.Editor prefEditor = TOGGLE_STATE.edit();
                    prefEditor.putBoolean("isChecked", true);
                    prefEditor.commit();

                    // Set notification
                    Log.i(TAG, "Settings notification for UCR background blocking ON");
                    ucrnotification(true);


                } else {
                    Log.i(TAG, "Toggle pressed = now OFF ...");

                    boolean stopResult = stopService(new Intent(getApplicationContext(), TestService.class));

                    if (stopResult) {
                        Log.i(TAG, "Service was stopped");
                    } else {
                        Log.e(TAG, "Service tried to stop but failed");
                    }


                    // GET TOGGLE_STATUS get data!
                    Log.i(TAG, "Setting toggle (OFF) state to Sharedprefs");

                    // commit out TOGGLE_STATUS save data! (OFF Version)
                    SharedPreferences TOGGLE_STATE = getSharedPreferences("TOGGLE_STATUS", MODE_PRIVATE);
                    SharedPreferences.Editor prefEditor = TOGGLE_STATE.edit();
                    prefEditor.putBoolean("isChecked", false);
                    prefEditor.commit();

                    // Set notification
                    Log.i(TAG, "Settings notification for UCR background blocking OFF");
                    ucrnotification(false);
                }

            }

        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.namitservices.ucr.TestService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.namitservices.ucr/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.namitservices.ucr/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}

