package com.gmail.lcapps.safeteendriving;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class MainTeenActivity extends Activity
{
    private static final String TAG = "MainTeenActivity";

    private Intent m_currentLocationServiceIntent;
    private TextView m_registrationIdText;
    private TextView m_latitudeText;
    private TextView m_longitudeText;
    private TextView m_speedText;
    private TextView m_serviceText;
    private TextView m_callsText;

    private String m_latitude;
    private String m_longitude;
    private String m_speed;
    private boolean m_service = true;
    private boolean m_calls = false;

    private BroadcastReceiver m_receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            switch (intent.getAction())
            {
                case BroadcastEventType.GRABBED_NEW_TOKEN:

                    createTeenDriver(intent.getStringExtra("token"));

                    break;

                case BroadcastEventType.ASK_TEEN_FOR_REGISTRATION:

                    showRequestDialogBox(intent.getStringExtra("message"), intent.getStringExtra("parentGuid"));

                    break;

                case BroadcastEventType.NEW_LOCATION_DATA:

                    m_latitude = intent.getStringExtra("latitude");
                    m_longitude = intent.getStringExtra("longitude");
                    m_speed = intent.getStringExtra("speed");
                    m_calls = intent.getBooleanExtra("calls", false);

                    updateUI();

                    break;

                case BroadcastEventType.CHANGE_TEEN_SERVICE_STATUS:

                    if (m_service == true)
                    {
                        stopService(m_currentLocationServiceIntent);
                        m_service = false;
                    }
                    else
                    {
                        startLocationService();
                        m_service = true;
                    }

                    updateUI();

                    break;
            }
        }
    };

    private void updateUI()
    {
        m_latitudeText.setText(m_latitude);
        m_longitudeText.setText(m_longitude);
        m_speedText.setText(m_speed + " mph");

        if (m_service == true)
        {
            m_serviceText.setText("ON");
            m_serviceText.setTextColor(Color.parseColor("#006600"));
        }
        else
        {
            m_serviceText.setText("OFF");
            m_serviceText.setTextColor(Color.RED);
        }

        if (m_calls == true)
        {
            m_callsText.setText("ENABLED");
            m_callsText.setTextColor(Color.parseColor("#006600"));
        }
        else
        {
            m_callsText.setText("DISABLED");
            m_callsText.setTextColor(Color.RED);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastEventType.GRABBED_NEW_TOKEN);
        intentFilter.addAction(BroadcastEventType.ASK_TEEN_FOR_REGISTRATION);
        intentFilter.addAction(BroadcastEventType.NEW_LOCATION_DATA);
        intentFilter.addAction(BroadcastEventType.CHANGE_TEEN_SERVICE_STATUS);

        this.registerReceiver(m_receiver, intentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        this.unregisterReceiver(m_receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_teen);

        m_registrationIdText = (TextView) findViewById(R.id.labelRegistrationId);
        m_latitudeText = (TextView) findViewById(R.id.labelLatitude);
        m_longitudeText = (TextView) findViewById(R.id.labelLongitude);
        m_speedText = (TextView) findViewById(R.id.labelSpeed);
        m_serviceText = (TextView) findViewById(R.id.labelService);
        m_callsText = (TextView) findViewById(R.id.labelCalls);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String regId = preferences.getString("regId", "null");

        if (regId.equals("null"))
        {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        else
        {
            m_registrationIdText.setText(regId);

            startLocationService();;
        }
    }

    private void startLocationService()
    {
        m_currentLocationServiceIntent = new Intent(this, CustomLocationListenerService.class);
        m_currentLocationServiceIntent.putExtra("maxSpeed", 15);

        startService(m_currentLocationServiceIntent);
    }

    private void createTeenDriver(String token)
    {
        String url = "https://66ctfnx3b2.execute-api.us-west-2.amazonaws.com/prod/addTeenPhone";
        String message = null;
        try
        {
            message = new JSONObject().put("token", token).toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        MyHttpRequest request = new MyHttpRequest(getApplicationContext());
        request.setDataDownloadListener(new MyHttpRequest.DataDownloadListener()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void dataDownloadedSuccessfully(Object data)
            {
                String regId = null;
                try
                {
                    JSONObject reader = new JSONObject(data.toString());
                    regId = reader.getString("data");
                }
                catch(Exception e)
                {
                    Log.v("Error", e.toString());
                }

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPreferences.edit().putString("regId", regId).apply();

                m_registrationIdText.setText(regId);

            }
            @Override
            public void dataDownloadFailed() {
                // handler failure (e.g network not available etc.)
            }
        });
        request.execute(url, message);
    }

    private void showRequestDialogBox(String message, final String parentGuid)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(message);
        alert.setPositiveButton("Accept", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String teenId = preferences.getString("regId", "null");

                String url = "https://66ctfnx3b2.execute-api.us-west-2.amazonaws.com/prod/registerTeenPhone";

                String payload = null;
                try
                {
                    payload = new JSONObject().put("parentGuid", parentGuid).put("teenGuid", teenId).toString();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                MyHttpRequest request = new MyHttpRequest(getApplicationContext());
                request.setDataDownloadListener(new MyHttpRequest.DataDownloadListener()
                {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void dataDownloadedSuccessfully(Object data)
                    {
                        String guid = null;
                        try
                        {
                            JSONObject reader = new JSONObject(data.toString());
                            guid = reader.getString("data");
                        }
                        catch (Exception e)
                        {
                            Log.v("Error", e.toString());
                        }
                    }

                    @Override
                    public void dataDownloadFailed() {
                        // handler failure (e.g network not available etc.)
                    }
                });
                request.execute(url, payload);
            }
        });
        alert.setNegativeButton("Decline", null);
        alert.show();

    }
}
