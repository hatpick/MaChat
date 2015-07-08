package datapp.machat.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import datapp.machat.R;
import datapp.machat.dao.Friend;
import datapp.machat.helper.CircleTransform;

/**
 * Created by hat on 7/6/15.
 */
public class FriendListAdapter extends ArrayAdapter<Friend>{
    private int mLastPosition;
    private LayoutInflater inflater;
    private final String TAG = "FriendListAdapter";
    private CircleTransform transformation;
    private Context mContext;

    public FriendListAdapter(Context context, int resource, ArrayList<Friend> friends) {
        super(context, 0, friends);
        mContext = context;
        inflater = ((Activity) context).getLayoutInflater();
        mLastPosition = -1;
        transformation = new CircleTransform(context);
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
                        Glide.with(getContext())
                                .load(friendObj.getJSONObject("profilePicture").getString("url"))
                                .centerCrop()
                                .crossFade()
                                .transform(transformation)
                                .into(refHolder.friendAvatar);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        if (position > mLastPosition) {
            Animation bounceInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.bounce_in);
            row.startAnimation(bounceInAnimation);
            mLastPosition = position;
        }

        return row;
    }

    static class FriendHolder {
        TextView friendName;
        ImageView friendAvatar;
    }
}
