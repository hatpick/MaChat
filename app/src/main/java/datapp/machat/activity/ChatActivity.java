package datapp.machat.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.faradaj.blurbehind.BlurBehind;
import com.faradaj.blurbehind.OnBlurCompleteListener;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import datapp.machat.R;
import datapp.machat.adapter.MessageAdapter;
import datapp.machat.adapter.SelfieconAdapter;
import datapp.machat.application.MaChatApplication;
import datapp.machat.custom.CircleTransform;
import datapp.machat.custom.CustomActivity;
import datapp.machat.custom.UserStatus;
import datapp.machat.dao.GiphyGIF;
import datapp.machat.dao.Selfiecon;
import datapp.machat.helper.LocationHelper;
import datapp.machat.helper.ResizeAnimation;
import datapp.machat.helper.SendNotification;
import datapp.machat.helper.SizeHelper;

public class ChatActivity extends CustomActivity {
    private final String TAG = "ChatActivity";
    private ParseUser sender;
    private ParseUser receiver;

    private String senderFbId;
    private String receiverFbId;

    private String sessionId;

    private ListView chatListView;
    private EditText messageEditText;
    private Button messageSendBtn;
    private Button locationSendBtn;
    private FrameLayout loading;

    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FOLDER = "/saved_recordings";
    private MediaRecorder recorder = null;
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };

    private ArrayList<ParseObject> messageList;
    private MessageAdapter messageAdapter;

    private boolean isRunning;

    private Date lastMsgDate;
    private Date firstMsgDate;

    private static Handler handler;

    private SharedPreferences sessionDetails;

    private final int RESULT_LOAD_IMAGE = 1127;
    private final int RESULT_SEARCH_GIPHY = 1327;
    private final int RESULT_CREATE_GIF = 1227;
    private final int RESULT_CREATE_GIF_NEW = 1247;

    SwipeRefreshLayout swipeContainer;
    private boolean keyboardVisible = false;
    private LocationHelper myLocation;
    private SharedPreferences.Editor sessionEditor;

    private FrameLayout moreActions;

    private Location mLocation;
    private final int IMAGE_MAX_SIZE = 500000;
    private Button voiceSendBtn;

    private SharedPreferences notificationDetails;

    private String getRecordingFilename(){
        String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MaChat" + AUDIO_RECORDER_FOLDER;
        File f = new File(root);
        if(!f.exists()) f.mkdirs();
        String fileName = root + "/" + System.currentTimeMillis() + file_exts[currentFormat];
        Log.v(TAG, fileName);
        return fileName;
    }

    private File getLatestFilefromDir(String dirPath){
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    private void startRecording(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getRecordingFilename());
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);
        recorder.setMaxDuration(30000);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.v(TAG, "Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            //Log.v(TAG, "Warning: " + what + ", " + extra);
        }
    };
    private void stopRecording(){
        if(recorder != null){
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        notificationDetails = getSharedPreferences("notificationDetails", MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        BlurBehind.getInstance()
                .withAlpha(75)
                .withFilterColor(Color.parseColor("#B5008795"))
                .setBackground(this);

        if (savedInstanceState != null) {
            byte[] bitmapData = savedInstanceState.getByteArray("bg");
            if (bitmapData != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                BitmapDrawable bd = new BitmapDrawable(bitmap);
                bd.setAlpha(75);
                bd.setColorFilter(Color.parseColor("#B5008795"), PorterDuff.Mode.DST_ATOP);
                getWindow().getDecorView().setBackground(bd);
            }
        }

        myLocation = new LocationHelper();
        sessionDetails = this.getSharedPreferences("sessionDetails", MODE_PRIVATE);
        sessionEditor = sessionDetails.edit();

        sender = ParseUser.getCurrentUser();

        messageEditText = (EditText) findViewById(R.id.new_message_content);
        messageEditText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        messageSendBtn = (Button) findViewById(R.id.send_new_message_btn);
        voiceSendBtn = (Button) findViewById(R.id.record_void_btn);
        loading = (FrameLayout) findViewById(R.id.loading_chat);

        moreActions = (FrameLayout) findViewById(R.id.more_actions);
        locationSendBtn = (Button) findViewById(R.id.add_location_btn);

        chatListView = (ListView) findViewById(R.id.chatSession);
        chatListView.setDivider(getResources().getDrawable(R.drawable.transparent_divider));
        chatListView.setDividerHeight(10);
        chatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);


        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        final View activityRootView = findViewById(R.id.main_container);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                activityRootView.getWindowVisibleDisplayFrame(r);
                int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 500) {
                    keyboardVisible = true;
                } else {
                    keyboardVisible = false;
                }
            }
        });

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        chatListView.setAdapter(messageAdapter);

        //setPadding(mainContainer);
        handler = new Handler();

        setTouchNClick(R.id.send_new_message_btn);
        setTouchNClick(R.id.send_new_selfiecon_btn);
        setTouchNClick(R.id.new_message_content);
        setTouchNClick(R.id.add_attachment_btn);
        setTouchNClick(R.id.record_void_btn);
        setTouchNClick(R.id.add_location_btn);
        setTouchNClick(R.id.toggle_more_options);
        setTouchNClick(R.id.search_giphy_gifs);
        setTouchNClick(R.id.close_more_actions);
        setTouchNClick(R.id.camera_selfiecon_btn);

        loading.setVisibility(View.VISIBLE);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRunning = false;
                _loadOldConversation();
            }
        });

        swipeContainer.setColorSchemeResources(R.color.color1,
                R.color.color2, R.color.color3);


        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Animation fadeIn = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.fade_in_quick);
                Animation fadeOut = AnimationUtils.loadAnimation(ChatActivity.this, R.anim.fade_out_quick);
                fadeIn.setFillAfter(true);
                fadeOut.setFillAfter(true);
                fadeIn.setFillEnabled(true);
                fadeOut.setFillEnabled(true);
                fadeIn.setInterpolator(new BounceInterpolator());
                fadeOut.setInterpolator(new BounceInterpolator());
                if (charSequence.length() > 0) {
                    messageSendBtn.setEnabled(true);
                    voiceSendBtn.setEnabled(false);
//                    messageSendBtn.startAnimation(fadeIn);
//                    voiceSendBtn.startAnimation(fadeOut);
                    voiceSendBtn.setVisibility(View.GONE);
                    messageSendBtn.setVisibility(View.VISIBLE);
                } else {
                    messageSendBtn.setEnabled(false);
                    voiceSendBtn.setEnabled(true);
//                    messageSendBtn.startAnimation(fadeOut);
//                    voiceSendBtn.startAnimation(fadeIn);
                    voiceSendBtn.setVisibility(View.VISIBLE);
                    messageSendBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    LocationHelper.LocationResult locationResult = new LocationHelper.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                mLocation = location;
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Drawable d = getWindow().getDecorView().getBackground();
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        outState.putByteArray("bg", bitmapdata);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        myLocation.stopUpdates();
        super.onPause();
        isRunning = false;
        UserStatus.setUserOffline();
        if(messageAdapter.getMediaPlayer() != null) {
            messageAdapter.getMediaPlayer().stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(messageAdapter.getMediaPlayer() != null) {
            messageAdapter.getMediaPlayer().release();
            messageAdapter.setMediaPlayer(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserStatus.setUserOnline();
        myLocation.getLocation(this, locationResult);

        receiverFbId = getIntent().getStringExtra("receiverFbId");
        if (receiverFbId == null) {
            receiverFbId = sessionDetails.getString("receiverFbId", "");
        } else {
            sessionEditor.putString("receiverFbId", receiverFbId);
            sessionEditor.apply();
        }

        senderFbId = getIntent().getStringExtra("senderFbId");

        isRunning = true;
        _fetchReceiver();
    }

    private void sendMessage(final ParseUser receiver, final String type, final String content, final ParseGeoPoint location, final String gifUrl) {
        ParseQuery<ParseObject> query = new ParseQuery("Session");
        query.whereEqualTo("user", receiver);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null && parseObjects.size() > 0) {
                    if (!receiver.getBoolean("inApp")) {
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("toId", receiver.getObjectId());
                        params.put("msgType", type);
                        params.put("msgContent", content);
                        params.put("toId", receiver.getObjectId());
                        ParseCloud.callFunctionInBackground("sendPushMessage", params, new FunctionCallback<String>() {
                            @Override
                            public void done(String s, ParseException e) {
                                if (e != null) {
                                    Toast.makeText(ChatActivity.this, "Push failed!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

                final ParseObject message = new ParseObject("Message");
                message.put("from", sender);
                message.put("to", receiver);
                message.put("content", content);
                message.put("type", type);
                message.put("sessionId", sender.getObjectId() + receiver.getObjectId());
                message.put("status", "sent");
                message.put("isPlaying", false);
                if (type.equals("Location") && location != null)
                    message.put("location", location);
                if(type.equals("selfiecon") && gifUrl != null)
                    message.put("gifUrl", gifUrl);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();

                message.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            message.put("status", "delivered");
                            message.saveEventually(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    messageAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.send_new_message_btn) {
            final String messageText = messageEditText.getText().toString();
            messageEditText.setText(null);

            final String videoId = SizeHelper.extractYTId(messageText);
            final boolean isVine = SizeHelper.isVine(messageText);

            receiver.fetchInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (e == null) {
                        receiver = parseUser;
                        if (videoId != null) {
                            sendMessage(receiver, "youtube", videoId, null, null);
                        } else if (isVine) {
                            JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, "http://web.engr.oregonstate.edu/~ghorashi/vine/vine.php?url=" + messageText, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        sendMessage(receiver, "vine", response.getString("video"), null, null);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            });
                            MaChatApplication.getInstance().getRequestQueue().add(jor);
                        } else {
                            sendMessage(receiver, "text", messageText, null, null);
                        }
                    }
                }
            });
        } else if (v.getId() == R.id.send_new_selfiecon_btn) {
            BlurBehind.getInstance().execute(ChatActivity.this, new OnBlurCompleteListener() {
                @Override
                public void onBlurComplete() {
                    Intent selfieconIntent = new Intent(ChatActivity.this, SelficonActivity.class);
                    selfieconIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    selfieconIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(selfieconIntent, RESULT_CREATE_GIF);
                }
            });
            _toggleMoreActions();
        } else if (v.getId() == R.id.camera_selfiecon_btn) {
            BlurBehind.getInstance().execute(ChatActivity.this, new OnBlurCompleteListener() {
                @Override
                public void onBlurComplete() {
                    Intent selfieconIntent = new Intent(ChatActivity.this, SelfieconCameraActivity.class);
                    selfieconIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    selfieconIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(selfieconIntent, RESULT_CREATE_GIF_NEW);
                }
            });
            _toggleMoreActions();
        } else if (v.getId() == R.id.toggle_more_options) {
            if (keyboardVisible) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
            }
            _toggleMoreActions();
        } else if (v.getId() == R.id.close_more_actions) {
            _toggleMoreActions();
        } else if (v.getId() == R.id.record_void_btn) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this);
            LayoutInflater factory = LayoutInflater.from(ChatActivity.this);
            View innerView = factory.inflate(R.layout.voice_recorder, null);
            final ArcProgress arcProgress = (ArcProgress) innerView.findViewById(R.id.arc_progress);
            alert.setView(innerView);
            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            final AlertDialog dialog = alert.show();
            arcProgress.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Timer timer = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    arcProgress.setProgress(arcProgress.getProgress() + 1);
                                }
                            });
                        }
                    };

                    switch(motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            startRecording();
                            timer.schedule(task, 100, 1000);
                            return true;
                        case MotionEvent.ACTION_UP:
                            Log.v(TAG, "ACTION UP");
                            task.cancel();
                            timer.cancel();
                            timer.purge();
                            stopRecording();

                            String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MaChat" + AUDIO_RECORDER_FOLDER;
                            File recording = getLatestFilefromDir(root);
                            Log.v(TAG, recording.getAbsolutePath());
                            try{
                                FileInputStream fis = new FileInputStream(recording);
                                ByteArrayOutputStream bos= new ByteArrayOutputStream();
                                byte[] buf = new byte[(int)recording.length()];

                                for (int readNum; (readNum=fis.read(buf)) != -1;){
                                    bos.write(buf,0,readNum);
                                }

                                byte[] bytes = bos.toByteArray();
                                final ParseFile voiceFile = new ParseFile (recording.getName(), bytes);
                                voiceFile.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null) {
                                            Log.v(TAG, "Saved recording on Parse.com");
                                            sendMessage(receiver, "recording", voiceFile.getUrl(), null, null);
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                            catch (IOException ex) {
                                Toast.makeText(ChatActivity.this, "Error conerting into byte: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            return false;
                    }
                    return false;
                }
            });
        } else if (v.getId() == R.id.add_attachment_btn) {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, RESULT_LOAD_IMAGE);
            _toggleMoreActions();
        } else if (v.getId() == R.id.add_location_btn) {
            locationSendBtn.setEnabled(false);
            if (mLocation != null) {
                final ParseGeoPoint loc = new ParseGeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
                receiver.fetchInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (e == null) {
                            receiver = parseUser;
                            sendMessage(receiver, "map", "Location", loc, null);
                            locationSendBtn.setEnabled(true);
                            _toggleMoreActions();
                        }

                    }
                });
            } else {
                myLocation.getLocation(this, locationResult);
                Toast.makeText(ChatActivity.this, "Waiting for location, try again in a moment!", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.search_giphy_gifs) {
            BlurBehind.getInstance().execute(ChatActivity.this, new OnBlurCompleteListener() {
                @Override
                public void onBlurComplete() {
                    Intent giphyIntent = new Intent(ChatActivity.this, GiphyActivity.class);
                    giphyIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    giphyIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(giphyIntent, RESULT_SEARCH_GIPHY);
                }
            });
            _toggleMoreActions();
        }
    }

    private Bitmap decodeFile(File f, int scale) {
        Bitmap b = null;
        FileInputStream fis;
        int innerScale = 1;
        try {
            if (scale == -1) {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;

                fis = new FileInputStream(f);
                BitmapFactory.decodeStream(fis, null, o);
                fis.close();

                while ((o.outWidth * o.outHeight) * (1 / Math.pow(2, innerScale)) >
                        IMAGE_MAX_SIZE) {
                    innerScale++;
                }
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = (scale == -1) ? innerScale : 1;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return b;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_CREATE_GIF:
                if (resultCode == RESULT_OK) {
                    final Selfiecon selfie = data.getParcelableExtra("newSelfiecon");
                    receiver.fetchInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (e == null) {
                                receiver = parseUser;

                                sendMessage(receiver, "selfiecon", selfie.getThumbnailUrl(), null, selfie.getGifUrl());
                            }

                        }
                    });
                }
                break;
            case RESULT_CREATE_GIF_NEW:
                if (resultCode == RESULT_OK) {
                    final Selfiecon selfie = data.getParcelableExtra("newSelfiecon");
                    receiver.fetchInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (e == null) {
                                receiver = parseUser;
                                sendMessage(receiver, "selfiecon", selfie.getThumbnailUrl(), null, selfie.getGifUrl());
                            }

                        }
                    });
                }
                break;
            case RESULT_LOAD_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    File f = new File(filePath);

                    Bitmap selectedImageBitmap = null;

                    selectedImageBitmap = decodeFile(f, -1);
                    if (selectedImageBitmap == null) {
                        Toast.makeText(ChatActivity.this, "Attaching image failed!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Random generator = new Random();
                    int n = 10000;
                    n = generator.nextInt(n);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
                    final ParseFile imageFile = new ParseFile("media_" + n + ".jpg", out.toByteArray());
                    imageFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            receiver.fetchInBackground(new GetCallback<ParseUser>() {
                                @Override
                                public void done(ParseUser parseUser, ParseException e) {
                                    if (e == null) {
                                        receiver = parseUser;
                                        sendMessage(receiver, "media", imageFile.getUrl(), null, null);
                                    }

                                }
                            });
                        }
                    });
                }
                break;
            case RESULT_SEARCH_GIPHY:
                if (resultCode == RESULT_OK) {
                    final GiphyGIF newgiphyGIF = data.getParcelableExtra("newgiphyGIF");
                    receiver.fetchInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (e == null) {
                                receiver = parseUser;
                                sendMessage(receiver, "giphy", newgiphyGIF.getSmallSizedUrl(), null, null);
                            }

                        }
                    });

                }
                break;
        }

    }

    private void _fetchReceiver() {
        //fetch chat history
        ParseQuery query = ParseUser.getQuery();
        query.whereEqualTo("fbId", receiverFbId);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null && parseUser != null) {
                    receiver = parseUser;
                    _setupActionBar();
                    sessionId = sender.getObjectId() + receiver.getObjectId();
                    int notificationId = notificationDetails.getInt(sessionId, -1);
                    if(notificationId != -1) {
                        SendNotification.cancelNotification(notificationId);
                    }
                    _loadConversation();
                } else {
                    finish();
                    Toast.makeText(ChatActivity.this, "Error loading chat history!", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

        TextView user_name = new TextView(actionBar.getThemedContext());
        user_name.setText(receiver.getString("fName") + " " + receiver.getString("lName"));
        user_name.setPadding(20, 0, 0, 0);
        user_name.setTextSize(16);
        ActionBar.LayoutParams txtLayoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        txtLayoutParams.leftMargin = 20;
        user_name.setLayoutParams(txtLayoutParams);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
        dateFormat.setTimeZone(TimeZone.getDefault());

        TextView user_membership = new TextView(actionBar.getThemedContext());
        user_membership.setText("Member since " + dateFormat.format(receiver.getCreatedAt()));
        user_membership.setPadding(20, 0, 0, 0);
        user_membership.setTextSize(12);
        user_membership.setLayoutParams(txtLayoutParams);

        textLinearLayout.addView(user_name);
        textLinearLayout.addView(user_membership);

        ImageView pp_picture = new ImageView(actionBar.getThemedContext());
        Transformation transformation = new CircleTransform(this);

        Glide.with(this)
                .load(receiver.getParseFile("profilePicture").getUrl())
                .centerCrop()
                .crossFade()
                .transform(transformation)
                .into(pp_picture);
        ActionBar.LayoutParams imgLayoutParams = new ActionBar.LayoutParams((int) SizeHelper.convertDpToPixel(40, this), (int) SizeHelper.convertDpToPixel(40, this), Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        pp_picture.setLayoutParams(imgLayoutParams);

        linearLayout.addView(pp_picture);
        linearLayout.addView(textLinearLayout);


        actionBar.setCustomView(linearLayout);
    }

    @Override
    public void onBackPressed() {
        if (moreActions.getHeight() > 0) {
            _toggleMoreActions();
        } else {
            super.onBackPressed();
        }
    }

    private void _toggleMoreActions() {
        final ResizeAnimation anim = new ResizeAnimation(moreActions);
        anim.setDuration(150);
        anim.setInterpolator(new AccelerateInterpolator());
        if (moreActions.getHeight() > 0) {
            messageEditText.setFocusableInTouchMode(true);
            messageEditText.setFocusable(true);
            messageEditText.requestLayout();
            anim.setParams((int) SizeHelper.convertDpToPixel(70f, this), 0, SizeHelper.getDisplayWidth(this), 0);
        } else {
            messageEditText.setFocusableInTouchMode(false);
            messageEditText.setFocusable(false);
            messageEditText.requestLayout();
            anim.setParams(0, (int) SizeHelper.convertDpToPixel(70f, this), 0, SizeHelper.getDisplayWidth(this));
        }
        moreActions.startAnimation(anim);
    }

    private void _loadConversation() {
        ParseQuery<ParseObject> query = new ParseQuery("Message");
        if (messageList.size() == 0) {
            query.whereContainedIn("sessionId", Arrays.asList(sender.getObjectId() + receiver.getObjectId(), receiver.getObjectId() + sender.getObjectId()));
        } else {
            if (lastMsgDate != null)
                query.whereGreaterThan("createdAt", lastMsgDate);
            query.whereEqualTo("to", sender);
            query.whereEqualTo("from", receiver);
        }
        query.orderByDescending("createdAt");
        query.setLimit(15);
        query.include("gif");
        query.include("from");
        query.include("to");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    loading.setVisibility(View.GONE);
                    if (list.size() > 0) {
                        for (int i = list.size() - 1; i >= 0; i--) {
                            ParseObject messageObj = list.get(i);
                            messageList.add(messageObj);
                            if (lastMsgDate == null || lastMsgDate.before(messageObj.getCreatedAt()))
                                lastMsgDate = messageObj.getCreatedAt();
                        }
                        firstMsgDate = messageList.get(0).getCreatedAt();
                        messageAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Network issue!", Toast.LENGTH_SHORT).show();
                }
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (isRunning)
                            _loadConversation();
                    }
                }, 1000);
            }
        });
    }

    private void _loadOldConversation() {
        ParseQuery<ParseObject> query = new ParseQuery("Message");
        query.whereContainedIn("sessionId", Arrays.asList(sender.getObjectId() + receiver.getObjectId(), receiver.getObjectId() + sender.getObjectId()));
        query.orderByDescending("createdAt");
        query.setLimit(15);
        if (firstMsgDate != null) {
            query.whereLessThan("createdAt", firstMsgDate);
        }
        query.include("gif");
        query.include("from");
        query.include("to");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    ArrayList<ParseObject> tempArray = new ArrayList<>();
                    loading.setVisibility(View.GONE);
                    if (list.size() > 0) {
                        for (int i = list.size() - 1; i >= 0; i--) {
                            ParseObject messageObj = list.get(i);
                            tempArray.add(messageObj);
                            if (firstMsgDate == null || firstMsgDate.after(messageObj.getCreatedAt()))
                                firstMsgDate = messageObj.getCreatedAt();
                        }
                        for (ParseObject obj : messageList)
                            tempArray.add(obj);
                        messageList.clear();
                        messageList.addAll(tempArray);
                        messageAdapter.notifyDataSetChanged();
                        chatListView.setSelection(15);
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Network issue!", Toast.LENGTH_SHORT).show();
                }
                isRunning = true;
                swipeContainer.setRefreshing(false);
            }
        });
    }
}
