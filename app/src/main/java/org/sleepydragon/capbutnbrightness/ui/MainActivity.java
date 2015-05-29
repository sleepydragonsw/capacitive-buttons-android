package org.sleepydragon.capbutnbrightness.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.sleepydragon.capbutnbrightness.util.Logger;

public class MainActivity extends FragmentActivity {

    private final Logger mLogger;

    public MainActivity() {
        mLogger = new Logger(this);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        mLogger.d("onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        mLogger.d("onDestroy()");
        super.onDestroy();
    }

}
