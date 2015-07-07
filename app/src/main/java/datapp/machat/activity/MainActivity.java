package datapp.machat.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.adapter.FriendListAdapter;
import datapp.machat.custom.CustomActivity;
import datapp.machat.dao.Friend;

public class MainActivity extends CustomActivity {
    private ArrayList<ParseUser> friends = new ArrayList<>();
    private GridView friendsListView;
    private LinearLayout mainContainer;
    private Transformation transformation;

    private ArrayList<Friend> friendsArray;
    private FriendListAdapter friendListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        friendsListView = (GridView) findViewById(R.id.friendView);
        mainContainer = (LinearLayout) findViewById(R.id.main_container);

        friendsArray = new ArrayList<>();
        friendListAdapter = new FriendListAdapter(this, R.layout.friend_contact, friendsArray);
        friendsListView.setAdapter(friendListAdapter);
        _setupActionBar();
        _setupFriendList();

        setPadding(mainContainer);
    }

    private void _setupFriendList() {
        JSONArray friends = ParseUser.getCurrentUser().getJSONArray("friends");
        if(friends.length() > 0) {
            try {
                JSONObject _friend;
                Friend friend;
                for (int i = 0; i < friends.length(); i++) {
                    _friend = (JSONObject) ((JSONArray) (friends.get(i))).get(0);
                    friend = new Friend(_friend.getString("name"), _friend.getString("id"));
                    friendsArray.add(friend);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            friendListAdapter.notifyDataSetChanged();
        }

        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO: go to chat session with friend
                Friend friend = friendListAdapter.getItem(i);
            }
        });
    }

    private void _setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        LinearLayout linearLayout = new LinearLayout(actionBar.getThemedContext());
        linearLayout.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        //App name textview
        TextView app_name = new TextView(actionBar.getThemedContext());
        app_name.setText(getString(R.string.app_name));
        app_name.setPadding(20, 0, 0, 0);
        app_name.setTextSize(17);
        ActionBar.LayoutParams txtLayoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT , Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        txtLayoutParams.leftMargin = 20;
        app_name.setLayoutParams(txtLayoutParams);

        ImageView pp_picture = new ImageView(actionBar.getThemedContext());transformation = new RoundedTransformationBuilder()
                .borderColor(Color.parseColor("#ffffff"))
                .borderWidthDp(1)
                .cornerRadiusDp(30)
                .oval(false)
                .build();

        Picasso.with(this)
                .load(ParseUser.getCurrentUser().getParseFile("profilePicture").getUrl())
                .fit()
                .transform(transformation)
                .into(pp_picture);
        ActionBar.LayoutParams imgLayoutParams = new ActionBar.LayoutParams(convertDpToPixel(40, this), convertDpToPixel(40, this) , Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        pp_picture.setLayoutParams(imgLayoutParams);

        linearLayout.addView(pp_picture);
        linearLayout.addView(app_name);


        actionBar.setCustomView(linearLayout);
    }

    public static int convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int)px;
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
