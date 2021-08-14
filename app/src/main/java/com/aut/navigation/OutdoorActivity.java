package com.aut.navigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class OutdoorActivity extends AppCompatActivity {

    boolean clicked;
    Handler handler;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_detection);
        if (isNetworkConnected()) {
            if (isGPSEnabled(OutdoorActivity.this)) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
                handler = new Handler();

                handler.post(new Runnable() {
                    public void run() {
                        if (!clicked) {
                            Intent intent = new Intent(OutdoorActivity.this, HelloService.class);
                            startService(intent);
                            GPSTracker gpsTracker = new GPSTracker(OutdoorActivity.this);
                            if(gpsTracker.canGetLocation()) {
                                double latitude = gpsTracker.getLatitude();
                                double longitude = gpsTracker.getLongitude();
                                Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                            } else {
                                gpsTracker.showSettingsAlert();
                            }
                            handler.postDelayed(this, 5000); //now is every 3 minutes
                        }
                    }
                });
            } else {
                String text = "لطفا دسترسی موقعیت مکانی خود را بررسی کنید.";
                Spannable centeredText = new SpannableString(text);
                centeredText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                        0, text.length() - 1,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                Toast.makeText(OutdoorActivity.this, centeredText, Toast.LENGTH_SHORT).show();
            }
        } else {
            String text = "لطفا اتصال اینترنت خود را بررسی کنید.";
            Spannable centeredText = new SpannableString(text);
            centeredText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    0, text.length() - 1,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            Toast.makeText(OutdoorActivity.this, centeredText, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public boolean isGPSEnabled(Context mContext) {
        LocationManager lm = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
