package datapp.machat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import datapp.machat.R;
import datapp.machat.custom.CustomActivity;

public class RegisterActivity extends CustomActivity {
    private EditText inputUsername;
    private EditText inputEmail;
    private EditText inputPassword;
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setTouchNClick(R.id.registerBtn);

        inputUsername = (EditText) findViewById(R.id.register_username);
        inputPassword = (EditText) findViewById(R.id.register_password);
        inputEmail = (EditText) findViewById(R.id.register_email);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if(v.getId() == R.id.registerBtn) {
            String username, email, password;
            username = inputUsername.getText().toString();
            email = inputEmail.getText().toString();
            password = inputPassword.getText().toString();

            if(username.length() == 0){
                inputUsername.setError(getString(R.string.required_error));
                return;
            } else if(username.length() < 6) {
                inputUsername.setError(getString(R.string.username_length_error));
                return;
            }

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
            if(password.length() == 0){
                inputPassword.setError(getString(R.string.required_error));
                return;
            } else if(password.length() < 6) {
                inputPassword.setError(getString(R.string.pass_length_error));
                return;
            }


            //All is good
            final ProgressDialog dia = ProgressDialog.show(this, null,
                    getString(R.string.alert_wait));

            final ParseUser pu = new ParseUser();
            pu.setUsername(username);
            pu.setEmail(email);
            pu.setPassword(password);

            pu.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    dia.dismiss();
                    if(e == null) {
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
