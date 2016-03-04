package com.gmail.lcapps.safeteendriving;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
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

public class CustomLocationListenerService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private GoogleApiClient m_googleApiClient;
    private Location m_lastLocation;
    private Location m_currentLocation;
    private LocationRequest m_locationRequest;
    private boolean m_requestingLocationUpdates = false;
    //private final IBinder m_binder = new LocalBinder();

    private static final String TAG = "LocationListenerService";

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
        String maxSpeed = intent.getStringExtra("maxSpeed");

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopLocationUpdates();
        m_googleApiClient.disconnect();
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
        int speed = (int)(location.getSpeed() * 2.2369);

        Intent updateUiIntent = new Intent(BroadcastEventType.NEW_LOCATION_DATA);
        updateUiIntent.putExtra("latitude", String.valueOf(location.getLatitude()));
        updateUiIntent.putExtra("longitude", String.valueOf(location.getLongitude()));
        updateUiIntent.putExtra("speed", String.valueOf(speed));

        getApplicationContext().sendBroadcast(updateUiIntent);
    }
    //endregion

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
