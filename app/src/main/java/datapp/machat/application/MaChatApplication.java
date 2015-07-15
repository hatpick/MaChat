package datapp.machat.application;

import android.app.Application;
import android.os.Environment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crittercism.app.Crittercism;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.PushService;

import java.io.File;

import datapp.machat.R;
import datapp.machat.activity.MainActivity;

/**
 * Created by hat on 6/30/15.
 */
public class MaChatApplication extends Application {
    private static MaChatApplication ourInstance;
    private static String path = "/MaChat";
    private RequestQueue mRequestQueue;
    public static String getPath() {
        return path;
    }

    public static MaChatApplication getInstance() {
        return ourInstance;
    }

    public MaChatApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ourInstance = this;
        Parse.initialize(this, getString(R.string.parse_first_arg), getString(R.string.parse_second_arg));
        ParseFacebookUtils.initialize(this.getApplicationContext());
        PushService.setDefaultPushCallback(this, MainActivity.class);
        Crittercism.initialize(getApplicationContext(), getString(R.string.crittercism_app_id));

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + path);
        myDir.mkdirs();
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
