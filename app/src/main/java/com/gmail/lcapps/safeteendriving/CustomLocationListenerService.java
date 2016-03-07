package com.gmail.lcapps.safeteendriving;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class CustomLocationListenerService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private static final String TAG = "LocationListenerService";
    private final int NUM_MILLISECONDS_TIMER = 30000;

    private GoogleApiClient m_googleApiClient;
    private Location m_lastLocation;
    private Location m_currentLocation;
    private LocationRequest m_locationRequest;
    private boolean m_requestingLocationUpdates = false;
    private boolean m_calls = false;
    private int m_maxSpeed;
    private int m_numTimesUnderMph;
    private Timer m_timer;
    private MyTimerTask m_timerTask;

    private BroadcastReceiver m_receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            switch (intent.getAction())
            {
                case BroadcastEventType.PHONE_STATE:

                    if (m_calls == false)
                        endCallRightNow();

                    break;

                case BroadcastEventType.SMS_RECEIVED:

                    //abortBroadcast();
                    Log.d(TAG, "s");

                    break;
            }
        }
    };

    //region Region - Overrides
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(999);
        intentFilter.addAction(BroadcastEventType.PHONE_STATE);
        intentFilter.addAction(BroadcastEventType.SMS_RECEIVED);

        this.registerReceiver(m_receiver, intentFilter);

        if (m_googleApiClient == null)
        {
            m_googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        m_googleApiClient.connect();
        createLocationRequest();

        if (m_googleApiClient.isConnected() && (!m_requestingLocationUpdates))
        {
            startLocationUpdates();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        m_maxSpeed = intent.getIntExtra("maxSpeed", 10);

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        stopLocationUpdates();
        m_googleApiClient.disconnect();

        destroyTimerAndTimerTask();
    }
    //endregion
    //region Region - Google API Connections/onLocationChanged()
    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        Log.v(TAG, result.getErrorMessage());

    }

    @Override
    public void onConnectionSuspended(int connectionHint)
    {
        Log.v(TAG, "Suspended.");
    }

    @Override
    public void onConnected(Bundle connectionHint)
    {
        m_lastLocation = LocationServices.FusedLocationApi.getLastLocation(m_googleApiClient);
        if (m_lastLocation != null)
        {
            //updateUI(m_lastLocation);
        }

        startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        m_currentLocation = location;

        updateUiIntent();
        checkMaxSpeed();
    }
    //endregion

    private void updateUiIntent()
    {
        int speed = (int)(m_currentLocation.getSpeed() * 2.2369);

        Intent updateUiIntent = new Intent(BroadcastEventType.NEW_LOCATION_DATA);
        updateUiIntent.putExtra("latitude", String.valueOf(m_currentLocation.getLatitude()));
        updateUiIntent.putExtra("longitude", String.valueOf(m_currentLocation.getLongitude()));
        updateUiIntent.putExtra("speed", String.valueOf(speed));
        updateUiIntent.putExtra("calls", m_calls);

        getApplicationContext().sendBroadcast(updateUiIntent);
    }

    private void destroyTimerAndTimerTask()
    {
        if (m_timer != null)
        {
            m_timer.cancel();
            m_timer = null;
        }

        if (m_timerTask != null)
        {
            m_timerTask.cancel();
            m_timerTask = null;
        }
    }

    private void checkMaxSpeed()
    {
        int speed = (int)(m_currentLocation.getSpeed() * 2.2369);

        if (speed < m_maxSpeed)
        {
            m_numTimesUnderMph++;

            // approx 1 min. (NEW_LOCATION_DATA (every 6 seconds) * m_numTimesUnderMph (10) = 60 seconds
            if (m_numTimesUnderMph == 10)
            {
                m_timer = new Timer();
                m_timerTask = new MyTimerTask();

                // Set timer to go off in 30 seconds if still under m_maxSpeed
                m_timer.schedule(m_timerTask, NUM_MILLISECONDS_TIMER);
            }
        }
        else
        {
            destroyTimerAndTimerTask();

            m_numTimesUnderMph = 0;

            if (m_calls == true)
            {
                m_calls = false;
                updateUiIntent();
            }
        }
    }

    class MyTimerTask extends TimerTask
    {
        public void run()
        {
            m_numTimesUnderMph = 0;

            if (m_calls == false)
            {
                m_calls = true;
                updateUiIntent();
            }
        }
    }

    private void endCallRightNow()
    {
        try
        {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            Method m1 = tm.getClass().getDeclaredMethod("getITelephony");
            m1.setAccessible(true);
            Object iTelephony = m1.invoke(tm);

            Method m2 = iTelephony.getClass().getDeclaredMethod("silenceRinger");
            Method m3 = iTelephony.getClass().getDeclaredMethod("endCall");

            m2.invoke(iTelephony);
            m3.invoke(iTelephony);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
    }

    private void startLocationUpdates()
    {
        LocationServices.FusedLocationApi.requestLocationUpdates(m_googleApiClient, m_locationRequest, this);
        m_requestingLocationUpdates = true;
    }

    private void stopLocationUpdates()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(m_googleApiClient, this);
        m_requestingLocationUpdates = false;
    }

    private void createLocationRequest()
    {
        // TODO: Add GPS Battery Setting enum (class) for Low, Medium, High

        m_locationRequest = new LocationRequest();
        m_locationRequest.setInterval(6000);
        m_locationRequest.setFastestInterval(6000);
        m_locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checkLocationSettings();
    }

    private void checkLocationSettings()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(m_locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(m_googleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        /*Log.d(TAG, "k");
                        try
                        {

                        // NEED TO STOP SERVICE AND RETURN CODE TO MAINTEENACTIVITY
                            // https://developers.google.com/android/reference/com/google/android/gms/common/api/Status#startResolutionForResult(android.app.Activity, int)
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().

                            // Need to add REQUEST_CHECK_SETTINGS
                            status.startResolutionForResult(MainTeenActivity, 1);
                        }
                        catch (IntentSender.SendIntentException e)
                        {
                            // Ignore the error.
                        }*/
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }

        });
    }
}
