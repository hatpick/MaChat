package datapp.machat.selfiecon;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import datapp.machat.R;
import datapp.machat.application.MaChatApplication;
import datapp.machat.custom.CircleTransform;
import datapp.machat.dao.Selfiecon;
import datapp.machat.helper.SizeHelper;

/**
 * Created by nfarahma on 7/3/2015.
 */
public class SelfieconCameraPreview extends SurfaceView implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback {
    private static final String TAG = "SelfieconCameraPreview";
    private SurfaceHolder holder;
    private int mIndex = 0;
    protected final Paint paint = new Paint();
    protected final Path path = new Path();
    private Camera.Size mPreviewSize;
    private float ratio;
    final private Activity activity = ((Activity) getContext());
    private Camera mCamera;
    private List<Camera.Size> mSupportedPreviewSizes;
    private List<Bitmap> mImages = new ArrayList<Bitmap>();
    private int mNumberOfImages = 3;
    private int radius;
    private ArrayList<ImageView> selfiePreviews;

    public SelfieconCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void IncrementIndex() {
        mIndex++;
    }

    public void ResetIndex() {
        mIndex = 0;
    }

    public void setSelfiePreviews(ArrayList<ImageView> selfiePreviews) {
        this.selfiePreviews = selfiePreviews;
    }

    public void setCamera(Camera c) {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        mCamera = c;
        try {
            if (mCamera != null) {
                mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SelfieconCameraPreview(Context context) {
        super(context);

        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        if (mImages.size() == mNumberOfImages) {
            setVisibility(INVISIBLE);
        } else {
            if (mImages.size() < mNumberOfImages) {
                path.addCircle(width / 2, height / 2, radius, Path.Direction.CW);
                canvas.drawPath(path, paint);
                setZOrderOnTop(true);
            }
        }
    }

    public Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap flip(Bitmap d) {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap src = d;
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);

            if(mPreviewSize.height >= mPreviewSize.width)
                ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
            else
                ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

            setMeasuredDimension(width, (int) (width * ratio));
            radius = (int) SizeHelper.convertDpToPixel(150f, getContext());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated");
        startPreview(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        stopPreview();
        configureCamera();
        startPreview(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed");
        stopPreview();
    }

    private boolean takingPicture = false;

    @Override
    public void onClick(View arg0) {
        if(!takingPicture) {
            takingPicture = true;
            mCamera.takePicture(null, null, null, this);
        }

    }

    public Bitmap centerCrop(Bitmap srcBmp) {
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {

        Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
        mImages.add(picture);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RotateBitmap(flip(centerCrop(picture)), 90).compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        if(mIndex == 0) {
            ImageView selfie1 = (ImageView) activity.findViewById(R.id.selfie1);
            Glide.with(getContext())
                    .load(byteArray)
                    .centerCrop()
                    .crossFade()
                    .transform(new CircleTransform(getContext()))
                    .into(selfie1);
        } else if(mIndex == 1){
            ImageView selfie2 = (ImageView) activity.findViewById(R.id.selfie2);
            Glide.with(getContext())
                    .load(byteArray)
                    .centerCrop()
                    .crossFade()
                    .transform(new CircleTransform(getContext()))
                    .into(selfie2);
        } else if(mIndex == 2) {
            ImageView selfie3 = (ImageView) activity.findViewById(R.id.selfie3);
            Glide.with(getContext())
                    .load(byteArray)
                    .centerCrop()
                    .crossFade()
                    .transform(new CircleTransform(getContext()))
                    .into(selfie3);
        }

        IncrementIndex();
        stopPreview();

        if (mImages.size() == mNumberOfImages) {
            setVisibility(GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final ImageView imageView = (ImageView) activity.findViewById(R.id.gif);
                    final Button useSelfie = (Button) activity.findViewById(R.id.use_selfiecon_btn);
                    final Button startOverSelfie = (Button) activity.findViewById(R.id.restart_selfiecon_btn);

                    imageView.setVisibility(VISIBLE);
                    for (int i = 0; i < selfiePreviews.size(); i++) {
                        moveViewToScreenCenter(selfiePreviews.get(i));
                    }


                    Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout selfieHolder= (LinearLayout) activity.findViewById(R.id.selfieHolder);
                            selfieHolder.setVisibility(GONE);
                            _generateGif(imageView, useSelfie, startOverSelfie);
                            takingPicture = false;
                        }
                    }, 1200);
                }
            }, 500);
        } else {
            invalidate();
            startPreview(holder);
            takingPicture = false;
        }
    }

    private void moveViewToScreenCenter(final View view )
    {
        FrameLayout root = (FrameLayout) activity.findViewById(R.id.middleSurface);
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics( dm );
        int statusBarOffset = dm.heightPixels - root.getMeasuredHeight();

        int originalPos[] = new int[2];
        view.getLocationOnScreen( originalPos );

        float xDest = dm.widthPixels/2;
        xDest -= (view.getMeasuredWidth()/2);
        float yDest = dm.heightPixels/2 - (view.getMeasuredHeight()/2) - statusBarOffset;

        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new LinearInterpolator());
        animSet.setFillEnabled(true);
        animSet.setFillAfter(true);
        animSet.setDuration(1150);

        TranslateAnimation animMove = new TranslateAnimation( 0, xDest - originalPos[0] , 0, yDest - originalPos[1] );
        Log.v(TAG, new Float(xDest - originalPos[0]).toString() + ", " + new Float(yDest - originalPos[1]).toString());
        animSet.addAnimation(animMove);

        AlphaAnimation animAlpha = new AlphaAnimation(1.0f, 0);
        animSet.addAnimation(animAlpha);

        view.startAnimation(animSet);
    }

    private void _generateGif(ImageView imageView, Button useSelfie, Button startOverSelfie) {
        String root = Environment.getExternalStorageDirectory().toString();
        File gifDir = new File(root + MaChatApplication.getPath() + "/saved_gifs");
        gifDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        final String fname = "selfiecon-" + n + ".gif";
        final File file = new File(gifDir, fname);
        if (file.exists()) file.delete();
        try {

            final OutputStream outputStream = new FileOutputStream(file);
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.start(outputStream);
            encoder.setRepeat(100);
            encoder.setDelay(150);
            for (int i = 0; i < mImages.size(); i++) {
                encoder.addFrame(RotateBitmap(flip(centerCrop(mImages.get(i))), 90));

            }
            encoder.finish();

            Glide.with(getContext())
                    .load(gifDir + "/" + file.getName())
                    .centerCrop().crossFade()
                    .transform(new CircleTransform(getContext()))
                    .placeholder(R.drawable.circle_bg)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);

            useSelfie.setVisibility(VISIBLE);
            startOverSelfie.setVisibility(VISIBLE);

            startOverSelfie.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.finish();
                    activity.startActivity(activity.getIntent());

                    //TODO: not correct
                }
            });

            useSelfie.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    int size = (int) file.length();
                    byte[] bytes = new byte[size];
                    try {
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                        buf.read(bytes, 0, bytes.length);
                        buf.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final ProgressDialog dia = new ProgressDialog(getContext());
                    dia.show();
                    dia.setContentView(R.layout.progress_dialog);
                    TextView diaTitle = (TextView) dia.findViewById(R.id.pd_title);
                    diaTitle.setText("Saving your Selficon...");

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    RotateBitmap(flip(centerCrop(mImages.get(1))), 90).compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    final ParseFile _gifFile = new ParseFile(fname, bytes);
                    final ParseFile _thumbnail = new ParseFile(fname.replace(".gif", "") + "_thumbnail.jpg", byteArray);
                    final Intent intent = new Intent();

                    _thumbnail.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null) {
                                _gifFile.saveInBackground(new SaveCallback() {
                                    public void done(ParseException e) {
                                        if(e == null) {
                                            final ParseObject gifFile = ParseObject.create("GIF");
                                            gifFile.setACL(new ParseACL(ParseUser.getCurrentUser()));
                                            gifFile.put("creator", ParseUser.getCurrentUser());
                                            gifFile.put("gifFile", _gifFile);
                                            gifFile.put("thumbnail", _thumbnail);
                                            gifFile.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    dia.dismiss();
                                                    if(e == null) {
                                                        Selfiecon selfiecon = new Selfiecon(gifFile.getObjectId(), _gifFile.getUrl(), _thumbnail.getUrl());
                                                        intent.putExtra("newSelficon", selfiecon);
                                                        activity.setResult(Activity.RESULT_OK, intent);
                                                        activity.finish();
                                                    } else {
                                                        activity.setResult(Activity.RESULT_CANCELED);
                                                        activity.finish();
                                                        Toast.makeText(getContext(), TAG + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            activity.setResult(Activity.RESULT_CANCELED);
                                            activity.finish();
                                            dia.dismiss();
                                            Toast.makeText(getContext(), TAG + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                activity.setResult(Activity.RESULT_CANCELED);
                                activity.finish();
                                dia.dismiss();
                                Toast.makeText(getContext(), TAG + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "Error Writing: " + e.getMessage());
        }
    }


    private void stopPreview() {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.d(TAG, "tried to stop a non-existent preview");
        }
    }

    private void configureCamera() {
        setWillNotDraw(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
        setOnClickListener(this);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setZoom(0);
        parameters.setJpegQuality(100);
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
    }

    private void startPreview(SurfaceHolder holder) {
        if (mCamera == null) {
            return;
        }
        try {
            configureCamera();
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}