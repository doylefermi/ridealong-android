package com.ridealong.haxorware.trackmyrider;

/**
 * Created by haxorware on 23/1/16.
 */
        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.SharedPreferences;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;

        import android.content.Intent;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import butterknife.ButterKnife;
        import butterknife.Bind;

        import org.apache.http.HttpResponse;
        import org.apache.http.NameValuePair;
        import org.apache.http.client.ClientProtocolException;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.entity.UrlEncodedFormEntity;
        import org.apache.http.client.methods.HttpPost;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.message.BasicNameValuePair;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.OutputStream;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Iterator;
        import java.util.List;
        import java.util.Map;
        import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static int loginstat = 0;
    public static String msg="";
    public static String uname="";
    public static String pass="";
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.link_signup) TextView _signupLink;
    SharedPreferences sharedpreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uname=email;
        pass=password;
        registerInBackground(email,password);

        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        Log.e("status", ""+ loginstat);
                        if (loginstat == 1) {

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("username", uname);
                            Log.e("commiting username", "" + uname);
                            editor.commit();
                            onLoginSuccess();
                        }
                        else onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here

                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private static void registerInBackground(String email, String pwd) {

        new AsyncTask<Void,Void,String>() {

            @Override
            protected String doInBackground(Void... params) {
                    sendRegistrationIdToBackend();
                return msg;
            }

            private void sendRegistrationIdToBackend() {
                final int MAX_ATTEMPTS = 5;
                final int BACKOFF_MILLI_SECONDS = 2000;
                final Random random = new Random();

                String serverUrl = "http://doylefermi.site88.net/login.php";
                Map<String, String> params = new HashMap<String, String>();

                params.put("username", uname);
                params.put("password", pass);


                long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

                for (int i = 1; i <= MAX_ATTEMPTS; i++) {
                    Log.d(TAG, "Attempt #" + i + " to register");
                    try {
                        post(serverUrl, params);

                        return;
                    } catch (IOException e) {

                        Log.e(TAG, "Failed to register on attempt " + i + ":" + e);
                        if (i == MAX_ATTEMPTS) {
                            break;
                        }
                        try {
                            Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                            Thread.sleep(backoff);
                        } catch (InterruptedException e1) {

                            Log.d(TAG, "Thread interrupted: abort remaining retries!");
                            Thread.currentThread().interrupt();
                            return;
                        }

                        backoff *= 2;
                    }
                }

            }
            private  void post(String endpoint, Map<String, String> params)throws IOException{
                URL url;
                try {
                    url = new URL(endpoint);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("invalid url: " + endpoint);
                }
                StringBuilder bodyBuilder = new StringBuilder();
                Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<String, String> param = iterator.next();
                    bodyBuilder.append(param.getKey()).append('=')
                            .append(param.getValue());
                    if (iterator.hasNext()) {
                        bodyBuilder.append('&');
                    }
                }
                String body = bodyBuilder.toString();
                Log.v(TAG, "Posting '" + body+ "' to " + url);
                byte[] bytes = body.getBytes();
                HttpURLConnection conn = null;
                try {
                    Log.e("URL", "> " + url);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setFixedLengthStreamingMode(bytes.length);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded;charset=UTF-8");

                    OutputStream out = conn.getOutputStream();
                    out.write(bytes);
                    out.close();

                    int status = conn.getResponseCode();
                    if (status == 202) {
                        loginstat=1;
                    }
                    else{ loginstat=0; Log.e("HTTP status", ""+ status);}
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }

            protected void onPostExecute(String msg) {

            }


        }.execute(null, null, null);}
}
