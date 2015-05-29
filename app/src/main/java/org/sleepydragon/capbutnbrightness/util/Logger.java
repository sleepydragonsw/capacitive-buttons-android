package org.sleepydragon.capbutnbrightness.util;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Formatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Logger {

    public static final String TAG = "CapacitiveButtons";

    private final String mSubTag;
    private final StringBuilder mStringBuilder;
    private final Formatter mFormatter;
    private final Lock mFormatterLock;

    public Logger(@NonNull final String subTag) {
        mSubTag = subTag;
        mStringBuilder = new StringBuilder();
        mFormatter = new Formatter(mStringBuilder);
        mFormatterLock = new ReentrantLock();
    }

    public Logger(@NonNull final Object subTag) {
        this(subTag.getClass().getSimpleName());
    }

    public void e(@NonNull final String message, Object... args) {
        final String formattedMessage = formatMessage(message, args);
        Log.e(TAG, formattedMessage);
    }

    public void w(@NonNull final String message, Object... args) {
        final String formattedMessage = formatMessage(message, args);
        Log.w(TAG, formattedMessage);
    }

    public void i(@NonNull final String message, Object... args) {
        final String formattedMessage = formatMessage(message, args);
        Log.i(TAG, formattedMessage);
    }

    public void d(@NonNull final String message, Object... args) {
        final String formattedMessage = formatMessage(message, args);
        Log.d(TAG, formattedMessage);
    }

    public void v(@NonNull final String message, Object... args) {
        final String formattedMessage = formatMessage(message, args);
        Log.v(TAG, formattedMessage);
    }

    @NonNull
    private String formatMessage(@NonNull final String message, Object... args) {
        final StringBuilder sb;
        final Formatter formatter;

        if (mFormatterLock.tryLock()) {
            sb = mStringBuilder;
            formatter = mFormatter;
        } else {
            sb = new StringBuilder();
            formatter = new Formatter(sb);
        }

        final String result;
        try {
            sb.append(mSubTag).append(": ");
            formatter.format(message, args);
            result = sb.toString();
            sb.setLength(0);
        } finally {
            if (formatter == mFormatter) {
                mFormatterLock.unlock();
            }
        }

        return result;
    }

}
