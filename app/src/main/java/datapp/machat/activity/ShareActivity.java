package datapp.machat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import datapp.machat.R;
import datapp.machat.adapter.FriendListAdapter;
import datapp.machat.custom.CustomActivity;
import datapp.machat.dao.Friend;
import datapp.machat.helper.SizeHelper;

public class ShareActivity extends CustomActivity{
    private GridView friendsListView;

    private ArrayList<Friend> friendsArray;
    private FriendListAdapter friendListAdapter;
    private String youtubeUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        friendsListView = (GridView) findViewById(R.id.friendView);
        friendsArray = new ArrayList<>();
        friendListAdapter = new FriendListAdapter(this, R.layout.friend_contact, friendsArray);
        friendsListView.setAdapter(friendListAdapter);
        _setupFriendList();

        //setPadding(mainContainer);

        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Friend receiver = friendListAdapter.getItem(i);
                shareYoutubeUrl(receiver, youtubeUrl);
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            youtubeUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
    }

    private void shareYoutubeUrl(Friend receiver, final String youtubeUrl) {
        final String videoId = SizeHelper.extractYTId(youtubeUrl);
        ParseQuery query = ParseUser.getQuery();
        query.whereEqualTo("fbId", receiver.getFbId());
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null && parseUser != null) {
                    String type = "text";
                    String content = youtubeUrl;
                    if(videoId != null) {
                        type = "youtube";
                        content = videoId;
                    } else {
                        Toast.makeText(ShareActivity.this, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    final ParseUser receiver = parseUser;
                    ParseUser sender = ParseUser.getCurrentUser();
                    if(!receiver.getBoolean("inApp")) {
                        HashMap<String, Object> params = new HashMap<String, Object>();
                        params.put("toId", receiver.getObjectId());
                        params.put("msgType", type);
                        params.put("msgContent", content);
                        params.put("toId", receiver.getObjectId());
                        ParseCloud.callFunctionInBackground("sendPushMessage", params, new FunctionCallback<String>() {
                            @Override
                            public void done(String s, ParseException e) {
                                if (e != null) {
                                    Toast.makeText(ShareActivity.this, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                    Intent intent = new Intent(ShareActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }

                    final ParseObject message = new ParseObject("Message");
                    message.put("from", sender);
                    message.put("to", receiver);
                    message.put("content", content);
                    message.put("type", type);
                    message.put("sessionId", sender.getObjectId() + receiver.getObjectId());
                    message.put("status", "sent");

                    message.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                message.put("status", "delivered");
                                message.saveEventually(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null){
                                            Toast.makeText(ShareActivity.this, "Youtube video is shared with " + receiver.getString("fName") + ".", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ShareActivity.this, MainActivity.class);
                                            intent.putExtra("shared", true);
                                            intent.putExtra("receiverFbId", receiver.getString("fbId"));
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(ShareActivity.this, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ShareActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    finish();
                    Toast.makeText(ShareActivity.this, "Error loading chat history!", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                        friendsArray.add(friend);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                friendListAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Toast.makeText(ShareActivity.this, "Loading friends failed!", Toast.LENGTH_SHORT).show();
            friendListAdapter.notifyDataSetChanged();
            e.printStackTrace();
        }
    }
}
