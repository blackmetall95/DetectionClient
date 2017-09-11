package com.kirov.detectionclient;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by rzcc5 on 09-Sep-17.
 */

public class Settings extends PreferenceActivity{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
