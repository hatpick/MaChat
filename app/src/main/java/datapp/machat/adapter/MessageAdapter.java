package datapp.machat.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.kevinsawicki.timeago.TimeAgo;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.activity.ChatActivity;
import datapp.machat.custom.CircleTransform;
import datapp.machat.helper.EmojiExtractor;
import datapp.machat.helper.SizeHelper;
import datapp.machat.helper.TextIndex;


public class MessageAdapter extends ArrayAdapter<ParseObject> {
    private int mLastPosition;
    private LayoutInflater inflater;
    private final String TAG = "MessageAdapter";
    private CircleTransform transformation;
    private Context mContext;
    private MediaPlayer mediaPlayer;

    public MessageAdapter(Context context, ArrayList<ParseObject> messages) {
        super(context, 0, messages);
        mContext = context;
        inflater = ((Activity) context).getLayoutInflater();
        mLastPosition = -1;
        transformation = new CircleTransform(context);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ParseObject message = getItem(position);

        MessageHolder messageHolder = null;

        if (row == null) {
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
            messageHolder.openMapsBtn = (Button) row.findViewById(R.id.open_maps_btn);
            messageHolder.navigateBtn = (Button) row.findViewById(R.id.navigate_to_btn);
            messageHolder.messageDateText = (TextView) row.findViewById(R.id.message_date_text);
            messageHolder.dateWrapper = (LinearLayout) row.findViewById(R.id.date_wrapper);
            messageHolder.messageContent = (TextView) row.findViewById(R.id.message_content);
            messageHolder.messageContainer = (LinearLayout) row.findViewById(R.id.message_container);
            messageHolder.messageMainContainer = (LinearLayout) row.findViewById(R.id.message_main_container);
            messageHolder.iconWrapper = (LinearLayout) row.findViewById(R.id.icon_wrapper);
            messageHolder.messageWrapper = (LinearLayout) row.findViewById(R.id.message_wrapper);
            messageHolder.selfieconWrapper = (LinearLayout) row.findViewById(R.id.selficon_wrapper);
            messageHolder.ytWrapper = (LinearLayout) row.findViewById(R.id.yt_wrapper);
            messageHolder.mediaWrapper = (LinearLayout) row.findViewById(R.id.media_wrapper);
            messageHolder.giphyWrapper = (LinearLayout) row.findViewById(R.id.giphy_wrapper);
            messageHolder.buzzWrapper = (LinearLayout) row.findViewById(R.id.buzz_wrapper);
            messageHolder.mapWrapper = (LinearLayout) row.findViewById(R.id.map_wrapper);
            messageHolder.vineWrapper = (LinearLayout) row.findViewById(R.id.vine_wrapper);
            messageHolder.vineContent = (VideoView) row.findViewById(R.id.vine_content);
            messageHolder.videoPlay = (Button) row.findViewById(R.id.vine_play);
            messageHolder.recordingWrapper = (LinearLayout) row.findViewById(R.id.recording_wrapper);
            messageHolder.recordingContent = (LinearLayout) row.findViewById(R.id.recording_content);
            messageHolder.recordingPlay = (Button) row.findViewById(R.id.recording_content_play);

            messageHolder.statusDelivered = (ImageView) row.findViewById(R.id.msg_status_delivered);
            messageHolder.statusSeen = (ImageView) row.findViewById(R.id.msg_status_seen);
            messageHolder.statusWrapper = (FrameLayout) row.findViewById(R.id.status_wrapper);

            final MessageHolder helperHolder = messageHolder;

            messageHolder.videoPlay.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent motionEvent) {
                    Button playBtn = (Button) v;
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (helperHolder.vineContent.isPlaying()) {
                            helperHolder.vineContent.pause();
                            playBtn.setBackground(mContext.getResources().getDrawable(R.mipmap.video_play));
                            Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in_quick);
                            fadeIn.setFillAfter(true);
                            fadeIn.setFillEnabled(true);
                            playBtn.startAnimation(fadeIn);
                        } else {
                            helperHolder.vineContent.start();
                            playBtn.setBackground(mContext.getResources().getDrawable(R.mipmap.video_pause));
                            Animation fadeOut = AnimationUtils.loadAnimation(mContext, R.anim.fade_out_quick);
                            fadeOut.setFillAfter(true);
                            fadeOut.setFillEnabled(true);
                            playBtn.startAnimation(fadeOut);
                        }
                    }
                    return true;
                }
            });

            messageHolder.vineContent.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent motionEvent) {
                    VideoView videoView = (VideoView) v;
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (videoView.isPlaying()) {
                            videoView.pause();
                            helperHolder.videoPlay.setBackground(mContext.getResources().getDrawable(R.mipmap.video_play));
                            Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in_quick);
                            fadeIn.setFillAfter(true);
                            fadeIn.setFillEnabled(true);
                            helperHolder.videoPlay.startAnimation(fadeIn);
                        } else {
                            videoView.start();
                            helperHolder.videoPlay.setBackground(mContext.getResources().getDrawable(R.mipmap.video_pause));
                            Animation fadeOut = AnimationUtils.loadAnimation(mContext, R.anim.fade_out_quick);
                            fadeOut.setFillAfter(true);
                            fadeOut.setFillEnabled(true);
                            helperHolder.videoPlay.startAnimation(fadeOut);
                        }
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
        if (message.getCreatedAt() != null)
            messageHolder.messageDateText.setText(new TimeAgo().timeAgo(message.getCreatedAt()));
        else
            messageHolder.messageDateText.setText("Just now");

        if (messageType.equals("text")) {
            typeWrapper = messageHolder.messageWrapper;
            //TODO:Spannable
            String msgContent = message.getString("content");
            try {
                SpannableString spannableString = new SpannableString(message.getString("content"));
                ArrayList<TextIndex> extractedString = EmojiExtractor.extract(msgContent);
                for(TextIndex ti : extractedString) {
                    if(ti.isEmoji()) {
                        spannableString.setSpan(new AbsoluteSizeSpan(25, true), ti.getStart(), ti.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                messageHolder.messageContent.setText(spannableString);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (messageType.equals("selfiecon")) {
            typeWrapper = messageHolder.selfieconWrapper;


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
        } else if (messageType.equals("media")) {
            typeWrapper = messageHolder.mediaWrapper;


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
        } else if (messageType.equals("giphy")) {
            typeWrapper = messageHolder.giphyWrapper;


            Glide.with(mContext)
                    .load(message.getString("content"))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageHolder.giphyContent);

            messageHolder.giphyContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } else if (messageType.equals("map")) {
            typeWrapper = messageHolder.mapWrapper;


            final Double lat = new Double(message.getParseGeoPoint("location").getLatitude());
            final Double lng = new Double(message.getParseGeoPoint("location").getLongitude());

            String url = mContext.getResources().getString(R.string.maps_static)
                    .replace("{lat}", lat.toString()).replace("{lng}", lng.toString()).replace("{label}", message.getParseUser("from").getString("fName").toUpperCase().charAt(0) + "");

            Glide.with(mContext)
                    .load(url)
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(messageHolder.mapContent);

            messageHolder.openMapsBtn.setOnTouchListener(ChatActivity.TOUCH);
            messageHolder.navigateBtn.setOnTouchListener(ChatActivity.TOUCH);

            messageHolder.openMapsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMap(Uri.parse("geo:" + lat + "," + lng + "?z=15"));
                }
            });

            messageHolder.navigateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMap(Uri.parse("google.navigation:q=" + lat + "," + lng + "&mode=d"));
                }
            });
        } else if (messageType.equals("youtube")) {
            typeWrapper = messageHolder.ytWrapper;


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
        } else if (messageType.equals("vine")) {
            typeWrapper = messageHolder.vineWrapper;


            String vineUrl = message.getString("content");
            messageHolder.vineContent.setVideoURI(Uri.parse(vineUrl));
        } else if (messageType.equals("buzz")) {
            typeWrapper = messageHolder.buzzWrapper;
            if(!message.getParseUser("from").getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) && !message.getBoolean("buzzed")) {
                MediaPlayer mPlayer;
                mPlayer = MediaPlayer.create(mContext, R.raw.buzz);
                mPlayer.start();
                message.put("buzzed", true);
                message.saveInBackground();
            }

            Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
            shake.setFillAfter(true);
            shake.setFillEnabled(true);
            messageHolder.buzzWrapper.startAnimation(shake);

        } else if (messageType.equals("recording")) {
            typeWrapper = messageHolder.recordingWrapper;


            if (!getItem(position).getBoolean("isPlaying")) {
                messageHolder.recordingPlay.setBackgroundResource(android.R.drawable.ic_media_play);
            } else {
                messageHolder.recordingPlay.setBackgroundResource(android.R.drawable.ic_media_pause);
            }

            final String recordingUrl = message.getString("content");
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    updateRecordingUI();
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
            });

            messageHolder.recordingPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button b = (Button) view;
                    if (!getItem(position).getBoolean("isPlaying")) {
                        updateRecordingUI();
                        getItem(position).put("isPlaying", true);
                        b.setBackgroundResource(android.R.drawable.ic_media_pause);
                        try {
                            Runnable r = new Runnable() {
                                public void run() {
                                    try {
                                        setDataSource(recordingUrl);
                                        mediaPlayer.prepare();
                                        mediaPlayer.start();
                                    } catch (IOException e) {
                                        Log.e(TAG, e.getMessage(), e);
                                    }
                                }
                            };
                            new Thread(r).start();
                        } catch (Exception e) {
                            Log.e(TAG, "error: " + e.getMessage(), e);
                            if (mediaPlayer != null) {
                                mediaPlayer.stop();
                                mediaPlayer.reset();
                            }
                        }
                    } else {
                        getItem(position).put("isPlaying", false);
                        b.setBackgroundResource(android.R.drawable.ic_media_play);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                }
            });
        }


        typeWrapper.setVisibility(View.VISIBLE);

        if (message.getParseUser("from").getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            int margin = (int) SizeHelper.convertDpToPixel(5f, mContext);

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) messageHolder.messageContainer.getLayoutParams();
            lp.setMargins(0, 0, margin, 0);
            messageHolder.messageContainer.setLayoutParams(lp);

            messageHolder.messageContainer.removeAllViews();
            messageHolder.messageContainer.addView(typeWrapper);
            messageHolder.messageContainer.addView(messageHolder.dateWrapper);


            messageHolder.messageMainContainer.removeAllViews();
            messageHolder.messageMainContainer.addView(messageHolder.messageContainer);
            //messageHolder.messageMainContainer.addView(messageHolder.iconWrapper);

            messageHolder.messageContainer.setGravity(Gravity.RIGHT);
            messageHolder.dateWrapper.setGravity(Gravity.RIGHT);

            if(messageType.equals("text")) {
                messageHolder.messageContent.setBackgroundResource(R.drawable.message_bg_sender);
                messageHolder.messageContent.setMaxWidth((int) SizeHelper.convertDpToPixel(250f, mContext));
            }

            messageHolder.statusWrapper.setVisibility(View.VISIBLE);
            String msgStatus = message.getString("status");
            if (msgStatus != null) {
                if (msgStatus.equals("delivered")) {
                    messageHolder.statusDelivered.setVisibility(View.VISIBLE);
                    messageHolder.statusSeen.setVisibility(View.GONE);
                } else if (msgStatus.equals("seen")) {
                    messageHolder.statusDelivered.setVisibility(View.GONE);
                    messageHolder.statusSeen.setVisibility(View.VISIBLE);
                } else {
                    messageHolder.statusDelivered.setVisibility(View.GONE);
                    messageHolder.statusSeen.setVisibility(View.GONE);
                }
            }
        } else {
            int margin = (int) SizeHelper.convertDpToPixel(5f, mContext);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) messageHolder.messageContainer.getLayoutParams();
            lp.setMargins(margin, 0, 0, 0);
            messageHolder.messageContainer.setLayoutParams(lp);

            messageHolder.messageContainer.removeAllViews();
            messageHolder.messageContainer.addView(typeWrapper);
            messageHolder.messageContainer.addView(messageHolder.dateWrapper);

            messageHolder.messageMainContainer.removeAllViews();
            messageHolder.messageMainContainer.addView(messageHolder.iconWrapper);
            messageHolder.messageMainContainer.addView(messageHolder.messageContainer);

            messageHolder.messageContainer.setGravity(Gravity.LEFT);
            messageHolder.dateWrapper.setGravity(Gravity.LEFT);

            if(messageType.equals("text")) {
                messageHolder.messageContent.setBackgroundResource(R.drawable.message_bg);
                messageHolder.messageContent.setMaxWidth((int) SizeHelper.convertDpToPixel(200f, mContext));
            }

            messageHolder.statusWrapper.setVisibility(View.GONE);
        }

        if (position > mLastPosition) {
            Animation bounceInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in_quick);
            row.startAnimation(bounceInAnimation);
            mLastPosition = position;
        }

        return row;

    }

    private void updateRecordingUI() {
        int c = 0;
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).get("type").equals("recording") && getItem(i).getBoolean("isPlaying")) {
                getItem(i).put("isPlaying", false);
                c++;
            }
        }

        if (c > 0) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            notifyDataSetChanged();
        }
    }

    public void watchYoutubeVideo(String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            mContext.startActivity(intent);
        }
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW, geoLocation);
        intent.setPackage("com.google.android.apps.maps");
        mContext.startActivity(intent);
    }

    public void showImage(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "image/*");
        mContext.startActivity(intent);
    }

    private void setDataSource(String path) throws IOException {
        if (!URLUtil.isNetworkUrl(path)) {
            mediaPlayer.setDataSource(path);
        } else {
            URL url = new URL(path);
            URLConnection cn = url.openConnection();
            cn.connect();
            InputStream stream = cn.getInputStream();
            if (stream == null)
                throw new RuntimeException("stream is null");
            File temp = File.createTempFile("mediaplayertmp", ".mp4");
            String tempPath = temp.getAbsolutePath();
            FileOutputStream out = new FileOutputStream(temp);
            byte buf[] = new byte[128];
            do {
                int numread = stream.read(buf);
                if (numread <= 0)
                    break;
                out.write(buf, 0, numread);
            } while (true);
            mediaPlayer.setDataSource(tempPath);
            try {
                stream.close();
            } catch (IOException ex) {
                Log.e(TAG, "error: " + ex.getMessage(), ex);
            }
        }
    }

    static class MessageHolder {
        LinearLayout messageMainContainer;
        LinearLayout messageContainer;

        LinearLayout iconWrapper;
        ImageView avatar;

        LinearLayout messageWrapper;
        TextView messageContent;
        TextView messageDateText;

        LinearLayout selfieconWrapper;
        ImageView thumbnailSelfiecon;
        ImageView gifSelfiecon;

        LinearLayout mediaWrapper;
        ImageView mediaContent;

        LinearLayout giphyWrapper;
        ImageView giphyContent;

        LinearLayout vineWrapper;
        VideoView vineContent;
        Button videoPlay;

        LinearLayout recordingWrapper;
        LinearLayout recordingContent;
        Button recordingPlay;

        LinearLayout ytWrapper;
        ImageView ytContent;
        ImageView ytPlay;

        LinearLayout buzzWrapper;

        ImageView statusDelivered;
        ImageView statusSeen;
        FrameLayout statusWrapper;

        LinearLayout dateWrapper;

        LinearLayout mapWrapper;
        Button openMapsBtn;
        Button navigateBtn;
        ImageView mapContent;
    }
}
