package com.gmail.lcapps.safeteendriving;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterParentAccountActivity extends Activity {

    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);

        this.createAccountButton = (Button) findViewById(R.id.buttonCreateAccount);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), RegistrationIntentService.class);
                startService(intent);

                String url = "https://66ctfnx3b2.execute-api.us-west-2.amazonaws.com/prod/createUser";
                String email = ((EditText)findViewById(R.id.txtBoxEmail)).getText().toString();
                String firstName = ((EditText)findViewById(R.id.txtBoxFirstName)).getText().toString();
                String lastName = ((EditText)findViewById(R.id.txtBoxLastName)).getText().toString();
                String pw = ((EditText)findViewById(R.id.txtBoxPassword)).getText().toString();

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String token = sharedPrefs.getString("token", "null");

                String message = null;
                try {
                    message = new JSONObject().put("email", email).put("token", token).put("firstName", firstName).put("lastName", lastName).put("password", pw).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                MyHttpRequest request = new MyHttpRequest(getApplicationContext());
                request.setDataDownloadListener(new MyHttpRequest.DataDownloadListener()
                {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void dataDownloadedSuccessfully(Object data) {

                        String guid = null;
                        try {
                            JSONObject reader = new JSONObject(data.toString());
                            guid = reader.getString("data");
                        }
                        catch(Exception e) {
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
        });
    }
}

