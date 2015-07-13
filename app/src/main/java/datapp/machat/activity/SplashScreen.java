package datapp.machat.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import datapp.machat.R;

public class SplashScreen extends Activity {
    private final int SPLASH_TIME_OUT = 3000;
    private final int LOGO_ANIMATION_DURATION = 2000;
    private final int LOGO_ANIMATION_DELAY = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if(ParseUser.getCurrentUser() != null) {
            Intent mainIntent = new Intent(SplashScreen.this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    moveViewToScreenCenter(findViewById(R.id.logo));
                }
            }, LOGO_ANIMATION_DELAY);

            final Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }
    }

    private void moveViewToScreenCenter( final View view ){
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 66, getResources().getDisplayMetrics());

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float screenHeight = (float) displaymetrics.heightPixels;
        int originalPos[] = new int[2];
        view.getLocationOnScreen( originalPos );

        float yDelta = originalPos[1] - screenHeight;

        AnimationSet animSet = new AnimationSet(true);
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);
        animSet.setDuration(LOGO_ANIMATION_DURATION);
        animSet.setInterpolator(new BounceInterpolator());

        TranslateAnimation toTop = new TranslateAnimation(0, 0, 0, yDelta + px);
        toTop.setFillAfter(true);
        animSet.addAnimation(toTop);
        ScaleAnimation scale = new ScaleAnimation(1f, .667f, 1f, .667f, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
        animSet.addAnimation(scale);
        view.startAnimation(animSet);
    }
}
