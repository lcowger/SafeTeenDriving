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

public class MainTeenActivity extends Activity
{
    private TextView m_registrationIdText;
    private BroadcastReceiver m_receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {

            if (intent.getAction().equals(BroadcastEventType.GRABBED_TOKEN))
            {
                createTeenDriver(intent.getStringExtra("token"));
            }
            if (intent.getAction().equals(BroadcastEventType.ASK_TEEN_FOR_REGISTRATION))
            {
                showRequestDialogBox(intent.getStringExtra("message"), intent.getStringExtra("parentGuid"));
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        this.registerReceiver(m_receiver, new IntentFilter(BroadcastEventType.GRABBED_TOKEN));
        this.registerReceiver(m_receiver, new IntentFilter(BroadcastEventType.ASK_TEEN_FOR_REGISTRATION));
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

        this.m_registrationIdText = (TextView) findViewById(R.id.labelRegistrationId);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String regId = preferences.getString("regId", "null");

        if (regId.equals("null"))
        {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        else
            this.m_registrationIdText.setText(regId);
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
