package datapp.machat.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.kevinsawicki.timeago.TimeAgo;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.application.MaChatApplication;
import datapp.machat.custom.CircleTransform;
import datapp.machat.helper.SizeHelper;

/**
 * Created by hat on 7/7/15.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {
    private int mLastPosition;
    private LayoutInflater inflater;
    private final String TAG = "MessageAdapter";
    private CircleTransform transformation;
    private Context mContext;
    private int imageWidth;

    public MessageAdapter(Context context, ArrayList<ParseObject> messages) {
        super(context, 0, messages);
        mContext = context;
        inflater = ((Activity) context).getLayoutInflater();
        mLastPosition = -1;
        transformation = new CircleTransform(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ParseObject message = getItem(position);
        ParseObject prevMessage = null;
        if(position - 1 > 0)
            prevMessage = getItem(position - 1);

        //Log.v(TAG, "id: " + position + " sessionId: " + message.getString("sessionId"));

        MessageHolder messageHolder = null;

        if(row == null) {
            row = inflater.inflate(R.layout.message, parent, false);
            messageHolder = new MessageHolder();
            messageHolder.avatar = (ImageView) row.findViewById(R.id.avatar);
            messageHolder.gifSelfiecon = (ImageView) row.findViewById(R.id.selfiecon_content_gif);
            messageHolder.giphyContent = (ImageView) row.findViewById(R.id.giphy_content);
            messageHolder.thumbnailSelfiecon = (ImageView) row.findViewById(R.id.selfiecon_content);
            messageHolder.ytContent = (ImageView) row.findViewById(R.id.yt_content);
            messageHolder.ytPlay = (ImageView) row.findViewById(R.id.yt_play);
            messageHolder.mediaContent = (ImageView) row.findViewById(R.id.media_content);
            messageHolder.mapContent = (ImageView) row.findViewById(R.id.map_content);
            messageHolder.messageDateGiphy = (TextView) row.findViewById(R.id.message_date_giphy);
            messageHolder.messageDateText = (TextView) row.findViewById(R.id.message_date_text);
            messageHolder.messageDateYT = (TextView) row.findViewById(R.id.message_date_yt);
            messageHolder.messageDateVine = (TextView) row.findViewById(R.id.message_date_vine);
            messageHolder.messageDateMedia = (TextView) row.findViewById(R.id.message_date_media);
            messageHolder.messageDateSelfiecon = (TextView) row.findViewById(R.id.message_date_selficon);
            messageHolder.messageDateMap = (TextView) row.findViewById(R.id.message_date_map);
            messageHolder.messageContent = (TextView) row.findViewById(R.id.message_content);
            messageHolder.messageContainer = (LinearLayout) row.findViewById(R.id.message_container);
            messageHolder.iconWrapper = (LinearLayout) row.findViewById(R.id.icon_wrapper);
            messageHolder.messageWrapper = (LinearLayout) row.findViewById(R.id.message_wrapper);
            messageHolder.selfieconWrapper = (LinearLayout) row.findViewById(R.id.selficon_wrapper);
            messageHolder.ytWrapper = (LinearLayout) row.findViewById(R.id.yt_wrapper);
            messageHolder.mediaWrapper = (LinearLayout) row.findViewById(R.id.media_wrapper);
            messageHolder.giphyWrapper = (LinearLayout) row.findViewById(R.id.giphy_wrapper);
            messageHolder.mapWrapper = (LinearLayout) row.findViewById(R.id.map_wrapper);
            messageHolder.vineWrapper = (LinearLayout) row.findViewById(R.id.vine_wrapper);
            messageHolder.vineContent = (VideoView) row.findViewById(R.id.vine_content);
            messageHolder.videoPlay = (Button) row.findViewById(R.id.vine_play);

            final MessageHolder helperHolder = messageHolder;

            messageHolder.vineContent.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(helperHolder.vineContent.isPlaying()){
                        helperHolder.vineContent.pause();
                        helperHolder.videoPlay.setBackground(mContext.getResources().getDrawable(R.mipmap.video_play));
                        Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in_quick);
                        fadeIn.setFillAfter(true);
                        fadeIn.setFillEnabled(true);
                        helperHolder.videoPlay.startAnimation(fadeIn);
                    } else {
                        helperHolder.vineContent.start();
                        helperHolder.videoPlay.setBackground(mContext.getResources().getDrawable(R.mipmap.video_pause));
                        Animation fadeOut = AnimationUtils.loadAnimation(mContext, R.anim.fade_out_quick);
                        fadeOut.setFillAfter(true);
                        fadeOut.setFillEnabled(true);
                        helperHolder.videoPlay.startAnimation(fadeOut);
                    }
                    return true;
                }
            });

            row.setTag(messageHolder);
        } else {
            messageHolder = (MessageHolder) row.getTag();
        }

        final ImageView gifSelfiecon = messageHolder.gifSelfiecon;

        String messageType = message.getString("type");
        Glide.with(mContext)
                .load(message.getParseUser("from").getParseFile("profilePicture").getUrl())
                .centerCrop()
                .crossFade()
                .transform(transformation)
                .into(messageHolder.avatar);

        LinearLayout typeWrapper = null;

        if(messageType.equals("text")){
            typeWrapper = messageHolder.messageWrapper;
            if(message.getCreatedAt() != null)
                messageHolder.messageDateText.setText(new TimeAgo().timeAgo(message.getCreatedAt()));
            else
                messageHolder.messageDateText.setText("Just now");
            messageHolder.messageContent.setText(message.getString("content"));
        } else if(messageType.equals("selfiecon")) {
            typeWrapper = messageHolder.selfieconWrapper;
            if(message.getCreatedAt() != null)
                messageHolder.messageDateSelfiecon.setText(new TimeAgo().timeAgo(message.getCreatedAt()));
            else
                messageHolder.messageDateSelfiecon.setText("Just now");

            Glide.with(mContext)
                    .load(message.getString("content"))
                    .centerCrop()
                    .transform(transformation)
                    .placeholder(R.drawable.circle_bg)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageHolder.thumbnailSelfiecon);
            messageHolder.thumbnailSelfiecon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    view.setVisibility(View.GONE);
                    gifSelfiecon.setVisibility(View.VISIBLE);
                    Glide.with(mContext)
                            .load(message.getString("gifUrl"))
                            .centerCrop().crossFade()
                            .transform(transformation)
                            .placeholder(R.drawable.circle_bg)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(gifSelfiecon);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.setVisibility(View.VISIBLE);
                            gifSelfiecon.setVisibility(View.GONE);
                        }
                    }, 5000);

                }
            });
            messageHolder.messageContainer.setBackgroundColor(Color.TRANSPARENT);
        } else if(messageType.equals("media")){
            typeWrapper = messageHolder.mediaWrapper;
            if(message.getCreatedAt() != null)
                messageHolder.messageDateMedia.setText(new TimeAgo().timeAgo(message.getCreatedAt()));
            else
                messageHolder.messageDateMedia.setText("Just now");

            Glide.with(mContext)
                    .load(message.getString("content"))
                    .centerCrop()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageHolder.mediaContent);

            messageHolder.mediaContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showImage(message.getString("content"));
                }
            });
        } else if(messageType.equals("giphy")){
            typeWrapper = messageHolder.giphyWrapper;
            if(message.getCreatedAt() != null)
                messageHolder.messageDateGiphy.setText(new TimeAgo().timeAgo(message.getCreatedAt()));
            else
                messageHolder.messageDateGiphy.setText("Just now");

            Glide.with(mContext)
                    .load(message.getString("content"))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageHolder.giphyContent);

            messageHolder.giphyContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //showGiphy(message.getString("content"));
                }
            });
        } else if(messageType.equals("map")){
            typeWrapper = messageHolder.mapWrapper;
            if(message.getCreatedAt() != null)
                messageHolder.messageDateMap.setText(new TimeAgo().timeAgo(message.getCreatedAt()));
            else
                messageHolder.messageDateMap.setText("Just now");

            final Double lat = new Double(message.getParseGeoPoint("location").getLatitude());
            final Double lng = new Double(message.getParseGeoPoint("location").getLongitude());

            String url = mContext.getResources().getString(R.string.maps_static)
                    .replace("{lat}", lat.toString()).replace("{lng}", lng.toString());

            Glide.with(mContext)
                    .load(url)
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageHolder.mapContent);

            messageHolder.mapContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMap(Uri.parse("geo:" + lat + "," + lng + "?z=15"));
                }
            });
        } else if(messageType.equals("youtube")){
            typeWrapper = messageHolder.ytWrapper;
            if(message.getCreatedAt() != null)
                messageHolder.messageDateYT.setText(new TimeAgo().timeAgo(message.getCreatedAt()));
            else
                messageHolder.messageDateYT.setText("Just now");

            final String videoId = message.getString("content");

            Glide.with(mContext)
                    .load(mContext.getString(R.string.youtube_thumbnail).replace("{video_id}", videoId))
                    .centerCrop().crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageHolder.ytContent);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    watchYoutubeVideo(videoId);
                }
            };

            messageHolder.ytContent.setOnClickListener(listener);
            messageHolder.ytPlay.setOnClickListener(listener);
        } else if(messageType.equals("vine")){
            typeWrapper = messageHolder.vineWrapper;
            final MessageHolder holder = messageHolder;
            if(message.getCreatedAt() != null)
                messageHolder.messageDateVine.setText(new TimeAgo().timeAgo(message.getCreatedAt()));
            else
                messageHolder.messageDateVine.setText("Just now");

            String vineUrl = message.getString("content");
            messageHolder.vineContent.setVideoURI(Uri.parse(vineUrl));
            messageHolder.vineContent.requestFocus();
        }

        typeWrapper.setVisibility(View.VISIBLE);
        boolean avatarFactor = false;
        /*if(prevMessage != null) {
            if(message.getString("sessionId").equals(prevMessage.getString("sessionId"))) {
                messageHolder.avatar.setVisibility(View.GONE);
                avatarFactor = true;
            }
            else {
                messageHolder.avatar.setVisibility(View.VISIBLE);
                avatarFactor = false;
            }
        } else {
            messageHolder.avatar.setVisibility(View.VISIBLE);
            avatarFactor = false;
        }*/

        if(message.getParseUser("from").getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) typeWrapper.getLayoutParams();
            int margin = (int) ((avatarFactor)? SizeHelper.convertDpToPixel(5f + 40f, mContext):SizeHelper.convertDpToPixel(5f, mContext));
            lp.setMargins(0, 0, margin , 0);
            typeWrapper.setLayoutParams(lp);

            messageHolder.messageContainer.removeAllViews();
            messageHolder.messageContainer.addView(typeWrapper);
            messageHolder.messageContainer.addView(messageHolder.iconWrapper);
            messageHolder.messageContainer.setGravity(Gravity.RIGHT);
        } else {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) typeWrapper.getLayoutParams();
            int margin = (int) ((avatarFactor)? SizeHelper.convertDpToPixel(5f + 40f, mContext):SizeHelper.convertDpToPixel(5f, mContext));
            lp.setMargins(margin, 0, 0, 0);
            typeWrapper.setLayoutParams(lp);

            messageHolder.messageContainer.removeAllViews();
            messageHolder.messageContainer.addView(messageHolder.iconWrapper);
            messageHolder.messageContainer.addView(typeWrapper);
            messageHolder.messageContainer.setGravity(Gravity.LEFT);
        }

        if (position > mLastPosition) {
            Animation bounceInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in_quick);
            row.startAnimation(bounceInAnimation);
            mLastPosition = position;
        }

        return row;

    }

    public void watchYoutubeVideo(String id){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            mContext.startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent=new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v="+id));
            mContext.startActivity(intent);
        }
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(intent);
        }
    }

    public void showImage(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "image/*");
        mContext.startActivity(intent);
    }

    static class MessageHolder {
        LinearLayout messageContainer;

        LinearLayout iconWrapper;
        ImageView avatar;

        LinearLayout messageWrapper;
        TextView messageContent;
        TextView messageDateText;

        LinearLayout selfieconWrapper;
        ImageView thumbnailSelfiecon;
        ImageView gifSelfiecon;
        TextView messageDateSelfiecon;

        LinearLayout mediaWrapper;
        ImageView mediaContent;
        TextView messageDateMedia;

        LinearLayout giphyWrapper;
        ImageView giphyContent;
        TextView messageDateGiphy;

        LinearLayout vineWrapper;
        VideoView vineContent;
        TextView messageDateVine;
        Button videoPlay;

        LinearLayout ytWrapper;
        ImageView ytContent;
        ImageView ytPlay;
        TextView messageDateYT;

        LinearLayout mapWrapper;
        ImageView mapContent;
        TextView messageDateMap;
    }
}
