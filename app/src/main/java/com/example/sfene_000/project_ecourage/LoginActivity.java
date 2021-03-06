package com.example.sfene_000.project_ecourage;

import  android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sfene_000.project_ecourage.user.DBHandler;
import com.example.sfene_000.project_ecourage.user.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.*;

public class LoginActivity extends AppCompatActivity {

    DBHandler db;
    EditText usernameField;
    EditText passwordField;
    SessionManager session;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DBHandler(this);
        session = new SessionManager(this);

        usernameField = (EditText)findViewById(R.id.usernameField);
        passwordField = (EditText)findViewById(R.id.passwordField);
        passwordField.setTypeface(Typeface.DEFAULT);
        passwordField.setTransformationMethod(new PasswordTransformationMethod());

    }


    public void login(View view) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String passwordHash = encode(password);
        String[] params = new String[]{username.toLowerCase(), passwordHash};
        LogInUser signUpUser = new LogInUser(this);
        signUpUser.execute(params);

    }

    public void signUp(View view){
        Intent intent = new Intent(view.getContext(),SignUpActivity.class);
        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
    }

    private static String encode(String password) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            byte[] passwordBytes = password.getBytes();
            byte[] hashBytes = md.digest(passwordBytes);
            String hash = hexToString(hashBytes);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String hexToString(byte[] bytes) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            int val = bytes[i];
            val = val & 0xff; // remove higher bits, sign
            if (val < 16)
                buff.append('0'); // leading 0
            buff.append(Integer.toString(val, 16));
        }
        return buff.toString();
    }

    private class LogInUser extends AsyncTask<String, Void, String> {
        private Activity activity;
        private String username;
        public LogInUser(Activity activity){
            this.activity= activity;
        }

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            if(username.length()>0 && password.length()>0){
                String url = "http://ecourage.org/sql_query.php?nFunction=2&username="+username+"&password="+password;
                ConnectionManager connectionManager = new ConnectionManager(url);
                Log.d("password",url);
                String responseMessage = (connectionManager.getResponseMessage());
                Log.d("password",responseMessage);
                if(responseMessage.equals("log in successful")){

                    try {
                        user = new User(connectionManager.getJSONObj());
                        username = user.getUsername();
                        db.addUser(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    session.createLoginSession(username);

                    return "logged in";
                } else{
                    return "wrong password";
                }
            }
            return "empty field";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("empty field")){
                TextView error =(TextView) findViewById(R.id.error);
                error.setText("One or more fields is missing your information!");
            } else if(result.equals("wrong password")) {
                TextView error =(TextView) findViewById(R.id.error);
                error.setText("incorrect passcode");
            } else {
                Intent intent = new Intent(activity, MainActivity.class);
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putString("currentUser", username);
//                editor.commit();

                setResult(Activity.RESULT_OK, intent);
                startActivity(intent);
                finish();
            }
        }
    }

}
