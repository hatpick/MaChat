package datapp.machat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import datapp.machat.R;
import datapp.machat.activity.MainActivity;
import datapp.machat.dao.Friend;

/**
 * Created by hat on 7/6/15.
 */
public class FriendListAdapter extends ArrayAdapter<Friend>{
    private int mLastPosition;
    private LayoutInflater inflater;
    private final String TAG = "FriendListAdapter";
    private Transformation transformation;

    public FriendListAdapter(Context context, int resource, ArrayList<Friend> friends) {
        super(context, 0, friends);
        inflater = ((Activity) context).getLayoutInflater();
        mLastPosition = -1;
        transformation = new RoundedTransformationBuilder()
                .borderColor(Color.parseColor("#ccffffff"))
                .borderWidthDp(3)
                .cornerRadiusDp(100)
                .oval(false)
                .build();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final Friend friend = getItem(position);
        FriendHolder friendHolder;
        if(row == null) {
            row = inflater.inflate(R.layout.friend_contact, parent, false);
            friendHolder = new FriendHolder();
            friendHolder.friendName = (TextView) row.findViewById(R.id.friend_name);
            friendHolder.friendAvatar = (ImageView) row.findViewById(R.id.friend_avatar);

            row.setTag(friendHolder);
        } else {
            friendHolder = (FriendHolder) row.getTag();
        }

        final FriendHolder refHolder = friendHolder;

        friendHolder.friendName.setText(friend.getName());

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("fbId", friend.getFbId());
        ParseCloud.callFunctionInBackground("getFacebookImgByfbId", params, new FunctionCallback<String>() {
            @Override
            public void done(String s, ParseException e) {
                if (e == null) {
                    try {
                        JSONObject friendObj = new JSONObject(s);
                        Picasso.with(getContext())
                                .load(friendObj.getJSONObject("profilePicture").getString("url"))
                                .fit()
                                .transform(transformation)
                                .into(refHolder.friendAvatar);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        return row;
    }

    static class FriendHolder {
        TextView friendName;
        ImageView friendAvatar;
    }
}
