package datapp.machat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.adapter.FriendAdapter;
import datapp.machat.application.MaChatApplication;
import datapp.machat.custom.CustomActivity;
import datapp.machat.dao.Friend;
import datapp.machat.dao.MaChatTheme;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

public class ShareActivity extends CustomActivity{
    private RecyclerView friendsListView;

    private ArrayList<Friend> friendsArray;
    private FriendAdapter friendListAdapter;
    private StaggeredGridLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        String themeName = getSharedPreferences("Theme", Context.MODE_PRIVATE).getString("Theme", "Default");
        MaChatTheme theme = MaChatApplication.getInstance().getThemeByName(themeName);
        getWindow().getDecorView().setBackgroundResource(theme.getId());

        friendsListView = (RecyclerView) findViewById(R.id.friendView);
        mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        friendsListView.setItemAnimator(new ScaleInAnimator(new OvershootInterpolator(1f)));
        friendsListView.getItemAnimator().setAddDuration(400);
        friendsListView.setHasFixedSize(true);
        friendsListView.setLayoutManager(mLayoutManager);
        friendsArray = new ArrayList<>();
        friendListAdapter = new FriendAdapter(this, friendsArray, "share");
        friendsListView.setAdapter(friendListAdapter);

        _setupFriendList();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            String url = intent.getStringExtra(Intent.EXTRA_TEXT);
            if(url.contains("vine")){
                friendListAdapter.setVine(url);
            } else {
                friendListAdapter.setYoutube(url);
            }
        }

    }

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
                        friendListAdapter.add(friend, friendListAdapter.getItemCount());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            Toast.makeText(ShareActivity.this, "Loading friends failed!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
