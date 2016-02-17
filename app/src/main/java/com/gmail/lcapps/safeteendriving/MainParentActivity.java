package com.gmail.lcapps.safeteendriving;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainParentActivity extends ListActivity
{

    private static final int GET_CONTACTS_RESULT_CODE = 0;
    private ArrayList<TeenDriver> m_driverList = new ArrayList<TeenDriver>();
    ArrayList<String> m_contactLabels = new ArrayList<String>();
    ServiceArrayAdapter m_serviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        m_contactLabels.add(""); // Add Contacts Button
        m_serviceAdapter = new ServiceArrayAdapter(this, this.m_contactLabels);
        setListAdapter(this.m_serviceAdapter);
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

                this.m_contactLabels.add(name);
                this.m_driverList.add(driver);
                this.m_serviceAdapter.notifyDataSetChanged();
            }
        }
    }

    public class ServiceArrayAdapter extends ArrayAdapter<String>
    {
        private ArrayList<String> m_values;
        private Context m_context;
        private LayoutInflater m_inflater;
        protected Button m_addContactButton;
        protected Button m_contactColorButton;

        public ServiceArrayAdapter(Context context, ArrayList<String> contactLabels)
        {
            super(context, android.R.layout.simple_list_item_1, contactLabels);
            this.m_context = context;
            this.m_values = contactLabels;
            this.m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getViewTypeCount()
        {
            return m_values.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            View rowView = null;

            if(position == 0)
            {
                rowView = m_inflater.inflate(R.layout.add_contact_list_item, parent, false);
                m_addContactButton = (Button) rowView.findViewById(R.id.buttonAddContact);

                m_addContactButton.setOnClickListener(new View.OnClickListener()
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
                rowView = m_inflater.inflate(R.layout.contact_main_service_list_item, parent, false);
                m_contactColorButton = (Button) rowView.findViewById(R.id.buttonMainService);
                m_contactColorButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                m_contactColorButton.setOnClickListener(new View.OnClickListener()
                {
                    //@Override
                    public void onClick(View v)
                    {
                        changeButton(m_driverList.get(position-1));
                    }

                });
            }

            TextView textView = (TextView) rowView.findViewById(R.id.label);
            textView.setText((String)m_values.get(position));

            return rowView;
        }

        private void changeButton(TeenDriver driver)
        {
            Button button = driver.getButton();
            if(button.getText().equals("Register Device"))
            {
                if(registerTeenPhone())
                {
                    button.setText("OFF");
                    button.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                }
            }
            else if(button.getText().equals("ON"))
            {
                sendNotification("off");
                button.setText("OFF");
                button.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                GeneralToastMsg("The service has been turned OFF");
            }
            else if(button.getText().equals("OFF"))
            {
                button.setText("ON");
                button.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                GeneralToastMsg("The service has been turned ON");
                sendNotification("on");
            }
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
            final EditText regIdText = new EditText(this.m_context);
            regIdText.setInputType(InputType.TYPE_CLASS_NUMBER);

            AlertDialog.Builder alert = new AlertDialog.Builder(this.m_context);
            alert.setMessage("Teen's Registration Id:");
            alert.setPositiveButton("Register", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String guid = preferences.getString("parentGuid", "null");

                    String url = "https://66ctfnx3b2.execute-api.us-west-2.amazonaws.com/prod/requestTeenPhone";
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

        private void GeneralToastMsg(String toastMsg)
        {
            Toast toast = Toast.makeText(m_context, toastMsg, Toast.LENGTH_SHORT);
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
