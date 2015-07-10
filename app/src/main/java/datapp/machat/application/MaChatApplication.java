package datapp.machat.application;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.PushService;

import datapp.machat.R;
import datapp.machat.activity.MainActivity;

/**
 * Created by hat on 6/30/15.
 */
public class MaChatApplication extends Application {
    private static MaChatApplication ourInstance;

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
    }
}
