package com.kirov.detectionclient;

import android.content.SharedPreferences;

/**
 * Created by rzcc5 on 09-Sep-17.
 */

public class PedometerSettings {
    SharedPreferences mSettings;

    public static int M_NONE = 1;
    public static int M_PACE = 2;
    public static int M_SPEED = 3;

    public PedometerSettings(SharedPreferences settings){
        mSettings = settings;
    }

    public boolean isMetric(){
        return mSettings.getString("units", "imperial").equals("metric");
    }

    public float getStepLength(){
        try {
            return Float.valueOf(mSettings.getString("step_length", "20").trim());
        }
        catch (NumberFormatException e){
            return 0f;
        }
    }

    public boolean isRunning(){
        return mSettings.getString("exercise_type", "running").equals("running");
    }

    public int getMaintainOption(){
        String p = mSettings.getString("maintain", "none");
        return
                p.equals("none") ? M_NONE:(
                        p.equals("pace") ? M_PACE:(
                                p.equals("speed") ? M_SPEED:(0)));
    }

    public int getDesiredPace() {
        return mSettings.getInt("desired_pace", 180); // steps/minute
    }
    public float getDesiredSpeed() {
        return mSettings.getFloat("desired_speed", 4f); // km/h or mph
    }
    public void savePaceOrSpeedSetting(int maintain, float desiredPaceOrSpeed) {
        SharedPreferences.Editor editor = mSettings.edit();
        if (maintain == M_PACE) {
            editor.putInt("desired_pace", (int)desiredPaceOrSpeed);
        }
        else
        if (maintain == M_SPEED) {
            editor.putFloat("desired_speed", desiredPaceOrSpeed);
        }
        editor.commit();
    }

    public boolean shouldTellSteps() {
        return mSettings.getBoolean("speak", false)
                && mSettings.getBoolean("tell_steps", false);
    }
    public boolean shouldTellPace() {
        return mSettings.getBoolean("speak", false)
                && mSettings.getBoolean("tell_pace", false);
    }

    //Internal

    public void saveServiceRunningWithTimestamp(boolean running) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("service_running", running);
        editor.putLong("last_seen", Utils.currentTimeInMillis());
        editor.commit();
    }

    public void saveServiceRunningWithNullTimestamp(boolean running) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("service_running", running);
        editor.putLong("last_seen", 0);
        editor.commit();
    }

    public void clearServiceRunning() {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("service_running", false);
        editor.putLong("last_seen", 0);
        editor.commit();
    }

    public boolean isServiceRunning() {
        return mSettings.getBoolean("service_running", false);
    }

    public boolean isNewStart() {
        // activity last paused more than 10 minutes ago
        return mSettings.getLong("last_seen", 0) < Utils.currentTimeInMillis() - 1000*60*10;
    }
}
