package org.sleepydragon.capbutnbrightness.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.sleepydragon.capbutnbrightness.R;
import org.sleepydragon.capbutnbrightness.util.Logger;

public class MainActivity extends AppCompatActivity {

    private final Logger mLogger;

    public MainActivity() {
        mLogger = new Logger(this);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        mLogger.d("onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.empty_container);

        if (savedInstanceState == null) {
            final Fragment fragment = new SelectBrightnessFragment();
            final FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.container, fragment, "MainFragment").commit();
        }
    }

    @Override
    protected void onDestroy() {
        mLogger.d("onDestroy()");
        super.onDestroy();
    }

}
