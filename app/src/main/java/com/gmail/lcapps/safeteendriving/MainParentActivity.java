package com.gmail.lcapps.safeteendriving;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MainParentActivity extends ListActivity
{
    private static final int GET_CONTACTS_RESULT_CODE = 0;
    private ArrayList<TeenDriver> m_driverList = new ArrayList<TeenDriver>();
    DriverArrayAdapter m_driverAdapter;

    private BroadcastReceiver m_receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals("teenRegistrationSuccessful"))
            {
                /* Possible ways to implement this:
                 *  (Both don't solve the problem of if the list changes (add/delete)
                 *   between request and response (ie index will be invalid/guessId might trigger false/positives)
                 *
                 *      Current way      - add 'guess id' to TeenDriver class
                 *                         guess id is set for the teen driver before request is made
                 *                         on response, the teen id is sent down
                 *                         loop through driver list and compare id to guess id
                 *                         when driver is found, set the real (not guess) id
                 *
                 *      Alt way          - on request, send up index of array position
                 *                         on response, the index is sent back down
                 *                         set the real id
                 *
                 *     Brainstorming     - send driver name up on request and down on response
                 *                         */

                String id = intent.getStringExtra("teenId");
                String message = intent.getStringExtra("message");

                teenRegistrationSuccessful(id, message);
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        this.registerReceiver(m_receiver, new IntentFilter("teenRegistrationSuccessful"));
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

        m_driverList.add(new TeenDriver()); // Add Contacts Button
        m_driverAdapter = new DriverArrayAdapter(this, this.m_driverList);
        setListAdapter(this.m_driverAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_CONTACTS_RESULT_CODE)
        {
            Uri contactData = data.getData();
            Cursor cursor = getContentResolver().query(contactData, null, null, null, null);

            if(cursor.moveToFirst())
            {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                TeenDriver driver = new TeenDriver();
                driver.setName(name);
                driver.setServiceStatus(ServiceStatus.REGISTER_DEVICE);
                driver.setGuessId("3");

                this.m_driverList.add(driver);
                this.m_driverAdapter.notifyDataSetChanged();
            }
        }
    }

    public void teenRegistrationSuccessful(String id, String message)
    {
        for (Iterator<TeenDriver> i = m_driverList.iterator(); i.hasNext();)
        {
            TeenDriver driver = i.next();

            if (id.equals(driver.getGuessId()))
            {
                driver.setId(id);
                driver.setServiceStatus(ServiceStatus.SERVICE_ON);
                break;
            }
        }

        this.m_driverAdapter.notifyDataSetChanged();
        GeneralToastMessage(message);
    }

    private void GeneralToastMessage(String msg)
    {
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    public class DriverArrayAdapter extends ArrayAdapter<TeenDriver>
    {
        public DriverArrayAdapter(Context context, ArrayList<TeenDriver> drivers)
        {
            super(context, 0, drivers);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            if(position == 0)
            {
                if (convertView == null)
                {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.add_contact_list_item, parent, false);
                }

                Button addDriverButton = (Button) convertView.findViewById(R.id.buttonAddDriver);
                addDriverButton.setOnClickListener(new View.OnClickListener()
                {
                    //@Override
                    public void onClick(View v)
                    {
                        openContactList();
                    }

                });
            }
            else
            {
                TeenDriver driver = getItem(position);

                if (convertView == null)
                {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.teen_driver_list_item, parent, false);
                }

                TextView txtViewName = (TextView) convertView.findViewById(R.id.labelName);
                ImageButton btnSettings = (ImageButton) convertView.findViewById(R.id.buttonSettings);
                btnSettings.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        GeneralToastMessage("Open Settings");
                    }
                });

                final Button btnService = (Button) convertView.findViewById(R.id.buttonMainService);
                ServiceStatus status = driver.getServerStatus();

                if (status == ServiceStatus.REGISTER_DEVICE)
                {
                    btnService.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    btnService.setText(R.string.register_service_button);
                }
                else if (status == ServiceStatus.WAITING_FOR_RESPONSE)
                {
                    btnService.setText(R.string.waiting_service_button);
                }
                else if (status == ServiceStatus.SERVICE_ON)
                {
                    btnService.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                    btnService.setText(R.string.on_service_button);
                }
                else if (status == ServiceStatus.SERVICE_OFF)
                {
                    btnService.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                    btnService.setText(R.string.off_service_button);
                }

                btnService.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (btnService.getText().equals(getResources().getText(R.string.register_service_button)))
                        {
                            // Register
                            Log.d("k", "p");
                        }
                        else if (btnService.getText().equals(getResources().getText(R.string.waiting_service_button)))
                        {
                            // Waiting
                            Log.d("k", "p");
                        }
                        else if (btnService.getText().equals(getResources().getText(R.string.on_service_button)))
                        {
                            // On
                            Log.d("k", "p");
                        }
                        else if (btnService.getText().equals(getResources().getText(R.string.off_service_button)))
                        {
                            // Off
                            Log.d("k", "p");
                        }
                    }

                });

                txtViewName.setText(driver.getName());
            }

            return convertView;
        }

        private void sendNotification(String msg)
        {
            /*SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String guid = preferences.getString("parentGuid", "null");
            //msg.put("guid", guid);

            String url = "https://66ctfnx3b2.execute-api.us-west-2.amazonaws.com/prod/notificationFunc";
            //String teenId = regIdText.getText().toString();

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
                    catch(Exception e)
                    {
                        Log.v("Error", e.toString());

                    }
                }
                @Override
                public void dataDownloadFailed()
                {
                    // handler failure (e.g network not available etc.)
                }
            });
            request.execute(url, message);*/
        }

        private boolean registerTeenPhone()
        {
            boolean returnValue = true;
            final EditText regIdText = new EditText(getApplicationContext());
            regIdText.setInputType(InputType.TYPE_CLASS_NUMBER);

            AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
            alert.setMessage("Teen's Registration Id:");
            alert.setPositiveButton("Register", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String url = "https://66ctfnx3b2.execute-api.us-west-2.amazonaws.com/prod/requestTeenPhone";
                    String guid = preferences.getString("parentGuid", "null");
                    String teenId = regIdText.getText().toString();

                    String message = null;
                    try
                    {
                        message = new JSONObject().put("parentGuid", guid).put("teenGuid", teenId).toString();
                    }
                    catch(JSONException e)
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
                            catch(Exception e)
                            {
                                Log.v("Error", e.toString());
                            }
                        }
                        @Override
                        public void dataDownloadFailed()
                        {
                            // handler failure (e.g network not available etc.)
                        }
                    });
                    request.execute(url, message);
                }
            });
            alert.setNegativeButton("Cancel", null);
            alert.setView(regIdText);
            alert.show();

            return true;
        }

        public void GeneralToastMsg(String toastMsg)
        {
            Toast toast = Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }

        private void openContactList()
        {
            Intent it = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(it, GET_CONTACTS_RESULT_CODE);
        }
    } // End Array Adapter
}
