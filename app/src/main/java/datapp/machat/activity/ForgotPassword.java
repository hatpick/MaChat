package datapp.machat.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import datapp.machat.R;
import datapp.machat.custom.CustomActivity;
import datapp.machat.helper.CircleTransform;

public class ForgotPassword extends CustomActivity {
    private EditText inputEmail;
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        setTouchNClick(R.id.resetPasswordBtn);

        inputEmail = (EditText) findViewById(R.id.forgot_password_email);
//        ImageView gif = (ImageView) findViewById(R.id.gif);
//        Glide.with(this)
//                .load("http://media.giphy.com/media/1a36onP1bmbfi/giphy.gif")
//                .asGif()
//                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                .placeholder(R.drawable.circle_placeholder)
//                .into(gif);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(v.getId() == R.id.resetPasswordBtn){
            String email = inputEmail.getText().toString();
            if(email.length() == 0){
                inputEmail.setError(getString(R.string.required_error));
                return;
            } else {
                Pattern pattern = Pattern.compile(EMAIL_PATTERN);
                Matcher matcher = pattern.matcher(email);
                if(!matcher.matches()) {
                    inputEmail.setError(getString(R.string.email_error));
                    return;
                }
            }

            ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        Toast.makeText(ForgotPassword.this, "An email was successfully sent with reset instructions.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ForgotPassword.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(ForgotPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
