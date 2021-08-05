package com.aut.navigation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.ar.sceneform.ux.ArFragment;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.List;

public class SourceDetection extends AppCompatActivity {

    Button mCapture,mDetect,mGallery;

    ArFragment fragment;
    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;

    private int mSourceDetectedFlag = 0, mCapturedFlag = 0, mGallerySelectFlag = 0;
    String place;
    private Bitmap mBitmap;
    Uri uri;
    String picpath = "",mSrc,mDest;
    List<String> mNavInstructions;
    static int mProceedFlag=0;
    private SensorManager sensorManager;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 7;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_detection);

        //Camera Permissions
        if (ContextCompat.checkSelfPermission(SourceDetection.this,
                android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SourceDetection.this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        }

        mCapture = (Button) findViewById(R.id.capturebtnid);
        mDetect = (Button) findViewById(R.id.detectbtnid);
//        mGallery = (Button) findViewById(R.id.selectbtnid);

        mCapture.setOnClickListener(view -> {
            IntentIntegrator integrator = new IntentIntegrator(SourceDetection.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Scan");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();

        });

        mDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SourceDetection.this, BasicNavigation.class);
                startActivity(intent);
            }
        });

        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.cam_fragment);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.e("Scan*******", "Cancelled scan");
            } else {
                Log.e("Scan", "Scanned");
                place = result.getContents();
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
