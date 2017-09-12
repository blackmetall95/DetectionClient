package com.kirov.detectionclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    /*========== Pedometer ==========*/
    private static final String TAG = "Pedometer";
    private SensorManager sensorManager;
    private SharedPreferences mSettings;
    private PedometerSettings pedometerSettings;
    private Utils mUtils;
    private Sensor accelerometer;
    private Display mDisplay;
    private TextView stepValueTxt;
    public int stepValue;
    private boolean mIsRunning;
    private boolean mQuitting = false;
    /*================================*/
    /*========== Client ==========*/
    private TextView response;
    private EditText addressText;
    private Button buttonConnect, buttonClear;
    private int port = 8080;
    private String address;
    private String stepValueStr;
    private int portNum;
    /*============================*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepValue = 0;

        mUtils = Utils.getInstance();

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mDisplay = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        /*========== Client ==========*/
        addressText = (EditText) findViewById(R.id.editView);
        buttonConnect = (Button) findViewById(R.id.button1);
        buttonClear = (Button) findViewById(R.id.button2);
        response = (TextView) findViewById(R.id.textView1);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Client mClient = new Client(addressText.getText().toString(),
                //        port, response);
                //mClient.execute();
                Client2 mClient2 = new Client2(addressText.getText().toString(), port, response);
                mClient2.execute();
            }
        });
        buttonClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                response.setText("");
            }
        });
        /*============================*/
    }

    protected void onStart(){
        Log.d(TAG, "[ACTIVITY] onStart");
        super.onStart();
    }

    protected void onResume(){
        super.onResume();
        Log.d(TAG, "[ACTIVITY] onResume");
        //sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        pedometerSettings = new PedometerSettings(mSettings);

        //Read from preferences if the service was running on the last onPause
        mIsRunning = pedometerSettings.isServiceRunning();
        //Start teh service if this is considered to be an application start
        if (!mIsRunning && pedometerSettings.isNewStart()){
            startStepService();
            bindStepService();
        }
        else if (mIsRunning){
            bindStepService();
        }
        pedometerSettings.clearServiceRunning();

        stepValueTxt = (TextView) findViewById(R.id.stepTxt);
    }

    @Override
    protected void onPause(){
        Log.d(TAG, "[ACTIVITY] onPause");
        super.onPause();
        //sensorManager.unregisterListener(this);

        if(mIsRunning){
            unbindStepService();
        }
        if(mQuitting){
            pedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
        }
        else {
            pedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
        }
    }

    @Override
    protected void onStop(){
        Log.d(TAG, "[ACTIVITY] onStop");
        stopStepService();
        super.onStop();
    }

    protected void onDestroy(){
        Log.d(TAG, "[ACTIVITY] onDestroy");
        stopStepService();
        super.onDestroy();
    }

    /*=============== Client Class ===============*/
    private class Client2 extends AsyncTask<Void, Void, String>{
        boolean isConnected = false;

        Client2(String add, int port, TextView responseText){
            address = add;
            portNum = port;
            //this.responseText = responseText;
            //mContext.getApplicationContext();
        }
        @Override
        protected String doInBackground(Void... params) {
            Socket socket = null;
            try{
                Log.d("[ASYNCTASK]:", "Connecting...");
                socket = new Socket(address, portNum);
                Log.d("[ASYNCTASK]:", "Connected!");
                //isConnected = true;
                if (stepValueStr!=null){
                    try{
                        Log.d("[OUTPUT]:", "Sending...");
                        DataOutputStream DOS = new DataOutputStream(socket.getOutputStream());
                        DOS.writeUTF(stepValueStr);
                        Log.d("[OUTPUT]:", stepValueStr);
                        socket.close();
                    } catch(IOException e){
                        Log.d("[OUTPUT]:", "Error Sending Data");
                        e.printStackTrace();
                        socket.close();
                    }
                }
            } catch(IOException e){
                e.printStackTrace();
            }
            return null;
        }

    }
    /*============================================*/

    private StepService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((StepService.StepBinder)service).getService();

            mService.registerCallback(mCallback);
            //mService.reloadSettings();

        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    private void startStepService() {
        if (! mIsRunning) {
            Log.d(TAG, "[SERVICE] Start");
            mIsRunning = true;
            startService(new Intent(MainActivity.this,
                    StepService.class));
        }
    }

    private void bindStepService(){
        Log.d(TAG, "[SERVICE] Bind");
        bindService(new Intent(MainActivity.this,
                StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindStepService(){
        unbindService(mConnection);
    }

    private void stopStepService(){
        Log.d(TAG, "[SERVICE] Stop");
        if (mService != null){
            stopService(new Intent(MainActivity.this, StepService.class));
        }
        mIsRunning = false;
    }

    private void resetValues(boolean updateDisplay){
        if (mService != null && mIsRunning){
            mService.resetValues();
        }
        else {
            stepValueTxt.setText("0");
            SharedPreferences state = getSharedPreferences("state", 0);
            SharedPreferences.Editor stateEditor = state.edit();
            if (updateDisplay) {
                stateEditor.putInt("steps", 0);
            }
        }
    }

    /*========== Step Service ==========*/
    private StepService.ICallback mCallback = new StepService.ICallback() {
        public void stepsChanged(int value) {
            handler.sendMessage(handler.obtainMessage(STEPS_MSG, value, 0));
        }
    };

    private static final int STEPS_MSG = 1;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case STEPS_MSG:
                    stepValue = msg.arg1;
                    stepValueTxt.setText("Steps: " + stepValue);
                    stepValueStr = stepValueTxt.getText().toString();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
}
