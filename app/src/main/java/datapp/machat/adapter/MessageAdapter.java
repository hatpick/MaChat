package datapp.machat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.kevinsawicki.timeago.TimeAgo;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import datapp.machat.R;

/**
 * Created by hat on 7/7/15.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {
    private int mLastPosition;
    private LayoutInflater inflater;
    private final String TAG = "MessageAdapter";
    private Transformation transformation;
    private Context mContext;
    private ArrayList<ParseObject> chatMessages;

    public MessageAdapter(Context context, ArrayList<ParseObject> messages) {
        super(context, 0, messages);
        mContext = context;
        chatMessages = messages;
        inflater = ((Activity) context).getLayoutInflater();
        mLastPosition = -1;
        transformation = new RoundedTransformationBuilder()
                .borderWidthDp(0)
                .cornerRadiusDp(30)
                .oval(false)
                .build();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ParseObject message = getItem(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm a EEE, d MMM yyyy");
        dateFormat.setTimeZone(TimeZone.getDefault());

        MessageHolder messageHolder = null;

        if(row == null) {
            row = inflater.inflate(R.layout.message, parent, false);
            messageHolder = new MessageHolder();
            messageHolder.avatar = (ImageView) row.findViewById(R.id.avatar);
            messageHolder.messageDate = (TextView) row.findViewById(R.id.message_date);
            messageHolder.messageContent = (TextView) row.findViewById(R.id.message_content);
            messageHolder.messageContainer = (LinearLayout) row.findViewById(R.id.message_container);
            messageHolder.iconWrapper = (LinearLayout) row.findViewById(R.id.icon_wrapper);
            messageHolder.messageWrapper = (LinearLayout) row.findViewById(R.id.message_wrapper);

            row.setTag(messageHolder);
        } else {
            messageHolder = (MessageHolder) row.getTag();
        }

        Picasso.with(getContext())
                .load(message.getParseUser("from").getParseFile("profilePicture").getUrl())
                .fit()
                .transform(transformation)
                .into(messageHolder.avatar);

        if(message.getCreatedAt() != null)
            messageHolder.messageDate.setText(new TimeAgo().timeAgo(message.getCreatedAt()));
        else
            messageHolder.messageDate.setText("Just now");

        if(message.getString("type").equals("text")) {
            messageHolder.messageContent.setText(message.getString("content"));
        } else {
            //set selfiecon
        }

        if(message.getParseUser("from").getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) messageHolder.messageWrapper.getLayoutParams();
            lp.setMargins(0, 0, (int) (5 * mContext.getResources().getDisplayMetrics().density + 0.5f), 0);
            messageHolder.messageWrapper.setLayoutParams(lp);

            messageHolder.messageContainer.removeAllViews();
            messageHolder.messageContainer.addView(messageHolder.messageWrapper);
            messageHolder.messageContainer.addView(messageHolder.iconWrapper);
            messageHolder.messageContainer.setGravity(Gravity.RIGHT);
        } else {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) messageHolder.messageWrapper.getLayoutParams();
            lp.setMargins((int) (5 * mContext.getResources().getDisplayMetrics().density + 0.5f), 0, 0, 0);
            messageHolder.messageWrapper.setLayoutParams(lp);

            messageHolder.messageContainer.removeAllViews();
            messageHolder.messageContainer.addView(messageHolder.iconWrapper);
            messageHolder.messageContainer.addView(messageHolder.messageWrapper);
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
        TextView messageContent;
        TextView messageDate;
        LinearLayout messageContainer;
        LinearLayout messageWrapper;
        LinearLayout iconWrapper;
        ImageView avatar;
        //TODO: add Selficon player
    }
}
