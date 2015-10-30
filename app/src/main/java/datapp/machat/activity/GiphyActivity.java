package datapp.machat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.faradaj.blurbehind.BlurBehind;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import datapp.machat.R;
import datapp.machat.adapter.GIFAdapter;
import datapp.machat.application.MaChatApplication;
import datapp.machat.custom.CustomActivity;
import datapp.machat.dao.GiphyGIF;
import datapp.machat.helper.ConnectionCheck;
import datapp.machat.helper.RecyclerItemClickListener;
import datapp.machat.helper.SizeHelper;

public class GiphyActivity extends CustomActivity {
    private ArrayList<GiphyGIF> gifsList;
    private GIFAdapter gifAdapter;
    private RecyclerView gifsView;
    private EditText searchTerm;
    private Button searchBtn;
    private ProgressBar progressBar;
    private RecyclerView.LayoutManager mLayoutManager;
    private static final String TAG = "GiphyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giphy);

        if (savedInstanceState != null) {
            byte[] bitmapData = savedInstanceState.getByteArray("bg-giphy");
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

        _setupActionBar();

        gifsView = (RecyclerView)findViewById(R.id.gifs_view);
        progressBar = (ProgressBar) findViewById(R.id.gif_loading);

        gifsView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        gifsView.setItemAnimator(new DefaultItemAnimator());
        gifsView.setLayoutManager(mLayoutManager);

        searchBtn = (Button)findViewById(R.id.search_gif);
        searchTerm = (EditText)findViewById(R.id.gif_search_term);
        gifsList = new ArrayList<>();
        gifAdapter = new GIFAdapter(this, gifsList);
        gifsView.setAdapter(gifAdapter);

        gifsView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final GiphyGIF giphyGIF = gifsList.get(position);
                Intent intent = new Intent();
                if(giphyGIF != null){
                    intent.putExtra("newgiphyGIF", giphyGIF);
                    setResult(Activity.RESULT_OK, intent);
                } else {
                    setResult(Activity.RESULT_CANCELED, intent);
                }
                finish();
            }
        }));

        setTouchNClick(R.id.search_gif);

        searchTerm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if(charSequence.length() > 0)
                    searchBtn.setEnabled(true);
                else
                    searchBtn.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchTerm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH){
                    String keyword = searchTerm.getText().toString();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchTerm.getWindowToken(), 0);
                    _searchGiphy(keyword);
                    return true;
                }
                return false;
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
        outState.putByteArray("bg-giphy", bitmapdata);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.search_gif){
            String tempKeyword = searchTerm.getText().toString();
            String keyword = tempKeyword;
            if(tempKeyword.indexOf(' ') > 0){
                keyword = TextUtils.join("+", tempKeyword.split("\\s+"));
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchTerm.getWindowToken(), 0);
            _searchGiphy(keyword);
        }
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
        activity_title.setText("Search Giphy for GIFs!");
        activity_title.setPadding(20, 0, 0, 0);
        activity_title.setTextSize(16);
        ActionBar.LayoutParams txtLayoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT , Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        txtLayoutParams.leftMargin = 20;
        activity_title.setLayoutParams(txtLayoutParams);

        textLinearLayout.addView(activity_title);

        ImageView giphy_icon = new ImageView(actionBar.getThemedContext());
        giphy_icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_giphy));

        ActionBar.LayoutParams imgLayoutParams = new ActionBar.LayoutParams((int) SizeHelper.convertDpToPixel(40, this), (int)SizeHelper.convertDpToPixel(40, this) , Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        giphy_icon.setLayoutParams(imgLayoutParams);

        linearLayout.addView(giphy_icon);
        linearLayout.addView(textLinearLayout);


        actionBar.setCustomView(linearLayout);
    }

    private void _searchGiphy(String keyword) {
        String url = getString(R.string.giphy_base_url) + getString(R.string.giphy_search_endpoint).replace("{query}", keyword);
        progressBar.setVisibility(View.VISIBLE);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray fetchedGifs = response.getJSONArray("data");
                    if(fetchedGifs.length() > 0){
                        gifsList.clear();
                        JSONObject jsonObject;
                        GiphyGIF giphyGIF;
                        for (int i = 0; i < fetchedGifs.length(); i++) {
                            jsonObject = fetchedGifs.getJSONObject(i);
                            JSONObject gifImage = jsonObject.getJSONObject("images").getJSONObject("fixed_width_downsampled");
                            giphyGIF = new GiphyGIF(jsonObject.getString("id"), gifImage.getInt("width"), gifImage.getInt("height"), jsonObject.getString("rating"), gifImage.getString("url"));
                            gifsList.add(giphyGIF);
                        }
                        gifAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error != null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(GiphyActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        if(ConnectionCheck.checkConnectivity(this))
            MaChatApplication.getInstance().getRequestQueue().add(req);
        else {
            Toast.makeText(this, getString(R.string.no_network_error), Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        }

    }
}
