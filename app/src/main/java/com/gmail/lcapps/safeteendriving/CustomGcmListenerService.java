package com.gmail.lcapps.safeteendriving;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import org.json.JSONException;
import org.json.JSONObject;

// Server API key: AIzaSyCTL7G8lOP0wvHFx-UWEbv3xflgYXmyeEI
// Sender ID: 725482207740

public class CustomGcmListenerService extends GcmListenerService
{
    private static final String TAG = "GcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        Log.d(TAG, "From: " + from);

        parseMessage(data);
    }

    private void parseMessage(Bundle data)
    {
        String jsonString = data.getString("message");
        JSONObject obj = null;
        try
        {
            obj = new JSONObject(jsonString);
        }
        catch (JSONException e)
        {
            Log.v(TAG, e.toString());
        }

        String type = obj.optString("type");

        switch (type)
        {
            case GcmMessageType.ASK_TEEN_FOR_REGISTRATION:
                askForRegistrationFromTeen(obj);
                break;

            case GcmMessageType.TEEN_REGISTRATION_SUCCESSFUL:
                sendTeenRegistrationSuccessfulToParent(obj);
                break;
        }

        /*if (type.equals("teenRequest"))
        {
            askForRegistrationFromTeen(obj);
        }
        else if (type.equals("teenRequestSuccessful"))
        {
            sendTeenRegistrationSuccessfulToParent(obj);
        }*/
    }

    private void askForRegistrationFromTeen(JSONObject jsonObj)
    {
        String firstName = jsonObj.optString("firstName");
        String lastName = jsonObj.optString("lastName");
        String email = jsonObj.optString("email");
        final String parentGuid = jsonObj.optString("parentGuid");
        final String alertMessage = firstName + " " + lastName + " (" + email + ") would like to register you as a driver on their phone.";

        Intent intent = new Intent(BroadcastEventType.ASK_TEEN_FOR_REGISTRATION);
        intent.putExtra("message", alertMessage);
        intent.putExtra("parentGuid", parentGuid);

        getApplicationContext().sendBroadcast(intent);
    }

    private void sendTeenRegistrationSuccessfulToParent(JSONObject jsonObj)
    {
        String msg = "Registration Successful!";
        Intent intent = new Intent(BroadcastEventType.TEEN_REGISTRATION_SUCCESSFUL);
        intent.putExtra("message", msg);
        intent.putExtra("teenId", jsonObj.optString("teenId"));

        getApplicationContext().sendBroadcast(intent);
    }

    // *********** Keep around in case of need to do something with notifications ****************
    /*private void doSomethingWithMessage(JSONObject message)
    {
        PowerManager pm = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, getClass().getName());

        //Acquire the lock
        wl.acquire();

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.cast_ic_notification_0)
                .setContentTitle("Notif!")
                .setContentText(message.optString("type"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setColor(Color.BLUE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

        wl.release();
    }*/
}

