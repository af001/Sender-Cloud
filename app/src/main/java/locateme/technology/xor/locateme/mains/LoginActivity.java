package locateme.technology.xor.locateme.mains;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import locateme.technology.xor.locateme.R;
import locateme.technology.xor.locateme.dialogs.ForgotDialog;
import locateme.technology.xor.locateme.parse.ParseMethods;
import locateme.technology.xor.locateme.support.AppData;

public class LoginActivity extends AppCompatActivity {

    private EditText userEmail;
    private EditText userPass;
    private Button loginBtn;
    private CheckBox newUser;
    private TextView forgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userEmail = (EditText) findViewById(R.id.et_email);
        userPass = (EditText) findViewById(R.id.et_password);
        loginBtn = (Button) findViewById(R.id.btn_signin);
        newUser = (CheckBox) findViewById(R.id.tb_signup);
        forgot = (TextView) findViewById(R.id.tv_signup);

        String msg = "Forgot password? <font color='#1fb6ed'>Click here</font> to reset!";
        forgot.setText(Html.fromHtml(msg));

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgotDialog forgotDialog = new ForgotDialog();
                forgotDialog.AlertUser(LoginActivity.this);
            }
        });

        userEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userEmail.setError(null);
                userPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        userPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userEmail.setError(null);
                userPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userEmail.getText().toString().length() == 0 || !IsValidEmail(userEmail.getText().toString())) {
                    userEmail.setText("");
                    userPass.setText("");
                    userEmail.setError("Invalid email address!");
                } else if (userPass.getText().toString().length() == 0) {
                    userEmail.setText("");
                    userPass.setText("");
                    userPass.setError("Invalid password!");
                } else {
                    UserLogin(userEmail.getText().toString(), userPass.getText().toString());
                }
            }
        });
    }

    private void UserLogin(String username, String password) {

        if (newUser.isChecked()) {
            final ParseUser user = new ParseUser();
            user.setUsername(userEmail.getText().toString());
            user.setPassword(userPass.getText().toString());
            user.setEmail(userEmail.getText().toString());

            // other fields can be set just like with ParseObject
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            if (mPhoneNumber != null)
                user.put("phone", mPhoneNumber);

            user.signUpInBackground(new SignUpCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        LoginSuccess();
                    } else {
                        if (e.getCode() == AppData.PARSE_ACCOUNT_EXISTS) {
                            userEmail.setText("");
                            userPass.setText("");
                            Toast.makeText(getBaseContext(), "Email already exists!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {
                        LoginSuccess();
                    } else {
                        Toast.makeText(getBaseContext(), "Login failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void LoginSuccess() {
        ParseMethods mParseMethods = new ParseMethods();
        mParseMethods.AddUserToInstallation();
        mParseMethods.BecomeSession();

        Intent returnIntent = new Intent();
        setResult(RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onStop(){
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED,returnIntent);
        super.onStop();
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED,returnIntent);
        super.onBackPressed();
        finish();
    }

    private boolean IsValidEmail(String email) {
        CharSequence target = email;
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}