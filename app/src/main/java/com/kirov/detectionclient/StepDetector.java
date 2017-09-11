package com.kirov.detectionclient;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by rzcc5 on 09-Sep-17.
 */

public class StepDetector implements SensorEventListener {
    private final static String TAG ="StepDetector";
    private float mLimit = 10;
    private float lastValues[] = new float[3*2];
    private float mScale[] = new float[3*2];
    private float yOffset;

    private float lastDirections[] = new float[3*2];
    private float lastExtremes[][] = {new float[3*2], new float[3*2]};
    private float lastDiff[] = new float[3*2];
    private int lastMatch = -1;

    private ArrayList<StepListener> mStepListeners = new ArrayList<>();

    public StepDetector(){
        int h = 480;
        yOffset = h*0.5f;
        mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }

    public void setSensitivity(float sensitivity) {
        mLimit = sensitivity;
    }

    public void addStepListener(StepListener sListener) {
        mStepListeners.add(sListener);
    }

    //@Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            }
            else {
                int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                if (j == 1) {
                    float vSum = 0;
                    for (int i=0; i<3; i++){
                        final float v = yOffset + event.values[1]*mScale[j];
                        vSum += v;
                    }
                    int k = 0;
                    float v = vSum / 3;
                    float direction = (v>lastValues[k] ? 1 : (v<lastValues[k] ? -1 : 0));
                    if (direction == - lastDirections[k]){
                        //Direction Changed
                        int extType = (direction > 0 ? 0 : 1); //minimum or maximum
                        lastExtremes[extType][k] = lastValues[k];
                        float diff = Math.abs(lastExtremes[extType][k] - lastExtremes[1-extType][k]);

                        if (diff > mLimit) {
                            boolean isAlmostAsLargeAsPrevious = diff > (lastDiff[k]*2/3);
                            boolean isPreviousLargeEnough = lastDiff[k] > (diff/3);
                            boolean isNotContra = (lastMatch != 1 - extType);

                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra){
                                //Log.d(TAG, "step");
                                for (StepListener stepListener : mStepListeners){
                                    stepListener.onStep();
                                }
                                lastMatch = extType;
                            }
                            else {
                                lastMatch = -1;
                            }
                        }
                        lastDiff[k] = diff;
                    }
                    lastDirections[k] = direction;
                    lastValues[k] = v;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
