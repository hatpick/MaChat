package datapp.machat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import datapp.machat.R;
import datapp.machat.custom.CustomActivity;
import datapp.machat.helper.BlurBuilder;

public class LoginActivity extends CustomActivity {
    private EditText inputUsername;
    private EditText inputPassword;
    private TextView forgotPassworBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTouchNClick(R.id.loginBtn);
        setTouchNClick(R.id.registerBtn);
        setTouchNClick(R.id.facebookBtn);

        inputUsername = (EditText) findViewById(R.id.login_username);
        inputPassword = (EditText) findViewById(R.id.login_password);
        forgotPassworBtn= (TextView) findViewById(R.id.forgotPasswordBtn);

        forgotPassworBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(v.getId() == R.id.registerBtn){
            startActivity(new Intent(this, RegisterActivity.class));
        } else if(v.getId() == R.id.loginBtn){
            String username = inputUsername.getText().toString();
            String password = inputPassword.getText().toString();

            if(username.length() == 0){
                inputUsername.setError(getString(R.string.required_error));
                return;
            }
            if(password.length() == 0){
                inputPassword.setError(getString(R.string.required_error));
                return;
            }

            final ProgressDialog dia = new ProgressDialog(this);
            dia.setContentView(R.layout.progress_dialog);
            TextView diaTitle = (TextView) dia.findViewById(R.id.pd_title);
            diaTitle.setText(getString(R.string.alert_wait));
            ProgressBar diaProgressBar = (ProgressBar ) dia.findViewById(R.id.pd_progressBar);
            diaProgressBar.setProgress(77);
            dia.show();

            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    dia.dismiss();
                    if(parseUser != null) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        } else if(v.getId() == R.id.facebookBtn){
            final ProgressDialog dia = new ProgressDialog(this);
            dia.show();
            dia.setContentView(R.layout.progress_dialog);
            TextView diaTitle = (TextView) dia.findViewById(R.id.pd_title);
            diaTitle.setText(getString(R.string.alert_wait));
            ProgressBar diaProgressBar = (ProgressBar ) dia.findViewById(R.id.pd_progressBar);
            diaProgressBar.setProgress(77);

            final List<String> permissions = Arrays.asList("public_profile", "email", "user_friends");
            ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
                @Override
                public void done(final ParseUser parseUser, ParseException e) {
                    if (e == null) {
                        if (parseUser == null) {
                            Toast.makeText(LoginActivity.this, "Facebook login failed!", Toast.LENGTH_SHORT).show();
                            dia.dismiss();
                        } else if (parseUser.isNew()) {
                            //New user
                            GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject fbUser, GraphResponse graphResponse) {
                                    ParseUser pUser = ParseUser.getCurrentUser();
                                    if(fbUser != null && pUser != null){
                                        try {
                                            pUser.setEmail(fbUser.getString("email"));
                                            pUser.put("fName", fbUser.getString("first_name"));
                                            pUser.put("lName", fbUser.getString("last_name"));
                                            pUser.put("fbId", fbUser.getString("id"));
                                            pUser.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    dia.dismiss();
                                                    if(e == null){
                                                        GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
                                                            @Override
                                                            public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                                                                ParseUser pUser = ParseUser.getCurrentUser();
                                                                if(jsonArray.length() > 0){
                                                                    pUser.addAllUnique("friends", Arrays.asList(jsonArray));
                                                                    parseUser.saveEventually();
                                                                }

                                                                //TODO: sync friends
                                                            }
                                                        }).executeAsync();

                                                        HashMap<String, Object> params = new HashMap<String, Object>();
                                                        ParseCloud.callFunctionInBackground("fetchProfilePicture", params, new FunctionCallback<String>() {
                                                            @Override
                                                            public void done(String s, ParseException e) {
                                                                if (e == null) {
                                                                    ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {
                                                                        @Override
                                                                        public void done(ParseUser currentUser, ParseException e) {
                                                                            if(e != null){
                                                                                Toast.makeText(LoginActivity.this, "PPFAILED: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                                                finish();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });

                                                    } else {
                                                        dia.dismiss();
                                                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    } else {
                                        dia.dismiss();
                                    }
                                }
                            }).executeAsync();
                        } else {
                            //Returning user
                            HashMap<String, Object> params = new HashMap<String, Object>();
                            ParseCloud.callFunctionInBackground("fetchProfilePicture", params, new FunctionCallback<String>() {
                                @Override
                                public void done(String s, ParseException e) {
                                    if (e == null) {
                                        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {
                                            @Override
                                            public void done(ParseUser currentUser, ParseException e) {
                                            if(e != null){
                                                Toast.makeText(LoginActivity.this, "PPFAILED: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(LoginActivity.this, "PPFAILED: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            dia.dismiss();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    final private void getFacebookIdInBackground() {
    }

    private int calAge(Date bd) {
        Calendar birthCal = new GregorianCalendar(bd.getYear(), bd.getMonth(), bd.getDay());
        Calendar nowCal = new GregorianCalendar();

        int age = nowCal.get(Calendar.YEAR) - (1900 + birthCal.get(Calendar.YEAR));

        boolean isMonthGreater = birthCal.get(Calendar.MONTH) >= nowCal
                .get(Calendar.MONTH);

        boolean isMonthSameButDayGreater = birthCal.get(Calendar.MONTH) == nowCal
                .get(Calendar.MONTH)
                && birthCal.get(Calendar.DAY_OF_MONTH) > nowCal
                .get(Calendar.DAY_OF_MONTH);

        if (isMonthGreater || isMonthSameButDayGreater) {
            age=age-1;
        }
        return age;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}
