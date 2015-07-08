package datapp.machat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.faradaj.blurbehind.BlurBehind;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import datapp.machat.R;
import datapp.machat.adapter.MessageAdapter;
import datapp.machat.custom.CustomActivity;

public class ChatActivity extends CustomActivity {
    private final String TAG = "ChatActivity";
    private ParseUser sender;
    private ParseUser receiver;

    private String senderFbId;
    private String receiverFbId;

    private ListView chatListView;
    private EditText messageEditText;
    private Button messageSendBtn;
    private Button messageSelfieBtn;
    private FrameLayout loading;
    private ArrayList<ParseObject> messageList;
    private MessageAdapter messageAdapter;
    private LinearLayout mainContainer;
    private boolean isRunning;
    private Date lastMsgDate;
    private static Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        BlurBehind.getInstance()
                .withAlpha(65)
                .withFilterColor(Color.parseColor("#B5008795"))
                .setBackground(this);

        sender = ParseUser.getCurrentUser();

        receiverFbId = getIntent().getStringExtra("receiverFbId");
        senderFbId = getIntent().getStringExtra("senderFbId");

        messageEditText = (EditText)findViewById(R.id.new_message_content);
        messageEditText .setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        messageSendBtn = (Button)findViewById(R.id.send_new_message_btn);
        messageSelfieBtn = (Button)findViewById(R.id.send_new_selfiecon_btn);

        mainContainer = (LinearLayout) findViewById(R.id.main_container);
        loading = (FrameLayout) findViewById(R.id.loading_chat);

        chatListView = (ListView) findViewById(R.id.chatSession);
        chatListView.setDivider(null);
        chatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        chatListView.setAdapter(messageAdapter);
        //setPadding(mainContainer);
        handler = new Handler();

        setTouchNClick(R.id.send_new_message_btn);
        setTouchNClick(R.id.send_new_selfiecon_btn);

        loading.setVisibility(View.VISIBLE);

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

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        _fetchReceiver();
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
            String messageText = messageEditText.getText().toString();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
            messageEditText.setText(null);
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
        } else if(v.getId() == R.id.send_new_selfiecon_btn){
            //TODO: selfie stuff goes here
        }
    }

    private void setPadding(View v){
        int actionBarHeight = 0, statusBarHeight = 0, defaultPadding = 0;
        statusBarHeight = getStatusBarHeight();
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        int dpAsPixelsTop = statusBarHeight + defaultPadding + actionBarHeight;
        int dpAsPixelsBottom = getNavigationBarHeight();
        v.setPadding(v.getPaddingLeft(),dpAsPixelsTop, v.getPaddingRight(), dpAsPixelsBottom);

    }

    private int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void _fetchReceiver() {
        //fetch chat history
        ParseQuery query = ParseUser.getQuery();
        query.whereEqualTo("fbId", receiverFbId);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if(e == null && parseUser != null) {
                    receiver = parseUser;
                    getSupportActionBar().setTitle(receiver.getString("fName") + " " + receiver.getString("lName"));
                    _loadConversation();
                } else {
                    finish();
                    Toast.makeText(ChatActivity.this, "Error loading chat history!", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        query.orderByAscending("createdAt");
        query.setLimit(50);
        query.include("from");
        query.include("to");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if(e == null) {
                    loading.setVisibility(View.GONE);
                    if(list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            ParseObject messageObj = list.get(i);
                            messageList.add(messageObj);
                            if (lastMsgDate == null || lastMsgDate.before(messageObj.getCreatedAt()))
                                lastMsgDate = messageObj.getCreatedAt();
                        }
                        messageAdapter.notifyDataSetChanged();
                    }
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
}
