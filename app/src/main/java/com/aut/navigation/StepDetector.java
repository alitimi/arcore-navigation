package com.aut.navigation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StepDetector implements SensorEventListener {

//    private static final int ACCELEROMETER_RING_SIZE = 50;
//    private static final int VEL_RING_SIZE = 10;
//
//    // change this threshold according to your sensitivity preferences
//    private static final float STEP_THRESHOLD = 50f;
//
//    private static final int STEP_DELAY_NS = 250000000;
//
//    private int accelerometerRingCounter = 0;
//    private float[] accelerometerRingX = new float[ACCELEROMETER_RING_SIZE];
//    private float[] accelerometerRingY = new float[ACCELEROMETER_RING_SIZE];
//    private float[] accelerometerRingZ = new float[ACCELEROMETER_RING_SIZE];
//    private int velRingCounter = 0;
//    private float[] velRing = new float[VEL_RING_SIZE];
//    private long lastStepTimeNs = 0;
//    private float oldVelocityEstimate = 0;
//
//    private StepListener listener;
//
//    void registerListener(StepListener listener) {
//        this.listener = listener;
//    }
//
//
//    void updateAccelerometer(long timeNs, float x, float y, float z) {
//        float[] currentAccelerometer = new float[3];
//        currentAccelerometer[0] = x;
//        currentAccelerometer[1] = y;
//        currentAccelerometer[2] = z;
//
//        // First step is to update our guess of where the global z vector is.
//        accelerometerRingCounter++;
//        accelerometerRingX[accelerometerRingCounter % ACCELEROMETER_RING_SIZE] = currentAccelerometer[0];
//        accelerometerRingY[accelerometerRingCounter % ACCELEROMETER_RING_SIZE] = currentAccelerometer[1];
//        accelerometerRingZ[accelerometerRingCounter % ACCELEROMETER_RING_SIZE] = currentAccelerometer[2];
//
//        float[] worldZ = new float[3];
//        worldZ[0] = SensorFilter.sum(accelerometerRingX) / Math.min(accelerometerRingCounter, ACCELEROMETER_RING_SIZE);
//        worldZ[1] = SensorFilter.sum(accelerometerRingY) / Math.min(accelerometerRingCounter, ACCELEROMETER_RING_SIZE);
//        worldZ[2] = SensorFilter.sum(accelerometerRingZ) / Math.min(accelerometerRingCounter, ACCELEROMETER_RING_SIZE);
//
//        float normalization_factor = SensorFilter.norm(worldZ);
//
//        worldZ[0] = worldZ[0] / normalization_factor;
//        worldZ[1] = worldZ[1] / normalization_factor;
//        worldZ[2] = worldZ[2] / normalization_factor;
//
//        float currentZ = SensorFilter.dot(worldZ, currentAccelerometer) - normalization_factor;
//        velRingCounter++;
//        velRing[velRingCounter % VEL_RING_SIZE] = currentZ;
//
//        float velocityEstimate = SensorFilter.sum(velRing);
//
//        if (velocityEstimate > STEP_THRESHOLD && oldVelocityEstimate <= STEP_THRESHOLD
//                && (timeNs - lastStepTimeNs > STEP_DELAY_NS)) {
//            listener.step(timeNs);
//            lastStepTimeNs = timeNs;
//        }
//        oldVelocityEstimate = velocityEstimate;
//    }

    private final static String TAG = "StepDetector";
    private float mLimit = 10;
    private float mLastValues[] = new float[3 * 2];
    private float mScale[] = new float[2];
    private float mYOffset;

    private float mLastDirections[] = new float[3 * 2];
    private float mLastExtremes[][] = {new float[3 * 2], new float[3 * 2]};
    private float mLastDiff[] = new float[3 * 2];
    private int mLastMatch = -1;

    private ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();

    public StepDetector() {
        int h = 480; // TODO: remove this constant
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }

    public void setSensitivity(float sensitivity) {
        mLimit = sensitivity; // 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62
    }

    public void addStepListener(StepListener sl) {
        mStepListeners.add(sl);
    }

    //public void onSensorChanged(int sensor, float[] values) {
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            } else {
                int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                if (j == 1) {
                    float vSum = 0;
                    for (int i = 0; i < 3; i++) {
                        final float v = mYOffset + event.values[i] * mScale[j];
                        vSum += v;
                    }
                    int k = 0;
                    float v = vSum / 3;

                    float direction = (Float.compare(v, mLastValues[k]));
                    if (direction == -mLastDirections[k]) {
                        // Direction changed
                        int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                        mLastExtremes[extType][k] = mLastValues[k];
                        float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                        if (diff > mLimit) {

                            boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                            boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                            boolean isNotContra = (mLastMatch != 1 - extType);

                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                Log.i(TAG, "step");
                                for (StepListener stepListener : mStepListeners) {
                                    stepListener.onStep();
                                }
                                mLastMatch = extType;
                            } else {
                                mLastMatch = -1;
                            }
                        }
                        mLastDiff[k] = diff;
                    }
                    mLastDirections[k] = direction;
                    mLastValues[k] = v;
                }
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}