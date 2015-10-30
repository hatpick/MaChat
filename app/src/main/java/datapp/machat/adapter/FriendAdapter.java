package datapp.machat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import datapp.machat.R;
import datapp.machat.activity.ChatActivity;
import datapp.machat.activity.MainActivity;
import datapp.machat.application.MaChatApplication;
import datapp.machat.dao.Friend;
import datapp.machat.helper.BlurBehind.BlurBehind;
import datapp.machat.helper.BlurBehind.OnBlurCompleteListener;
import datapp.machat.helper.MyProfilePictureView;
import datapp.machat.helper.SizeHelper;

/**
 * Created by hat on 10/29/15.
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendHolder> {
    private final int imageWidth;
    private Context mContext;
    private ArrayList<Friend> dataList;
    private final String TAG = "FriendAdapter";
    private String callee;
    private String vineUrl;
    private String youtubeUrl;

    public FriendAdapter(Context mContext, ArrayList<Friend> friends, String callee) {
        dataList = friends;
        this.mContext = mContext;
        this.callee = callee;

        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        int totalMargin = 0;// (int) SizeHelper.convertDpToPixel(32.0f, mContext);
        imageWidth = (width - totalMargin)/3;
    }

    @Override
    public FriendHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.friend_contact, viewGroup, false);
        FriendHolder friendHolder = new FriendHolder(v, imageWidth, new FriendHolder.IMyViewHolderClicks() {
            @Override
            public void onClick(View caller, int position) {
                final Friend selectedFriend = dataList.get(position);

                if(callee.equals("main")) {
                    BlurBehind.getInstance().execute((Activity) mContext, new OnBlurCompleteListener() {
                        @Override
                        public void onBlurComplete() {
                            Intent chatIntent = new Intent(mContext, ChatActivity.class);
                            chatIntent.putExtra("receiverFbId", selectedFriend.getFbId());
                            chatIntent.putExtra("senderFbId", ParseUser.getCurrentUser().getString("fbId"));
                            chatIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            chatIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(chatIntent);
                        }
                    });
                } else {
                    if(youtubeUrl != null)
                        shareYoutubeUrl(selectedFriend, youtubeUrl);
                    else
                        shareVineUrl(selectedFriend, vineUrl);
                }
            }
        });

        return friendHolder;
    }

    public void setVine(String url){
        this.vineUrl = url;
    }

    public void setYoutube(String url){
        this.youtubeUrl = url;

    }

    private void shareVineUrl(final Friend receiver, final String vineUrl) {
        String content = vineUrl;
        if(SizeHelper.isVine(vineUrl)) {
            JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, "http://web.engr.oregonstate.edu/~ghorashi/vine/vine.php?url=" + content, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        sendOrnotify("vine", response.getString("video"), receiver);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            MaChatApplication.getInstance().getRequestQueue().add(jor);
        } else {
            Toast.makeText(mContext, "Share failed!", Toast.LENGTH_SHORT).show();
            ((Activity)mContext).finish();
        }
    }

    private void sendOrnotify(final String type, final String content, Friend receiver){
        ParseQuery query = ParseUser.getQuery();
        query.whereEqualTo("fbId", receiver.getFbId());
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null && parseUser != null) {
                    final ParseUser receiver = parseUser;
                    ParseUser sender = ParseUser.getCurrentUser();
                    if (!receiver.getBoolean("inApp")) {
                        HashMap<String, Object> params = new HashMap<String, Object>();
                        params.put("toId", receiver.getObjectId());
                        params.put("msgType", type);
                        params.put("msgContent", content);
                        params.put("toId", receiver.getObjectId());
                        ParseCloud.callFunctionInBackground("sendPushMessage", params, new FunctionCallback<String>() {
                            @Override
                            public void done(String s, ParseException e) {
                                if (e != null) {
                                    Toast.makeText(mContext, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                    Intent intent = new Intent(mContext, MainActivity.class);
                                    mContext.startActivity(intent);
                                    ((Activity)mContext).finish();
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
                                        if (e == null) {
                                            Toast.makeText(mContext, type.toUpperCase() + " video is shared with " + receiver.getString("fName") + ".", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(mContext, MainActivity.class);
                                            intent.putExtra("shared", true);
                                            intent.putExtra("receiverFbId", receiver.getString("fbId"));
                                            mContext.startActivity(intent);
                                        } else {
                                            Toast.makeText(mContext, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(mContext, MainActivity.class);
                                            mContext.startActivity(intent);
                                        }
                                        ((Activity)mContext).finish();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    ((Activity)mContext).finish();
                    Toast.makeText(mContext, "Error loading chat history!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void shareYoutubeUrl(Friend receiver, final String youtubeUrl) {
        final String videoId = SizeHelper.extractYTId(youtubeUrl);
        String type;
        String content;
        if(videoId != null) {
            type = "youtube";
            content = videoId;
            sendOrnotify(type, content, receiver);
        } else {
            Toast.makeText(mContext, "Share failed!", Toast.LENGTH_SHORT).show();
            ((Activity)mContext).finish();
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void remove(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    public void add(Friend f, int position) {
        dataList.add(position, f);
        notifyItemInserted(position);
    }

    @Override
    public void onBindViewHolder(FriendHolder holder, int position) {
        Friend friend = dataList.get(position);
        holder.ppView.setProfileId(friend.getFbId());
        holder.nameView.setText(friend.getName().split(" ")[0]);
    }

    static class FriendHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        public MyProfilePictureView ppView;
        public TextView nameView;
        public LinearLayout holder;
        public IMyViewHolderClicks mListener;

        FriendHolder(View view, int width, IMyViewHolderClicks listener) {
            super(view);
            this.mListener = listener;
            this.ppView = (MyProfilePictureView)view.findViewById(R.id.friend_avatar);
            this.nameView = (TextView) view.findViewById(R.id.friend_name);
            this.holder = (LinearLayout) view.findViewById(R.id.friend_holder);
            this.holder.getLayoutParams().width = width;

            this.holder.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v, this.getAdapterPosition());
        }

        public interface IMyViewHolderClicks {
            void onClick(View caller, int position);
        }
    }
}
