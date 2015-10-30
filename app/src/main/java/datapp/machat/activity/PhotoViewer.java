package datapp.machat.activity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import datapp.machat.R;
import datapp.machat.helper.BlurBehind.BlurBehind;

public class PhotoViewer extends AppCompatActivity implements View.OnClickListener{
    private ImageView photoViewerImage;
    private Button photoViwerCloseBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        BlurBehind.getInstance()
                .withAlpha(75)
                .withFilterColor(Color.parseColor("#B5e2466d"))
                .setBackground(this);

        String url = getIntent().getStringExtra("imageUrl");

        photoViwerCloseBtn = (Button) findViewById(R.id.photo_viewer_close);
        photoViewerImage = (ImageView) findViewById(R.id.photo_viewer_image);

        photoViwerCloseBtn.setOnClickListener(this);
        Glide.with(this)
                .load(url)
                .centerCrop()
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
