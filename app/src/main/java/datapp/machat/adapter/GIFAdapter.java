package datapp.machat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.custom.CircleTransform;
import datapp.machat.dao.GiphyGIF;
import datapp.machat.dao.Selfiecon;
import datapp.machat.helper.SizeHelper;

/**
 * Created by hat on 7/15/15.
 */
public class GIFAdapter extends RecyclerView.Adapter<GIFAdapter.GiphyGIFHolder> {
    private int imageWidth;
    private Context mContext;
    private ArrayList<GiphyGIF> dataList;
    private final String TAG = "GiphyGIFAdapter";

    public GIFAdapter(Context mContext, ArrayList<GiphyGIF> gifs) {
        dataList = gifs;
        this.mContext = mContext;

        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        int totalMargin = 0;// (int) SizeHelper.convertDpToPixel(32.0f, mContext);
        imageWidth = (width - totalMargin)/2;
    }

    @Override
    public GiphyGIFHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.giphy_gif, viewGroup, false);
        GiphyGIFHolder vh = new GiphyGIFHolder(v, imageWidth);

        return vh;

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onBindViewHolder(GiphyGIFHolder holder, int position) {
        GiphyGIF giphyGIF = dataList.get(position);

        Glide.with(mContext)
            .load(giphyGIF.getSmallSizedUrl())
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(holder.gifViewer);
    }

    static class GiphyGIFHolder extends RecyclerView.ViewHolder  {
        public ImageView gifViewer;

        GiphyGIFHolder(View view, int width) {
            super(view);
            this.gifViewer = (ImageView)view.findViewById(R.id.giphy_gif_holder);
            this.gifViewer.getLayoutParams().width = width;
            this.gifViewer.getLayoutParams().height = width;

        }
    }
}
