package com.gmail.lcapps.safeteendriving;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
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

    private int m_maxSpeed = 10;
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
                case BroadcastEventType.GRABBED_NEW_TOKEN:
                    createTeenDriver(intent.getStringExtra("token"));
                    break;

                case BroadcastEventType.ASK_TEEN_FOR_REGISTRATION:
                    showRequestDialogBox(intent.getStringExtra("message"), intent.getStringExtra("parentGuid"));
                    break;

                case BroadcastEventType.NEW_LOCATION_DATA:
                    updateUI(intent.getStringExtra("latitude"), intent.getStringExtra("longitude"), intent.getStringExtra("speed"));
                    checkMaxSpeed(intent.getStringExtra("speed"));
                    break;
            }
        }
    };

    private void checkMaxSpeed(String speed)
    {
        if (Integer.parseInt(speed) < m_maxSpeed)
        {
            m_numTimesUnderMph++;

            if (m_numTimesUnderMph == 9)
            {
                m_timer = new Timer();
                m_timerTask = new MyTimerTask();

                m_timer.schedule(m_timerTask, 30000);
            }
        }
        else
        {
            m_timer.cancel();
            m_timer = null;
            m_timerTask.cancel();
            m_timerTask = null;
            m_numTimesUnderMph = 0;

            // if (calls/texts are enabled) { disable them }
        }
    }

    class MyTimerTask extends TimerTask
    {
        public void run()
        {
            m_numTimesUnderMph = 0;
            // if (calls/texts are disabled) { enabled them }
        }
    }

    private void updateUI(String latitude, String longitude, String speed)
    {
        m_latitudeText.setText(latitude);
        m_longitudeText.setText(longitude);
        m_speedText.setText(speed + " mph");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        this.registerReceiver(m_receiver, new IntentFilter(BroadcastEventType.GRABBED_NEW_TOKEN));
        this.registerReceiver(m_receiver, new IntentFilter(BroadcastEventType.ASK_TEEN_FOR_REGISTRATION));
        this.registerReceiver(m_receiver, new IntentFilter(BroadcastEventType.NEW_LOCATION_DATA));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        this.unregisterReceiver(m_receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_teen);

        m_registrationIdText = (TextView) findViewById(R.id.labelRegistrationId);
        m_latitudeText = (TextView) findViewById(R.id.labelLatitude);
        m_longitudeText = (TextView) findViewById(R.id.labelLongitude);
        m_speedText = (TextView) findViewById(R.id.labelSpeed);
        m_serviceText = (TextView) findViewById(R.id.labelService);

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
            m_currentLocationServiceIntent = new Intent(this, CustomLocationListenerService.class);

            startService(m_currentLocationServiceIntent);
        }
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
