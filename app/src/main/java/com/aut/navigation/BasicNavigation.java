package com.aut.navigation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.sceneform.ux.ArFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class BasicNavigation extends AppCompatActivity implements SensorEventListener, StepListener {

    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int numSteps = 0, limNumSteps = -1;
    ArFragment fragment;


    private TextView mSrcMessage, mDestMessage, mNavMsg, mNumStepsMsg;
    private int mListenerRegistered = 0;
    Button mStartNav, mStopNav;
    ListView mInstructionListView;
    int mDestNum = 0, mSrcNum = 0;
    int mDestGroup = 0, mSrcGroup = 0;
    int mDir = 0;
    int mStepsG1[] = {25, 15, 24, 14}, mStepsG2[] = {7, 25, 4, 24, 3, 20}, mStepsCross = 7;
    int mAryPtrSrc, mAryPtrDest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_detection);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        numSteps = 0;
        sensorManager.registerListener(BasicNavigation.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mListenerRegistered = 1;
        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.cam_fragment);


        //initialise a new string array
        String[] mEachInstruction = new String[]{};
        // Create a List from String Array elements
        final List<String> mAllInstructionList = new ArrayList<String>(Arrays.asList(mEachInstruction));
        // Create an ArrayAdapter from List
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, mAllInstructionList);
        // DataBind ListView with items from ArrayAdapter
//        mInstructionListView.setAdapter(arrayAdapter);
        // Add new Items to List
        //mAllInstructionList.add("inst1");
        //mAllInstructionList.add("inst2");
                /*
                    notifyDataSetChanged ()
                        Notifies the attached observers that the underlying
                        data has been changed and any View reflecting the
                        data set should refresh itself.
                 */
        //arrayAdapter.notifyDataSetChanged();


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
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAbsoluteDir = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            mAbsoluteDir = Math.round(mAbsoluteDir);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        runOnUiThread(() -> Toast.makeText(BasicNavigation.this, String.valueOf(numSteps), Toast.LENGTH_SHORT).show());
//        mNumStepsMsg.setText("Steps : " + numSteps);
        if (numSteps == limNumSteps) {
            mListenerRegistered = 0;
            sensorManager.unregisterListener(BasicNavigation.this);
            numSteps = 0;
        }

    }
}