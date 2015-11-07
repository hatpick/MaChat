package datapp.machat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.activity.MainActivity;
import datapp.machat.application.MaChatApplication;
import datapp.machat.dao.MaChatTheme;
import datapp.machat.helper.SizeHelper;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeItemHolder> {
    private final int imageWidth;
    private Context mContext;
    private ArrayList<MaChatTheme> dataList;
    private final String TAG = "ThemeAdapter";
    private MaChatTheme theme;

    public ThemeAdapter(Context mContext, ArrayList<MaChatTheme> themes) {
        dataList = themes;
        this.mContext = mContext;
        this.theme = ((MainActivity)mContext).getMaChatTheme();

        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        int totalMargin = (int) SizeHelper.convertDpToPixel(32f + 80f, mContext);
        imageWidth = (width - totalMargin)/4;
    }

    @Override
    public ThemeItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.theme_item, viewGroup, false);
        ThemeItemHolder vh = new ThemeItemHolder(v, imageWidth, new ThemeItemHolder.IMyViewHolderClicks() {
            @Override
            public void OnClick(View caller, int position) {
                MaChatTheme theme = dataList.get(position);
                ((MainActivity)mContext).setMaChatTheme(theme);
                LinearLayout layout = (LinearLayout) ((Activity)mContext).findViewById(R.id.main_container);
                layout.setBackgroundResource(theme.getId());
                String currentTheme = mContext.getSharedPreferences("Theme", Context.MODE_PRIVATE).getString("Theme", null);
                mContext.getSharedPreferences("Theme", Context.MODE_PRIVATE).edit().putString("Theme", theme.getName()).commit();
                mContext.getSharedPreferences("Theme", Context.MODE_PRIVATE).edit().apply();
                ((MainActivity)mContext).dismissThemeDialog();

                Drawable[] drawables = new Drawable[2];
                drawables[0] = mContext.getResources().getDrawable(MaChatApplication.getInstance().getThemeByName(currentTheme).getId());
                drawables[1] = mContext.getResources().getDrawable(MaChatApplication.getInstance().getThemeByName(theme.getName()).getId());
                final TransitionDrawable transitionDrawable = new TransitionDrawable(drawables);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        transitionDrawable.startTransition(1500);
                    }
                }, 1500);
            }
        });

        return vh;

    }

    public void remove(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    public void add(MaChatTheme t, int position) {
        dataList.add(position, t);
        notifyItemInserted(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onBindViewHolder(ThemeItemHolder holder, int position) {
        MaChatTheme themeItem = dataList.get(position);
        holder.imageView.setBackgroundResource(themeItem.getId());
        if(theme.getName().equals(themeItem.getName()))
            holder.selected.setVisibility(View.VISIBLE);
        else
            holder.selected.setVisibility(View.INVISIBLE);
    }

    static class ThemeItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageView;
        public ImageView selected;
        public FrameLayout frame;
        public IMyViewHolderClicks mListener;

        ThemeItemHolder(View view,int imageWidth, IMyViewHolderClicks listener) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.theme_item_bg);
            this.selected = (ImageView) view.findViewById(R.id.theme_selected);
            this.frame = (FrameLayout) view.findViewById(R.id.theme_frame);
            this.frame.getLayoutParams().height = imageWidth;
            this.mListener = listener;
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            mListener.OnClick(v, this.getAdapterPosition());
        }

        public interface IMyViewHolderClicks {
            void OnClick(View caller, int position);
        }
    }
}