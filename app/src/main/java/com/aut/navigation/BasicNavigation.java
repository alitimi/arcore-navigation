package com.aut.navigation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.TouchEventSystem;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BasicNavigation extends AppCompatActivity implements SensorEventListener, StepListener {

    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private int numSteps;
    private ArFragment fragment;

    String source;
    String destination;
    String src_dst;
    ArrayList<String> myPath = new ArrayList();
    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;

    //    private TextView mSrcMessage, mDestMessage, mNavMsg, mNumStepsMsg;
//    Button mStartNav, mStopNav;
//    ListView mInstructionListView;
//    int mDestNum = 0, mSrcNum = 0;
//    int mDestGroup = 0, mSrcGroup = 0;
//    int mDir = 0;
//    int mStepsG1[] = {25, 15, 24, 14}, mStepsG2[] = {7, 25, 4, 24, 3, 20}, mStepsCross = 7;
//    int mAryPtrSrc, mAryPtrDest;

    private int mListenerRegistered = 0;

    private final float[] mLastAccelerometer = new float[3];
    private final float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    float[] rMat = new float[9];
    float[] orientation = new float[3];
    int mAbsoluteDir;
    boolean goNext = false;
    private int mInstructionNum;
    int index = 0;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_detection);

        mInstructionNum = 0;

        //Source and Destination String
        Bundle b = getIntent().getExtras();
        source = b.getString("source");
        destination = b.getString("destination");
        src_dst = source + '_' + destination;

        //Reading the Related Path from Directions Json
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray dirs = obj.getJSONArray("dirs");
            JSONArray path = new JSONObject(dirs.get(0).toString()).getJSONArray("Entrance_Site");
            for (int i = 0; i < path.length(); i++) {
                String dir = String.valueOf(path.getJSONObject(i).toString().charAt(2));
                Integer steps = Integer.valueOf(new JSONObject((path.getJSONObject(i).toString())).get(dir).toString());
                myPath.add(dir + ' ' + String.valueOf(steps));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.cam_fragment);
        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);
            onUpdate();
        });
        startNavigation();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startNavigation() {

        //Sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        numSteps = 0;
        sensorManager.registerListener(BasicNavigation.this, accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(BasicNavigation.this, magnetometer,
                SensorManager.SENSOR_DELAY_UI);
        mListenerRegistered = 1;


    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccelerometer(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccelerometer(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAbsoluteDir = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            mAbsoluteDir = getRange(Math.round(mAbsoluteDir));
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void step(long timeNs) {
        Snackbar snackbar;
        int limNumSteps = -1;
        if (numSteps == limNumSteps) {
            mListenerRegistered = 0;
            sensorManager.unregisterListener(BasicNavigation.this);
            numSteps = 0;
        }
        numSteps++;
        if (index < myPath.size()) {
            String[] strings = myPath.get(index).split(" ");
            Integer key = Integer.valueOf(strings[0]);
            Integer value = Integer.valueOf(strings[1]);
            if (mAbsoluteDir == key) {
                snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Correct Direction " + key, Snackbar.LENGTH_SHORT);
                snackbar.show();
                if (value == 0) {
                    index++;
                    Toast.makeText(BasicNavigation.this, "Next direction", Toast.LENGTH_SHORT).show();
                }
                if (numSteps == value) {
                    Toast.makeText(BasicNavigation.this, "You walked " + numSteps, Toast.LENGTH_SHORT).show();
                    numSteps = 0;
                    index++;
                }
            }


        } else if (index == myPath.size()) {
            Toast.makeText(BasicNavigation.this, "You've reached your destination", Toast.LENGTH_SHORT).show();
        }


    }


    public int getRange(int degree) {
        int mRangeVal = 0;
        if (degree > 335 || degree < 25)
            mRangeVal = 1;    //N
        else if (degree > 65 && degree < 115)
            mRangeVal = 2;    //E
        else if (degree > 155 && degree < 205)
            mRangeVal = 3;    //S
        else if (degree > 245 && degree < 295)
            mRangeVal = 4;    //W
        return mRangeVal;
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("directions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    //-----------------------------AR Object placement----------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addObject(Uri model) {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    placeObject(fragment, hit.createAnchor(), model);
                    break;
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), model)
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Codelab error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);

        //Handling Rotaional orientation using Quaternion
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 90f));

        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    //---------------------------AR green dot center detection methods------------------------------

    private void onUpdate() {
        boolean trackingChanged = updateTracking();
        View contentView = findViewById(android.R.id.content);
        if (trackingChanged) {
            if (isTracking) {
                contentView.getOverlay().add(pointer);
            } else {
                contentView.getOverlay().remove(pointer);
            }
            contentView.invalidate();
        }

        if (isTracking) {
            boolean hitTestChanged = updateHitTest();
            if (hitTestChanged) {
                pointer.setEnabled(isHitting);
                contentView.invalidate();
            }
        }
    }

    private boolean updateTracking() {
        Frame frame = fragment.getArSceneView().getArFrame();
        boolean wasTracking = isTracking;
        isTracking = frame != null &&
                frame.getCamera().getTrackingState() == TrackingState.TRACKING;
        return isTracking != wasTracking;
    }

    private boolean updateHitTest() {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        boolean wasHitting = isHitting;
        isHitting = false;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    isHitting = true;
                    break;
                }
            }
        }
        return wasHitting != isHitting;
    }

    private android.graphics.Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth() / 2, vw.getHeight() / 2);
    }

    //----------------------------------------------------------------------------------------------
}