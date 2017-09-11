package com.kirov.detectionclient;

import android.app.Service;
import android.text.format.Time;

/**
 * Created by rzcc5 on 09-Sep-17.
 */

public class Utils {
    private static final String TAG = "Utils";
    private Service mService;

    private static Utils instance = null;

    private Utils(){
    }

    public static Utils getInstance(){
        if (instance == null){
            instance = new Utils();
        }
        return instance;
    }

    public void setService(Service service){
        mService = service;
    }

    //TODO TTS

    //TIME
    public static long currentTimeInMillis(){
        Time time = new Time();
        time.setToNow();
        return time.toMillis(false);
    }
}
