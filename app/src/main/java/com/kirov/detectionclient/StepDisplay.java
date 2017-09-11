package com.kirov.detectionclient;

import java.util.ArrayList;

/**
 * Created by rzcc5 on 09-Sep-17.
 */

public class StepDisplay implements StepListener{
    private int count = 0;
    PedometerSettings mSettings;
    Utils mUtils;

    public StepDisplay(PedometerSettings settings, Utils utils){
        mUtils = utils;
        mSettings = settings;
        notifyListener();
    }

    public void setSteps(int steps){
        count = steps;
        notifyListener();
    }

    public void reloadSettings(){
        notifyListener();
    }

    @Override
    public void onStep() {
        count++;
        notifyListener();
    }

    @Override
    public void passValue() {
    }

    //Listener
    public interface Listener {
        void stepsChanged(int value);
        void passValue();
    }

    private ArrayList<Listener> listeners = new ArrayList<>();

    public void addListener(Listener listener){
        listeners.add(listener);
    }
    public void notifyListener(){
        for (Listener listener : listeners){
            listener.stepsChanged(count);
        }
    }
}
