package org.sleepydragon.capbutnbrightness.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sleepydragon.capbutnbrightness.R;
import org.sleepydragon.capbutnbrightness.util.Logger;

public class SelectBrightnessFragment extends Fragment {

    private final Logger mLogger;

    public SelectBrightnessFragment() {
        mLogger = new Logger(this);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        mLogger.d("onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        mLogger.d("onDestroy()");
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        mLogger.d("onCreateView() container=%s savedInstanceState=%s", container, savedInstanceState);
        return inflater.inflate(R.layout.select_brightness_fragment, container, false);
    }

}
