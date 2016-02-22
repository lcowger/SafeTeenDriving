package com.gmail.lcapps.safeteendriving;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

// Server API key: AIzaSyCTL7G8lOP0wvHFx-UWEbv3xflgYXmyeEI
// Sender ID: 725482207740

public class TeenGcmListenerService extends GcmListenerService
{
    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        Log.d(TAG, "From: " + from);

        parseMessage(data);
    }

    static void updateMyActivity(Context context, String message)
    {

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

        if (type.equals("teenRequest"))
        {
            askForRegistration(obj);
        }
        else if (type.equals("teenRequestSuccessful"))
        {
            sendRegistrationSuccessful(obj);
        }
    }

    private void sendRegistrationSuccessful(JSONObject jsonObj)
    {
        String msg = "Registration Successful!";
        Intent intent = new Intent("showRequestAcceptedToast");
        intent.putExtra("message", msg);

        getApplicationContext().sendBroadcast(intent);
    }

    private void askForRegistration(JSONObject jsonObj)
    {
        String firstName = jsonObj.optString("firstName");
        String lastName = jsonObj.optString("lastName");
        String email = jsonObj.optString("email");
        final String parentGuid = jsonObj.optString("parentGuid");
        final String alertMessage = firstName + " " + lastName + " (" + email + ") would like access to your phone.";

        Intent intent = new Intent("showRequestDialog");
        intent.putExtra("message", alertMessage);
        intent.putExtra("parentGuid", parentGuid);

        getApplicationContext().sendBroadcast(intent);
    }

    private void doSomethingWithMessage(JSONObject message)
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
    }
}

