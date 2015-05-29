package org.sleepydragon.capbutnbrightness;

import android.app.Application;
import android.os.Handler;
import android.os.StrictMode;

import org.sleepydragon.capbutnbrightness.util.Logger;

public class MyApplication extends Application {

    private final Logger mLogger;

    public MyApplication() {
        mLogger = new Logger("MyApplication");
    }

    @Override
    public void onCreate() {
        mLogger.d("onCreate()");
        super.onCreate();
        setStrictModePolicies();
    }

    private void setStrictModePolicies() {
        // only set the StrictMode policies on debug builds
        if (!BuildConfig.DEBUG) {
            return;
        }

        // there is a "bug" where any StrictMode policy changes made in Application.onCreate()
        // are discarded when onCreate() returns; posting the Runnable fixes this;
        // see https://code.google.com/p/android/issues/detail?id=35298 for details
        final Runnable runnable = new SetStrictModePoliciesRunnable();
        runnable.run();
        new Handler().postAtFrontOfQueue(runnable);
    }

    /**
     * A Runnable whose run() method sets the StrictMode thread and VM policies.
     */
    private static class SetStrictModePoliciesRunnable implements Runnable {

        @Override
        public void run() {
            setThreadPolicy();
            setVmPolicy();
        }

        private void setThreadPolicy() {
            final StrictMode.ThreadPolicy.Builder builder = new StrictMode.ThreadPolicy.Builder();
            builder.detectAll();
            builder.penaltyLog();
            builder.penaltyDeath();
            final StrictMode.ThreadPolicy policy = builder.build();
            StrictMode.setThreadPolicy(policy);
        }

        private void setVmPolicy() {
            final StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            builder.detectAll();
            builder.penaltyLog();
            builder.penaltyDeath();
            final StrictMode.VmPolicy policy = builder.build();
            StrictMode.setVmPolicy(policy);
        }

    }

}
