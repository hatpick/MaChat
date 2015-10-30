package datapp.machat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.faradaj.blurbehind.BlurBehind;
import com.faradaj.blurbehind.OnBlurCompleteListener;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.victor.loading.newton.NewtonCradleLoading;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.adapter.FriendAdapter;
import datapp.machat.custom.CircleTransform;
import datapp.machat.custom.CustomActivity;
import datapp.machat.custom.UserStatus;
import datapp.machat.dao.Friend;
import datapp.machat.helper.SizeHelper;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

public class MainActivity extends CustomActivity {
    private ArrayList<ParseUser> friends = new ArrayList<>();
    private RecyclerView friendsListView;
    private CircleTransform transformation;

    private ArrayList<Friend> friendsArray;
    private FriendAdapter friendAdapter;
    private boolean isFromShare = false;
    private StaggeredGridLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        friendsListView = (RecyclerView) findViewById(R.id.friendView);
        mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        friendsListView.setItemAnimator(new ScaleInAnimator(new OvershootInterpolator(1f)));
        friendsListView.getItemAnimator().setAddDuration(400);
        friendsListView.setHasFixedSize(true);
        friendsListView.setLayoutManager(mLayoutManager);

        friendsArray = new ArrayList<>();
        friendAdapter = new FriendAdapter(this, friendsArray, "main");
        friendsListView.setAdapter(friendAdapter);
        _setupActionBar();
        _setupNotification();
        _setupFriendList();
        //setPadding(mainContainer);
    }

    private boolean isFromNotification = false;

    private void _setupFriendList() {
        friendsArray.clear();
        JSONArray friends = null;
        try {
            friends = new JSONArray(ParseUser.getCurrentUser().getString("friends"));
            if(friends.length() > 0) {
                try {
                    JSONObject _friend;
                    Friend friend;
                    for (int i = 0; i < friends.length(); i++) {
                        _friend = (JSONObject) friends.get(i);
                        friend = new Friend(_friend.getString("name"), _friend.getString("id"));
                        friendAdapter.add(friend, friendAdapter.getItemCount());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            Toast.makeText(MainActivity.this, "Loading friends failed!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void _setupNotification(){
        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    ParseInstallation.getCurrentInstallation().put("user", ParseUser.getCurrentUser().getObjectId());
                    ParseInstallation.getCurrentInstallation().saveInBackground();
                }
            }
        });
    }

    private void _setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        LinearLayout linearLayout = new LinearLayout(actionBar.getThemedContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        //App name textview
        LinearLayout textLinearLayout = new LinearLayout(actionBar.getThemedContext());
        textLinearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        TextView user_name = new TextView(actionBar.getThemedContext());
        user_name.setText(ParseUser.getCurrentUser().getString("fName") + " " + ParseUser.getCurrentUser().getString("lName"));
        user_name.setPadding(20, 0, 0, 0);
        user_name.setTextSize(16);
        ActionBar.LayoutParams txtLayoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT , Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        txtLayoutParams.leftMargin = 20;
        user_name.setLayoutParams(txtLayoutParams);

        TextView user_email = new TextView(actionBar.getThemedContext());
        user_email.setText(ParseUser.getCurrentUser().getString("email"));
        user_email.setPadding(20, 0, 0, 0);
        user_email.setTextSize(14);
        user_email.setLayoutParams(txtLayoutParams);

        textLinearLayout.addView(user_name);
        textLinearLayout.addView(user_email);

        ImageView pp_picture = new ImageView(actionBar.getThemedContext());
        transformation = new CircleTransform(this);

        Glide.with(this)
                .load(ParseUser.getCurrentUser().getParseFile("profilePicture").getUrl())
                .centerCrop()
                .transform(transformation)
                .into(pp_picture);
        ActionBar.LayoutParams imgLayoutParams = new ActionBar.LayoutParams((int) SizeHelper.convertDpToPixel(40, this), (int) SizeHelper.convertDpToPixel(40, this), Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        pp_picture.setLayoutParams(imgLayoutParams);

        linearLayout.addView(pp_picture);
        linearLayout.addView(textLinearLayout);


        actionBar.setCustomView(linearLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ParseUser.getCurrentUser() != null) {
            UserStatus.setUserOnline();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isFromNotification = intent.getBooleanExtra("notification", false);
        isFromShare = intent.getBooleanExtra("shared", false);
        if(isFromNotification) {
            final String receiverFbId = intent.getStringExtra("receiverFbId");
            Handler handler = new Handler();
            final ProgressDialog dia = new ProgressDialog(MainActivity.this);
            dia.show();
            dia.setContentView(R.layout.progress_dialog);
            dia.getWindow().setBackgroundDrawable(null);
            NewtonCradleLoading progressBar = (NewtonCradleLoading ) dia.findViewById(R.id.pd_progressBar);
            progressBar.start();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BlurBehind.getInstance().execute(MainActivity.this, new OnBlurCompleteListener() {
                        @Override
                        public void onBlurComplete() {
                            Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                            chatIntent.putExtra("receiverFbId", receiverFbId);
                            chatIntent.putExtra("senderFbId", ParseUser.getCurrentUser().getString("fbId"));
                            chatIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(chatIntent);
                            dia.dismiss();
                        }
                    });
                }
            }, 1000);
        } else if(isFromShare) {
            final String receiverFbId = intent.getStringExtra("receiverFbId");
            Handler handler = new Handler();
            final ProgressDialog dia = new ProgressDialog(MainActivity.this);
            dia.setProgressStyle(android.R.attr.progressBarStyleSmall);
            dia.show();
            dia.setContentView(R.layout.progress_dialog);
            dia.getWindow().setBackgroundDrawable(null);
            NewtonCradleLoading progressBar = (NewtonCradleLoading ) dia.findViewById(R.id.pd_progressBar);
            progressBar.start();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BlurBehind.getInstance().execute(MainActivity.this, new OnBlurCompleteListener() {
                        @Override
                        public void onBlurComplete() {
                            Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                            chatIntent.putExtra("receiverFbId", receiverFbId);
                            chatIntent.putExtra("senderFbId", ParseUser.getCurrentUser().getString("fbId"));
                            chatIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(chatIntent);
                            dia.dismiss();
                        }
                    });
                }
            }, 1000);
        }

    }

    @Override
    protected void onPause() {
        if(ParseUser.getCurrentUser() != null) {
            UserStatus.setUserOffline();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            ParseUser.getCurrentUser().put("inApp", false);
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    ParseUser.logOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
