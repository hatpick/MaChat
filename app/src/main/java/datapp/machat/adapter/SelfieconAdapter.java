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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseObject;

import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.custom.CircleTransform;
import datapp.machat.dao.Selfiecon;

/**
 * Created by hat on 7/9/15.
 */
public class SelfieconAdapter extends ArrayAdapter<Selfiecon> {
    private int mLastPosition;
    private LayoutInflater inflater;
    private CircleTransform transformation;
    private final String TAG = "SelfieconAdapter";
    private Context mContext;

    public SelfieconAdapter(Context context, ArrayList<Selfiecon> selfiecons) {
        super(context, 0, selfiecons);
        mContext = context;
        inflater = ((Activity) context).getLayoutInflater();
        mLastPosition = -1;
        transformation = new CircleTransform(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final Selfiecon gifObj = getItem(position);
        SelfieconHolder selfieconHolder;
        if(row == null) {
            row = inflater.inflate(R.layout.selfiecon, parent, false);
            selfieconHolder = new SelfieconHolder();
            selfieconHolder.gifViewer = (ImageView) row.findViewById(R.id.selfiecon_gif_viewer);

            row.setTag(selfieconHolder);
        } else {
            selfieconHolder = (SelfieconHolder) row.getTag();
        }

        Glide.with(mContext)
                .load(gifObj.getGifUrl())
                .transform(transformation)
                .placeholder(R.drawable.circle_bg)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(selfieconHolder.gifViewer);

        if (position > mLastPosition) {
            Animation bounceInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.bounce_in);
            row.startAnimation(bounceInAnimation);
            mLastPosition = position;
        }

        return row;
    }

    static class SelfieconHolder {
        ImageView gifViewer;
    }
}
