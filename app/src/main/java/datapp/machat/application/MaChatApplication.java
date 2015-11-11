package datapp.machat.application;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crittercism.app.Crittercism;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.PushService;

import java.io.File;
import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.activity.MainActivity;
import datapp.machat.dao.MaChatTheme;

/**
 * Created by hat on 6/30/15.
 */
public class MaChatApplication extends Application {
    private static MaChatApplication ourInstance;
    private static String path = "/MaChat";
    private RequestQueue mRequestQueue;

    //TODO: Hash
    private ArrayList<MaChatTheme> themes;
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
        if(!myDir.exists())
            myDir.mkdirs();

        String lastTheme = getSharedPreferences("Theme", Context.MODE_PRIVATE).getString("Theme", null);
        if(lastTheme == null) {
            getSharedPreferences("Theme", Context.MODE_PRIVATE).edit().putString("LastTheme", "Default").commit();
            getSharedPreferences("Theme", Context.MODE_PRIVATE).edit().putString("Theme", "Default").commit();
            getSharedPreferences("Theme", Context.MODE_PRIVATE).edit().apply();
        }

        themes = new ArrayList<>();

        MaChatTheme themeDef = new MaChatTheme("Default", R.drawable.bg_alt, R.color.theme_def_color1, R.color.theme_def_color2, R.color.theme_def_color, R.color.theme_def_ref);
        MaChatTheme themeOne = new MaChatTheme("ThemeOne", R.drawable.theme_one_bg, R.color.theme_one_color1, R.color.theme_one_color2, R.color.theme_one_color, R.color.theme_one_ref);
        MaChatTheme themeTwo = new MaChatTheme("ThemeTwo", R.drawable.theme_two_bg, R.color.theme_two_color1, R.color.theme_two_color2, R.color.theme_two_color, R.color.theme_two_ref);
        MaChatTheme themeThree = new MaChatTheme("ThemeThree", R.drawable.theme_three_bg, R.color.theme_three_color1, R.color.theme_three_color2, R.color.theme_three_color, R.color.theme_three_ref);
        MaChatTheme themeFour= new MaChatTheme("ThemeFour", R.drawable.theme_four_bg, R.color.theme_four_color1, R.color.theme_four_color2, R.color.theme_four_color, R.color.theme_four_ref);
        MaChatTheme themeFive= new MaChatTheme("ThemeFive", R.drawable.theme_five_bg, R.color.theme_five_color1, R.color.theme_five_color2, R.color.theme_five_color, R.color.theme_five_ref);
        MaChatTheme themeSix= new MaChatTheme("ThemeSix", R.drawable.theme_six_bg, R.color.theme_six_color1, R.color.theme_six_color2, R.color.theme_six_color, R.color.theme_six_ref);
        MaChatTheme themeSeven= new MaChatTheme("ThemeSeven", R.drawable.theme_seven_bg, R.color.theme_seven_color1, R.color.theme_seven_color2, R.color.theme_seven_color, R.color.theme_seven_ref);
        MaChatTheme themeVirgin = new MaChatTheme("ThemeVirgin", R.drawable.theme_virgin_bg, R.color.theme_virgin_color1, R.color.theme_virgin_color2, R.color.theme_virgin_color, R.color.theme_virgin_ref);
        MaChatTheme themeTitanium = new MaChatTheme("ThemeTitanium", R.drawable.theme_titanium_bg, R.color.theme_titanium_color1, R.color.theme_titanium_color2, R.color.theme_titanium_color, R.color.theme_titanium_ref);
        MaChatTheme themeAutumn = new MaChatTheme("ThemeAutumn", R.drawable.theme_autumn_bg, R.color.theme_autumn_color1, R.color.theme_autumn_color2, R.color.theme_autumn_color, R.color.theme_autumn_ref);
        MaChatTheme themeClouds = new MaChatTheme("ThemeClouds", R.drawable.theme_clouds_bg, R.color.theme_clouds_color1, R.color.theme_clouds_color2, R.color.theme_clouds_color, R.color.theme_clouds_ref);

        themes.add(themeDef);
        themes.add(themeOne);
        themes.add(themeTwo);
        themes.add(themeThree);
        themes.add(themeFour);
        themes.add(themeFive);
        themes.add(themeSix);
        themes.add(themeSeven);
        themes.add(themeVirgin);
        themes.add(themeTitanium);
        themes.add(themeAutumn);
        themes.add(themeClouds);
    }

    public ArrayList<MaChatTheme> getThemes() {
        return themes;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public MaChatTheme getThemeByName(String name) {
        for(MaChatTheme theme:themes) {
            if(theme.getName().equals(name)) return theme;
        }
        return null;
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
