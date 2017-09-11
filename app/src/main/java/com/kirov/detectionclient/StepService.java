package com.kirov.detectionclient;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by rzcc5 on 09-Sep-17.
 */

public class StepService extends Service {
    private SharedPreferences mSettings;
    private NotificationManager mNM;
    private StepDisplay stepDisplay;
    private StepDetector stepDetector;
    private SensorManager sensorManager;
    private SharedPreferences.Editor stateEditor;
    private Sensor mSensor;
    private SharedPreferences mState;
    private PedometerSettings pedometerSettings;
    private Utils mUtils;

    private int mSteps;

    public class StepBinder extends Binder {
        StepService getService() {
            return StepService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        super.onCreate();

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        //Load Settings
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        pedometerSettings = new PedometerSettings(mSettings);
        mState = getSharedPreferences("state", 0);

        mUtils = Utils.getInstance();
        mUtils.setService(this);
        //mUtils.initTTS();

        //Start Detecting
        stepDetector = new StepDetector();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerDetector();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);

        stepDisplay = new StepDisplay(pedometerSettings, mUtils);
        stepDisplay.setSteps(mSteps = mState.getInt("Steps", 0));
        stepDisplay.addListener(stepListener);
        stepDetector.addStepListener(stepDisplay);

        reloadSettings();
    }

    @Override
    public void onStart(Intent intent, int startId){
        Log.i(TAG, "SERVICE onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "SERVICE onDestroy");
        //mUtils.shutdownTTS();

        //Unregister receiver
        unregisterReceiver(mReceiver);
        unregisterDetector();

        stateEditor = mState.edit();
        stateEditor.putInt("steps", mSteps);
        stateEditor.apply();

        mNM.cancel(R.string.app_name);

        super.onDestroy();

        //Stop detecting
        sensorManager.unregisterListener(stepDetector);

        //Notify the user
        Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
    }

    private void registerDetector(){
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(stepDetector, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterDetector(){
        sensorManager.unregisterListener(stepDetector);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "SERVICE onBind");
        return mBinder;
    }

    //Receive messages from activity
    private final IBinder mBinder = new StepBinder();

    public interface ICallback{
        void stepsChanged(int value);
    }

    private ICallback mCallback;

    public void registerCallback(ICallback cb){
        mCallback = cb;
    }

    public void reloadSettings() {
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        if (stepDetector != null) {
            stepDetector.setSensitivity(
                    Float.valueOf(mSettings.getString("sensitivity", "10"))
            );
        }

        if (stepDisplay != null) stepDisplay.reloadSettings();
    }

    public void resetValues() {
        stepDisplay.setSteps(0);
    }

    //Forwards pace values from StepDisplay to the activity
    private StepDisplay.Listener stepListener = new StepDisplay.Listener() {
        @Override
        public void stepsChanged(int value) {
            mSteps = value;
            passValue();
        }

        @Override
        public void passValue() {
            if (mCallback != null) {
                mCallback.stepsChanged(mSteps);
            }
        }
    };

    private void showNotification(){

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Check action to be safe
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                //Unregister the listener and re-register it again
                StepService.this.unregisterDetector();
                StepService.this.registerDetector();
                /*if (mPedometerSettings.wakeAggressively()){
                    wakelock.release();
                    acquireWakeLock();
                }*/
            }
        }
    };
}
