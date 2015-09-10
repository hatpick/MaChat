package datapp.machat.helper;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import datapp.machat.R;
import datapp.machat.custom.NotificationReceiver;

/**
 * Created by hat on 7/12/15.
 */
public class SendNotification extends AsyncTask<String, Void, Bitmap> {
    private String sessionId;
    private Context context;
    private String title;
    private String content;
    private String url;
    private int id;
    private String senderId;
    private final static Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private static NotificationManagerCompat notificationManager;
    private Intent intent;

    private HashMap<String, Integer> sessionNotifs;

    public static void cancelNotification(int id) {
        if(notificationManager != null)
            notificationManager.cancel(id);
    }

    public SendNotification(Context context, Intent intent, String senderId, String title, String content, String url, int id, String sid) {
        this.context = context;
        this.title = title;
        this.content = content;
        this.url = url;
        this.id = id;
        this.senderId = senderId;
        this.intent = intent;
        this.sessionId = sid;
        notificationManager = NotificationManagerCompat.from(context);
        sessionNotifs = new HashMap<>();
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        InputStream in;
        try {
            URL _url = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) _url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(in);
            return Bitmap.createScaledBitmap(myBitmap, 96, 96, false);
        } catch (IOException e) {
            e.printStackTrace();
            return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_notif);
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        Intent oIntent = new Intent(NotificationReceiver.ACTION_PUSH_OPEN);
        Bundle extras = intent.getExtras();
        extras.putBoolean("notification", true);
        extras.putString("sid", sessionId);
        extras.putString("receiverFbId", senderId);
        extras.putInt("nid", id);
        oIntent.putExtras(extras);
        oIntent.setPackage(context.getPackageName());

        Intent dIntent = new Intent(NotificationReceiver.ACTION_PUSH_DELETE);
        dIntent.putExtras(intent.getExtras());
        dIntent.setPackage(context.getPackageName());

        PendingIntent oContentIntent = PendingIntent.getBroadcast(context, 0, oIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent dContentIntent = PendingIntent.getBroadcast(context, 0, dIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_notif)
                .setLargeIcon(bitmap)
                .setSound(uri)
                .setCategory(Notification.CATEGORY_SOCIAL)

                .setContentIntent(oContentIntent).setDeleteIntent(dContentIntent)
                .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
        notificationManager.notify(id, notification);
    }
}
