package datapp.machat.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.faradaj.blurbehind.BlurBehind;
import com.faradaj.blurbehind.OnBlurCompleteListener;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import datapp.machat.R;
import datapp.machat.adapter.SelfieconAdapter;
import datapp.machat.custom.CustomActivity;
import datapp.machat.dao.Selfiecon;
import datapp.machat.helper.SizeHelper;

public class SelficonActivity extends CustomActivity {
    private GridView selficonGridView;
    private ArrayList<Selfiecon> selficonList;
    private SelfieconAdapter selfieconAdapter;
    private FrameLayout loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selficon);

        loading = (FrameLayout) findViewById(R.id.loading_selfiecons);

        if (savedInstanceState != null) {
            byte[] bitmapData = savedInstanceState.getByteArray("bg-selfiecon");
            if (bitmapData != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                BitmapDrawable bd = new BitmapDrawable(bitmap);
                bd.setAlpha(75);
                bd.setColorFilter(Color.parseColor("#B5e2466d"), PorterDuff.Mode.DST_ATOP);
                getWindow().getDecorView().setBackground(bd);
            }
        } else {
            BlurBehind.getInstance()
                    .withAlpha(75)
                    .withFilterColor(Color.parseColor("#B5e2466d"))
                    .setBackground(this);
        }

        selficonGridView = (GridView) findViewById(R.id.selfiecon_gridview);

        _setupActionBar();

        selficonGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });

        selficonList = new ArrayList<>();
        selfieconAdapter = new SelfieconAdapter(this, selficonList);
        selficonGridView.setAdapter(selfieconAdapter);

        _fetchSelficons();

        selficonGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Selfiecon selfie = selfieconAdapter.getItem(i);
                Intent intent = new Intent();
                if (selfie != null) {
                    intent.putExtra("newSelfiecon", selfie);
                    setResult(Activity.RESULT_OK, intent);
                } else {
                    setResult(Activity.RESULT_CANCELED, intent);
                }
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }

    private void _fetchSelficons() {
        loading.setVisibility(View.VISIBLE);
        selficonList.clear();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("GIF");
        query.whereEqualTo("creator", ParseUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.setSkip(selficonList.size());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> selficonObjs, ParseException e) {
                if (e == null) {
                    if (selficonObjs.size() > 0) {
                        Selfiecon selfiecon;
                        for (int i = 0; i < selficonObjs.size(); i++) {
                            ParseObject po = selficonObjs.get(i);
                            selfiecon = new Selfiecon(po.getObjectId(), po.getParseFile("gifFile").getUrl(), po.getParseFile("thumbnail").getUrl());
                            selficonList.add(selfiecon);
                        }
                        selfieconAdapter.notifyDataSetChanged();
                        loading.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(SelficonActivity.this, "Error loading selfiecons!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Drawable d = getWindow().getDecorView().getBackground();
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        outState.putByteArray("bg-slfiecon", bitmapdata);

        super.onSaveInstanceState(outState);
    }

    private void _setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        LinearLayout linearLayout = new LinearLayout(actionBar.getThemedContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        //App name textview
        LinearLayout textLinearLayout = new LinearLayout(actionBar.getThemedContext());
        textLinearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        TextView activity_title = new TextView(actionBar.getThemedContext());
        activity_title.setText("Select a Selfiecon");
        activity_title.setPadding(20, 0, 0, 0);
        activity_title.setTextSize(16);
        ActionBar.LayoutParams txtLayoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT , Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        txtLayoutParams.leftMargin = 20;
        activity_title.setLayoutParams(txtLayoutParams);

        textLinearLayout.addView(activity_title);

        //TODO: change icon
        ImageView selficon_icon = new ImageView(actionBar.getThemedContext());
        selficon_icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_selfiecon));

        ActionBar.LayoutParams imgLayoutParams = new ActionBar.LayoutParams((int) SizeHelper.convertDpToPixel(40, this), (int)SizeHelper.convertDpToPixel(40, this) , Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        selficon_icon.setLayoutParams(imgLayoutParams);

        linearLayout.addView(selficon_icon);
        linearLayout.addView(textLinearLayout);


        actionBar.setCustomView(linearLayout);
    }
}
