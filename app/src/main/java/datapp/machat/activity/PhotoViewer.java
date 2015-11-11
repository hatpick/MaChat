package datapp.machat.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import datapp.machat.R;
import datapp.machat.application.MaChatApplication;
import datapp.machat.dao.MaChatTheme;
import datapp.machat.helper.BlurBehind.BlurBehind;

public class PhotoViewer extends AppCompatActivity implements View.OnClickListener{
    private ImageView photoViewerImage;
    private Button photoViwerCloseBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        String themeName = getSharedPreferences("Theme", Context.MODE_PRIVATE).getString("Theme", "Default");
        MaChatTheme theme = MaChatApplication.getInstance().getThemeByName(themeName);
        int overlayColor = getResources().getColor(theme.getColor());

        BlurBehind.getInstance()
                .withAlpha(75)
                .withFilterColor(overlayColor)
                .setBackground(this);

        String url = getIntent().getStringExtra("imageUrl");

        photoViwerCloseBtn = (Button) findViewById(R.id.photo_viewer_close);
        photoViewerImage = (ImageView) findViewById(R.id.photo_viewer_image);

        photoViwerCloseBtn.setOnClickListener(this);
        Glide.with(this)
                .load(url)
                .into(photoViewerImage);
    }

    @Override
    public void onClick(View v) {
        supportFinishAfterTransition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
