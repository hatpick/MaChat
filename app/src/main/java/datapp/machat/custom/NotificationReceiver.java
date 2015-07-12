package datapp.machat.custom;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import datapp.machat.R;
import datapp.machat.activity.ChatActivity;
import datapp.machat.activity.MainActivity;
import datapp.machat.helper.NotificationId;
import datapp.machat.helper.SendNotification;

/**
 * Created by hat on 7/12/15.
 */
public class NotificationReceiver extends ParsePushBroadcastReceiver {
    private final static String TAG = "NotificationReceiver";
    public static final String PARSE_DATA_KEY = "com.parse.Data";
    private String msg = "";

    private HashMap<String, Integer> notifIds = new HashMap<>();

    @Override
    protected void onPushReceive(final Context context, Intent intent) {
        final SharedPreferences notificationDetails = context.getSharedPreferences("notificationDetails", context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = notificationDetails.edit();

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString(PARSE_DATA_KEY));

            final String title = json.getString("title");
            final String alert = json.getString("alert");
            final String sid = json.getString("group");
            final String imgUrl = json.getString("sender_img_url");
            final String senderId = json.getString("sender_fbId");

            Integer id = notificationDetails.getInt(sid, -1);
            if(id == -1) {
                id = NotificationId.getID();
                editor.putInt(sid, id);
                editor.apply();
            }

            new SendNotification(context, intent, senderId, title, alert, imgUrl, id).execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Intent chatIntent = new Intent(context, MainActivity.class);
        chatIntent.putExtras(intent.getExtras());
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chatIntent);
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
        final SharedPreferences notificationDetails = context.getSharedPreferences("notificationDetails", context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = notificationDetails.edit();

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString(PARSE_DATA_KEY));
            final String sid = json.getString("group");
            editor.remove(sid);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
