package com.gmail.lcapps.safeteendriving;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterParentAccountActivity extends Activity
{
    private BroadcastReceiver m_receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(BroadcastEventType.GRABBED_NEW_TOKEN))
            {
                createAccount(intent.getStringExtra("token"));
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        this.registerReceiver(m_receiver, new IntentFilter(BroadcastEventType.GRABBED_NEW_TOKEN));
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
        setContentView(R.layout.activity_register_account);

        Button createAccountButton = (Button) findViewById(R.id.buttonCreateAccount);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), RegistrationIntentService.class);
                startService(intent);
            }
        });
    }

    private void createAccount(String token)
    {
        String url = "https://66ctfnx3b2.execute-api.us-west-2.amazonaws.com/prod/createUser";
        String email = ((EditText)findViewById(R.id.txtBoxEmail)).getText().toString();
        String firstName = ((EditText)findViewById(R.id.txtBoxFirstName)).getText().toString();
        String lastName = ((EditText)findViewById(R.id.txtBoxLastName)).getText().toString();
        String pw = ((EditText)findViewById(R.id.txtBoxPassword)).getText().toString();

        String message = null;
        try
        {
            message = new JSONObject().put("email", email).put("token", token).put("firstName", firstName).put("lastName", lastName).put("password", pw).toString();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        MyHttpRequest request = new MyHttpRequest(getApplicationContext());
        request.setDataDownloadListener(new MyHttpRequest.DataDownloadListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void dataDownloadedSuccessfully(Object data) {

                String guid = null;
                try {
                    JSONObject reader = new JSONObject(data.toString());
                    guid = reader.getString("data");
                } catch (Exception e) {
                    Log.v("Error", e.toString());

                }

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPreferences.edit().putString("parentGuid", guid).apply();

                Intent mainParentIntent = new Intent(RegisterParentAccountActivity.this, MainParentActivity.class);
                startActivity(mainParentIntent);
            }

            @Override
            public void dataDownloadFailed() {
                // handler failure (e.g network not available etc.)
            }
        });
        request.execute(url, message);
    }
}

