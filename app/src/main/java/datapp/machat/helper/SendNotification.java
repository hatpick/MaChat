package datapp.machat.helper;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import datapp.machat.R;

/**
 * Created by hat on 7/12/15.
 */
public class SendNotification extends AsyncTask<String, Void, Bitmap> {
    private Context context;
    private String title;
    private String content;
    private String url;
    private int id;
    private Intent intent;

    public SendNotification(Context context, Intent intent, String title, String content, String url, int id) {
        this.context = context;
        this.title = title;
        this.content = content;
        this.url = url;
        this.id = id;
        this.intent = intent;
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

        Bundle extras = intent.getExtras();
        Random random = new Random();
        int contentIntentRequestCode = random.nextInt();
        int deleteIntentRequestCode = random.nextInt();
        String packageName = context.getPackageName();
        Intent contentIntent = new Intent("com.parse.push.intent.OPEN");
        contentIntent.putExtras(extras);
        contentIntent.setPackage(packageName);
        Intent deleteIntent = new Intent("com.parse.push.intent.DELETE");
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);
        PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setContentIntent(pContentIntent).setDeleteIntent(pDeleteIntent)
                .build();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        notificationManager.notify(id, notification);
    }
}
