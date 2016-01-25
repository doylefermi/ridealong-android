package com.ridealong.haxorware.trackmyrider;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;



public class MainActivity extends ActionBarActivity implements LocationListener {
    private LocationManager locationManager;
    private static final String TAG = "MainActivity";
    public static String msg="";
    public static String uname="";
    public static String userprefs="";
    public static double lat=0;
    public static double lon=0;
    SharedPreferences shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shared = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

         userprefs = (shared.getString("username", ""));
        uname=userprefs;
        Log.e("Retrieval 1st userprefs", ""+ uname);

        if(uname=="") {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

        }
        else{Toast.makeText(getBaseContext(), "Signed in as "+userprefs, Toast.LENGTH_LONG).show();}



        Log.e("Retrieval 2nd userprefs", ""+ uname);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            //Toast.makeText(getBaseContext(), "No permissions", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        shared = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userprefs = (shared.getString("username", ""));
        uname=userprefs;

        TextView textView = (TextView) findViewById(R.id.textView);
        lat=location.getLatitude();
        lon=location.getLongitude();
        String msg = "Latitude: " + lat + "\nLongitude: " + lon;
        textView.setText(msg);

        updatelocation(uname,location.getLatitude(),location.getLongitude());
    }
    @Override
    public void onProviderDisabled(String provider) {

        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onProviderEnabled(String provider) {

        Toast.makeText(getBaseContext(), "Gps is turned on!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static void updatelocation(String email, final double lat, final double lon) {

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

                String serverUrl = "http://doylefermi.site88.net/locationpost.php";
                Map<String, String> params = new HashMap<String, String>();

                params.put("username", uname);
                params.put("latitude", lat+"");
                params.put("longitude", lon+"");


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
                    String line;
                    String jsonString;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();


                    while ((line = bufferedReader.readLine()) != null)
                    {
                        stringBuilder.append(line + '\n');
                    }

                    jsonString = stringBuilder.toString();
                    int status = conn.getResponseCode();
                    if (status == 200) {
                        Log.e("Updating database", ""+ jsonString);
                    }
                    else{  Log.e("HTTP status", ""+ status);}
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
