package datapp.machat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import com.parse.ParseUser;

import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.custom.CustomActivity;

public class MainActivity extends CustomActivity {
    private ArrayList<ParseUser> friends = new ArrayList<>();
    private GridView friendsList;
    private LinearLayout mainContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        friendsList = (GridView) findViewById(R.id.friendView);
        mainContainer = (LinearLayout) findViewById(R.id.main_container);

        setPadding(mainContainer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void setPadding(View v){
        int actionBarHeight = 0, statusBarHeight = 0, defaultPadding = 0;
        defaultPadding = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin)/2;
        statusBarHeight = getStatusBarHeight();
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        int dpAsPixels = statusBarHeight + defaultPadding + actionBarHeight;
        v.setPadding(v.getPaddingLeft(),dpAsPixels, v.getPaddingRight(), v.getPaddingBottom());

    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            ParseUser.logOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
