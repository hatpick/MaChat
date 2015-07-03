package datapp.machat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import datapp.machat.R;
import datapp.machat.custom.CustomActivity;

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

            final ProgressDialog dia = ProgressDialog.show(this, null,
                    getString(R.string.alert_wait));

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
            final List<String> permissions = Arrays.asList("public_profile", "email", "user_friends");
            ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (e == null) {
                        if (parseUser == null) {
                            Toast.makeText(LoginActivity.this, "Facebook login failed!", Toast.LENGTH_SHORT).show();
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
                                                    if(e == null){
                                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }
                            }).executeAsync();
                            GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
                                @Override
                                public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                                    if(jsonArray.length() > 0){

                                    }
                                }
                            }).executeAsync();
                        } else {
                            //Returning user
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
