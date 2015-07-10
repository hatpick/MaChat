package datapp.machat.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.faradaj.blurbehind.BlurBehind;
import com.faradaj.blurbehind.OnBlurCompleteListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import datapp.machat.R;
import datapp.machat.adapter.MessageAdapter;
import datapp.machat.adapter.SelfieconAdapter;
import datapp.machat.custom.CircleTransform;
import datapp.machat.custom.CustomActivity;
import datapp.machat.helper.SizeHelper;

public class ChatActivity extends CustomActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String TAG = "ChatActivity";
    private ParseUser sender;
    private ParseUser receiver;

    private String senderFbId;
    private String receiverFbId;

    private ListView chatListView;
    private GridView selficonGridView;
    private FrameLayout selfieconKeyboard;
    private EditText messageEditText;
    private Button messageSendBtn;
    private Button locationSendBtn;
    private FrameLayout loading;
    private ArrayList<ParseObject> messageList;
    private ArrayList<ParseObject> selficonList;
    private MessageAdapter messageAdapter;
    private SelfieconAdapter selfieconAdapter;
    private boolean isRunning;
    private Date lastMsgDate;
    private Date firstMsgDate;
    private static Handler handler;
    private SharedPreferences sessionDetails;
    private final int RESULT_LOAD_IMAGE = 1317;
    SwipeRefreshLayout swipeContainer;
    private boolean keyboardVisible = false;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = true;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        updateValuesFromBundle(savedInstanceState);

        sessionDetails = this.getSharedPreferences("sessionDetails", MODE_PRIVATE);
        SharedPreferences.Editor sessionEditor = sessionDetails.edit();

        BlurBehind.getInstance()
                .withAlpha(65)
                .withFilterColor(Color.parseColor("#B5008795"))
                .setBackground(this);

        buildGoogleApiClient();

        sender = ParseUser.getCurrentUser();

        receiverFbId = getIntent().getStringExtra("receiverFbId");
        if(receiverFbId == null) {
            receiverFbId = sessionDetails.getString("receiverFbId", "");
        } else {
            sessionEditor.putString("receiverFbId", receiverFbId);
            sessionEditor.apply();
        }

        senderFbId = getIntent().getStringExtra("senderFbId");
        messageEditText = (EditText)findViewById(R.id.new_message_content);
        messageEditText .setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        messageSendBtn = (Button)findViewById(R.id.send_new_message_btn);
        locationSendBtn = (Button)findViewById(R.id.add_location_btn);
        selfieconKeyboard = (FrameLayout) findViewById(R.id.selfiecon_keyboard);
        loading = (FrameLayout) findViewById(R.id.loading_chat);

        chatListView = (ListView) findViewById(R.id.chatSession);
        chatListView.setDivider(getResources().getDrawable(R.drawable.transparent_divider));
        chatListView.setDividerHeight(10);
        chatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);

        selficonGridView = (GridView) findViewById(R.id.selfiecon_gridview);
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

        selficonGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });

        selficonGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final ParseObject selfie = selfieconAdapter.getItem(i);
                receiver.fetchInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if(e == null){
                            receiver = parseUser;
                            if(!receiver.getBoolean("inApp")) {
                                HashMap<String, Object> params = new HashMap<String, Object>();
                                params.put("toId", receiver.getObjectId());
                                params.put("msgType", "selfiecon");
                                params.put("msgContent", selfie.getParseFile("gifFile").getUrl());
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

                            final ParseObject message = new ParseObject("Message");
                            message.put("from", sender);
                            message.put("to", receiver);
                            message.put("content", selfie.getParseFile("gifFile").getUrl());
                            message.put("type", "selfiecon");
                            message.put("sessionId", sender.getObjectId() + receiver.getObjectId());
                            message.put("status", "sent");
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

                    }
                });
            }
        });

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        chatListView.setAdapter(messageAdapter);

        selficonList = new ArrayList<>();
        selfieconAdapter = new SelfieconAdapter(this, selficonList);
        selficonGridView.setAdapter(selfieconAdapter);

        //setPadding(mainContainer);
        handler = new Handler();

        setTouchNClick(R.id.send_new_message_btn);
        setTouchNClick(R.id.send_new_selfiecon_btn);
        setTouchNClick(R.id.new_message_content);
        setTouchNClick(R.id.create_new_selfiecon_btn);
        setTouchNClick(R.id.add_attachment_btn);
        setTouchNClick(R.id.add_location_btn);

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
                if(charSequence.length() > 0)
                    messageSendBtn.setEnabled(true);
                else
                    messageSendBtn.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void _toggleSelfieconKeyboard(){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) selfieconKeyboard.getLayoutParams();
        params.height = (params.height == 0)?(int)SizeHelper.convertDpToPixel(LinearLayout.LayoutParams.WRAP_CONTENT, this) : 0;
        selfieconKeyboard.setLayoutParams(params);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //isRunning = false;
        stopLocationUpdates();
        ParseUser.getCurrentUser().put("inApp", false);
        ParseUser.getCurrentUser().saveInBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        _fetchReceiver();
        _fetchSelficons();
        ParseUser.getCurrentUser().put("inApp", true);
        ParseUser.getCurrentUser().saveInBackground();
    }

    private void _fetchSelficons() {
        selficonList.clear();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("GIF");
        query.whereEqualTo("creator", ParseUser.getCurrentUser());
        query.setSkip(selficonList.size());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> selficonObjs, ParseException e) {
                if (e == null) {
                    if (selficonObjs.size() > 0) {
                        for (int i = 0; i < selficonObjs.size(); i++) {
                            selficonList.add(selficonObjs.get(i));
                        }
                        selfieconAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Error loading selfiecons!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(v.getId() == R.id.send_new_message_btn) {
            final String messageText = messageEditText.getText().toString();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
            messageEditText.setText(null);

            receiver.fetchInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if(e == null){
                        receiver = parseUser;
                        if(!receiver.getBoolean("inApp")) {
                            HashMap<String, Object> params = new HashMap<String, Object>();
                            params.put("toId", receiver.getObjectId());
                            params.put("msgType", "text");
                            params.put("msgContent", messageText);
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

                        final ParseObject message = new ParseObject("Message");
                        message.put("from", sender);
                        message.put("to", receiver);
                        message.put("content", messageText);
                        message.put("type", "text");
                        message.put("sessionId", sender.getObjectId() + receiver.getObjectId());
                        message.put("status", "sent");
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

                }
            });
        } else if(v.getId() == R.id.send_new_selfiecon_btn){
            if(keyboardVisible){
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            _toggleSelfieconKeyboard();
        } else if(v.getId() == R.id.new_message_content) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) selfieconKeyboard.getLayoutParams();
            if(params.height == 0) return;
            params.height = 0;
            selfieconKeyboard.setLayoutParams(params);
        } else if(v.getId() == R.id.create_new_selfiecon_btn) {
            BlurBehind.getInstance().execute(ChatActivity.this, new OnBlurCompleteListener() {
                @Override
                public void onBlurComplete() {
                    Intent selfieconIntent = new Intent(ChatActivity.this, SelfieconCameraActivity.class);
                    selfieconIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(selfieconIntent);
                }
            });
        } else if(v.getId() == R.id.add_attachment_btn) {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto , RESULT_LOAD_IMAGE);
        } else if(v.getId() == R.id.add_location_btn) {
            if(mLastLocation != null){
                final ParseGeoPoint loc = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                receiver.fetchInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if(e == null){
                            receiver = parseUser;
                            if(!receiver.getBoolean("inApp")) {
                                HashMap<String, Object> params = new HashMap<String, Object>();
                                params.put("toId", receiver.getObjectId());
                                params.put("msgType", "map");
                                params.put("msgContent", "Location");
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

                            final ParseObject message = new ParseObject("Message");
                            message.put("from", sender);
                            message.put("to", receiver);
                            message.put("content", "Location");
                            message.put("location", loc);
                            message.put("type", "map");
                            message.put("sessionId", sender.getObjectId() + receiver.getObjectId());
                            message.put("status", "sent");
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

                    }
                });
            } else {
                Toast.makeText(ChatActivity.this, "Location is not available!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case RESULT_LOAD_IMAGE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap selectedImageBitmap = BitmapFactory.decodeFile(filePath);
//                    float RESIZE_FACTOR = _getResizeFactor(selectedImageBitmap.getWidth());
//                    if(RESIZE_FACTOR == 0){
//                        Toast.makeText(ChatActivity.this, "Too big!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                    Random generator = new Random();
                    int n = 10000;
                    n = generator.nextInt(n);

                    //Bitmap resizedBitmap = Bitmap.createScaledBitmap(selectedImageBitmap, (int)(selectedImageBitmap.getWidth() * RESIZE_FACTOR), (int)(selectedImageBitmap.getHeight() * RESIZE_FACTOR), true);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
                    final ParseFile imageFile = new ParseFile("media_" + n + ".jpg", out.toByteArray());
                    imageFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            receiver.fetchInBackground(new GetCallback<ParseUser>() {
                                @Override
                                public void done(ParseUser parseUser, ParseException e) {
                                    if(e == null){
                                        receiver = parseUser;
                                        if(!receiver.getBoolean("inApp")) {
                                            HashMap<String, Object> params = new HashMap<String, Object>();
                                            params.put("toId", receiver.getObjectId());
                                            params.put("msgType", "media");
                                            params.put("msgContent", imageFile.getUrl());
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

                                        final ParseObject message = new ParseObject("Message");
                                        message.put("from", sender);
                                        message.put("to", receiver);
                                        message.put("content", imageFile.getUrl());
                                        message.put("type", "media");
                                        message.put("sessionId", sender.getObjectId() + receiver.getObjectId());
                                        message.put("status", "sent");
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

                                }
                            });
                        }
                    });
                }
        }

    };

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
        ActionBar.LayoutParams txtLayoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT , Gravity.LEFT
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
                .centerCrop()
                .transform(transformation)
                .into(pp_picture);
        ActionBar.LayoutParams imgLayoutParams = new ActionBar.LayoutParams((int)SizeHelper.convertDpToPixel(40, this), (int)SizeHelper.convertDpToPixel(40, this) , Gravity.LEFT
                | Gravity.CENTER_VERTICAL);
        pp_picture.setLayoutParams(imgLayoutParams);

        linearLayout.addView(pp_picture);
        linearLayout.addView(textLinearLayout);


        actionBar.setCustomView(linearLayout);
    }

    @Override
    public void onBackPressed() {
        if(selfieconKeyboard.getHeight() > 0) {
            _toggleSelfieconKeyboard();
        } else {
            super.onBackPressed();
        }
    }

    private void _loadConversation() {
        ParseQuery<ParseObject> query = new ParseQuery("Message");
        if(messageList.size() == 0){
            query.whereContainedIn("sessionId", Arrays.asList(sender.getObjectId() + receiver.getObjectId(), receiver.getObjectId() + sender.getObjectId()));
        } else {
            if (lastMsgDate != null)
                query.whereGreaterThan("createdAt", lastMsgDate);
            query.whereEqualTo("to", sender);
            query.whereEqualTo("from", receiver);
        }
        query.orderByDescending("createdAt");
        query.setLimit(50);
        query.include("from");
        query.include("to");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null) {
                    loading.setVisibility(View.GONE);
                    if(list.size() > 0) {
                        for (int i = list.size() - 1; i >= 0; i--) {
                            ParseObject messageObj = list.get(i);
                            messageList.add(messageObj);
                            if (lastMsgDate == null || lastMsgDate.before(messageObj.getCreatedAt()))
                                lastMsgDate = messageObj.getCreatedAt();
                        }
                        firstMsgDate = messageList.get(0).getCreatedAt();
                    }
                    messageAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ChatActivity.this, "Network issue!", Toast.LENGTH_SHORT).show();
                }
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run()
                    {
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
        query.setLimit(50);
        if(firstMsgDate != null) {
            query.whereLessThan("createdAt", firstMsgDate);
        }
        query.include("from");
        query.include("to");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null) {
                    ArrayList<ParseObject> tempArray = new ArrayList<>();
                    loading.setVisibility(View.GONE);
                    if(list.size() > 0) {
                        for (int i = list.size() - 1; i >= 0; i--) {
                            ParseObject messageObj = list.get(i);
                            tempArray.add(messageObj);
                            if (firstMsgDate == null || firstMsgDate.after(messageObj.getCreatedAt()))
                                firstMsgDate = messageObj.getCreatedAt();
                        }
                        for(ParseObject obj:messageList)
                            tempArray.add(obj);
                        messageList.clear();
                        messageList.addAll(tempArray);
                        messageAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "Network issue!", Toast.LENGTH_SHORT).show();
                }
                isRunning = true;
                swipeContainer.setRefreshing(false);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mLastLocation != null) {
            if(mLastLocation.distanceTo(location) >= 200) {
                mLastLocation = location;
            }
        } else {
            mLastLocation = location;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mLastLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
        }
    }

    protected void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setSmallestDisplacement(100);
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }
}
