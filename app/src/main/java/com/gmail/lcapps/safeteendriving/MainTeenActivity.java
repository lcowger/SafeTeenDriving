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
            if (intent.getAction().equals("showRequestDialog"))
            {
                showRequestDialogBox(intent.getStringExtra("message"), intent.getStringExtra("parentGuid"));
            }
        }
    };

    //register your activity onResume()
    @Override
    public void onResume()
    {
        super.onResume();
        this.registerReceiver(m_receiver, new IntentFilter("showRequestDialog"));
    }

    //Must unregister onPause()
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

        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String teenId = preferences.getString("regId", "null");

        if (teenId.equals("null"))
        {
            // Need to register?
        }
        else
            this.m_registrationIdText.setText(teenId);
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

                String message = null;
                try
                {
                    message = new JSONObject().put("parentGuid", parentGuid).put("teenGuid", teenId).toString();
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
                request.execute(url, message);
            }
        });
        alert.setNegativeButton("Decline", null);
        alert.show();

    }
}
