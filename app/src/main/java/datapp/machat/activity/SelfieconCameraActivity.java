package datapp.machat.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.faradaj.blurbehind.BlurBehind;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import datapp.machat.R;
import datapp.machat.custom.CustomActivity;
import datapp.machat.custom.UserStatus;
import datapp.machat.selfiecon.SelfieconCameraPreview;

/**
 * Created by nfarahma on 7/4/2015.
 */
public class SelfieconCameraActivity extends CustomActivity  {
    private static final String TAG = "SelfieconCameraActivity";
    private static final int CAMERA_REQUEST = 1888;
    private ImageView selfie1, selfie2, selfie3;

    private List<Bitmap> mImages = new ArrayList<Bitmap>();

    private SelfieconCameraPreview preview;
    private Camera mCamera;
    private String receiverFbId;
    private String senderFbId;
    private FrameLayout layout;

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = openFrontFacingCameraGingerbread();
        } catch (Exception e) {
            Log.d(TAG, "Could not open camera");
        }
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfiecon_camera);

        receiverFbId = getIntent().getStringExtra("receiverFbId");
        senderFbId = getIntent().getStringExtra("senderFbId");

        BlurBehind.getInstance()
                .withAlpha(80)
                .withFilterColor(Color.parseColor("#B5008795"))
                .setBackground(this);

        layout = (FrameLayout)findViewById(R.id.middleSurface);

        selfie1 = (ImageView) findViewById(R.id.selfie1);
        selfie2 = (ImageView) findViewById(R.id.selfie2);
        selfie3 = (ImageView) findViewById(R.id.selfie3);

        setTouchNClick(R.id.restart_selfiecon_btn);
        setTouchNClick(R.id.use_selfiecon_btn);

        _resizeSelfiePreviews();
        //_setupPreview();
    }

    private void _setupPreview() {
        if(preview != null) {
            layout.removeView(preview);
        }
        preview = new SelfieconCameraPreview(this);
        layout.addView(preview);
        ArrayList<ImageView> selfiePreviews = new ArrayList<>(Arrays.asList(selfie1, selfie2, selfie3));
        preview.setSelfiePreviews(selfiePreviews);
    }

    private void _resizeSelfiePreviews() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        int totalMargin = 0; //(int) SizeHelper.convertDpToPixel(10.0f, this);
        int selfieWidth = (width - totalMargin)/3;

        LinearLayout.LayoutParams params;

        params = (LinearLayout.LayoutParams) selfie1.getLayoutParams();
        params.width = selfieWidth;
        params.height = selfieWidth;
        selfie1.setLayoutParams(params);
        params = (LinearLayout.LayoutParams) selfie2.getLayoutParams();
        params.width = selfieWidth;
        params.height = selfieWidth;
        selfie2.setLayoutParams(params);
        params = (LinearLayout.LayoutParams) selfie3.getLayoutParams();
        params.width = selfieWidth;
        params.height = selfieWidth;
        selfie3.setLayoutParams(params);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCamera = getCameraInstance();
        _setupPreview();
        preview.setCamera(mCamera);
        //UserStatus.setUserOnline();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            preview.setCamera(null);
            mCamera.release();
            mCamera = null;
            layout.removeView(preview);
            preview = null;
        }
        finish();
        //UserStatus.setUserOffline();
    }

    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }
}