package datapp.machat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.kevinsawicki.timeago.TimeAgo;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.custom.CircleTransform;

/**
 * Created by hat on 7/7/15.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {
    private int mLastPosition;
    private LayoutInflater inflater;
    private final String TAG = "MessageAdapter";
    private CircleTransform transformation;
    private Context mContext;

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

        MessageHolder messageHolder = null;

        if(row == null) {
            row = inflater.inflate(R.layout.message, parent, false);
            messageHolder = new MessageHolder();
            messageHolder.avatar = (ImageView) row.findViewById(R.id.avatar);
            messageHolder.gifSelfiecon = (ImageView) row.findViewById(R.id.selfiecon_content);
            messageHolder.mediaContent = (ImageView) row.findViewById(R.id.media_content);
            messageHolder.messageDateText = (TextView) row.findViewById(R.id.message_date_text);
            messageHolder.messageDateMedia = (TextView) row.findViewById(R.id.message_date_media);
            messageHolder.messageDateSelfiecon = (TextView) row.findViewById(R.id.message_date_selficon);
            messageHolder.messageContent = (TextView) row.findViewById(R.id.message_content);
            messageHolder.messageContainer = (LinearLayout) row.findViewById(R.id.message_container);
            messageHolder.iconWrapper = (LinearLayout) row.findViewById(R.id.icon_wrapper);
            messageHolder.messageWrapper = (LinearLayout) row.findViewById(R.id.message_wrapper);
            messageHolder.selfieconWrapper = (LinearLayout) row.findViewById(R.id.selficon_wrapper);
            messageHolder.mediaWrapper= (LinearLayout) row.findViewById(R.id.media_wrapper);

            row.setTag(messageHolder);
        } else {
            messageHolder = (MessageHolder) row.getTag();
        }

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
                    .transform(transformation)
                    .placeholder(R.drawable.circle_bg)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageHolder.gifSelfiecon);
            messageHolder.messageContainer.setBackgroundColor(Color.TRANSPARENT);
        } else if(messageType.equals("media")){
            typeWrapper = messageHolder.mediaWrapper;
            if(message.getCreatedAt() != null)
                messageHolder.messageDateMedia.setText(new TimeAgo().timeAgo(message.getCreatedAt()));
            else
                messageHolder.messageDateMedia.setText("Just now");

            Glide.with(mContext)
                    .load(message.getString("content"))
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageHolder.mediaContent);

            messageHolder.mediaContent.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            ImageView view = (ImageView) v;
                            //overlay is black with transparency of 0x77 (119)
                            view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                            view.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            ImageView view = (ImageView) v;
                            //clear the overlay
                            view.getDrawable().clearColorFilter();
                            view.invalidate();
                            break;
                        }
                    }

                    return true;
                }
            });
        }

        typeWrapper.setVisibility(View.VISIBLE);

        if(message.getParseUser("from").getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) typeWrapper.getLayoutParams();
            lp.setMargins(0, 0, (int) (5 * mContext.getResources().getDisplayMetrics().density + 0.5f), 0);
            typeWrapper.setLayoutParams(lp);

            messageHolder.messageContainer.removeAllViews();
            messageHolder.messageContainer.addView(typeWrapper);
            messageHolder.messageContainer.addView(messageHolder.iconWrapper);
            messageHolder.messageContainer.setGravity(Gravity.RIGHT);
        } else {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) typeWrapper.getLayoutParams();
            lp.setMargins((int) (5 * mContext.getResources().getDisplayMetrics().density + 0.5f), 0, 0, 0);
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

    static class MessageHolder {
        LinearLayout messageContainer;

        LinearLayout iconWrapper;
        ImageView avatar;

        LinearLayout messageWrapper;
        TextView messageContent;
        TextView messageDateText;

        LinearLayout selfieconWrapper;
        ImageView gifSelfiecon;
        TextView messageDateSelfiecon;

        LinearLayout mediaWrapper;
        ImageView mediaContent;
        TextView messageDateMedia;
    }
}
