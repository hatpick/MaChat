package datapp.machat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.transcode.BitmapToGlideDrawableTranscoder;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.activity.ChatActivity;

/**
 * Created by hat on 11/23/15.
 */
public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiHolder> {
    private Context mContext;
    private ChatActivity activity;
    private ArrayList<ParseObject> dataList;
    private final String TAG = "EmojiAdapter";

    public void remove(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    public void add(ParseObject em, int position) {
        dataList.add(position, em);
        notifyItemInserted(position);
    }

    public EmojiAdapter(Context mContext, ArrayList<ParseObject> emojies) {
        this.dataList = emojies;
        this.activity = (ChatActivity) mContext;
        this.mContext = mContext;
    }

    @Override
    public EmojiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_emoji, parent, false);
        EmojiHolder emojiHolder = new EmojiHolder(v, new EmojiHolder.IMyViewHolderClicks() {
            @Override
            public void onClick(View caller, int position) {
                final ParseObject selectedEmoji = dataList.get(position);
                //Do shit with it
                activity.sendMessage(activity.getReceiver(), "emoji", selectedEmoji.getParseFile("emoji").getUrl(), null, null);
            }
        });

        return emojiHolder;
    }

    @Override
    public void onBindViewHolder(EmojiHolder holder, int position) {
        ParseObject emoji = dataList.get(position);
        String gifUrl = emoji.getParseFile("emoji").getUrl();
        Glide
                .with(mContext)
                .load(gifUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .thumbnail(Glide
                                .with(mContext)
                                .load(gifUrl)
                                .asBitmap()
                                .transcode(new BitmapToGlideDrawableTranscoder(mContext), GlideDrawable.class)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(holder.emojiView);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class EmojiHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        public ImageView emojiView;
        public IMyViewHolderClicks mListener;

        EmojiHolder(View view, IMyViewHolderClicks listener) {
            super(view);
            this.mListener = listener;
            this.emojiView = (ImageView) view.findViewById(R.id.emoji_holder);
            this.emojiView.setOnClickListener(this);
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
