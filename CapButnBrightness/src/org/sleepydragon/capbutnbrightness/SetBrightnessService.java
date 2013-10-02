/*
 * This file is part of Capacitive Buttons.
 *
 * Capacitive Buttons is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Capacitive Buttons is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Capacitive Buttons.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sleepydragon.capbutnbrightness;

import org.sleepydragon.capbutnbrightness.devices.CapacitiveButtonsBacklightBrightness;
import org.sleepydragon.capbutnbrightness.devices.CapacitiveButtonsBacklightBrightness.DimBrightnessNotSupportedException;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfo;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfoDatabase;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * A service that performs the actual operation of setting the capacitive
 * buttons brightness. This is implemented as a service due to its usage of
 * blocking IO calls and requests for superuser privileges, all of which will
 * block the UI if invoked on the main thread.
 * <p>
 * The intents used to launch this service have some optional and some required
 * attributes.
 * <p>
 * The <em>action</em> of the Intent must be one of the <code>ACTION_</code>
 * constants defined in this class.
 * <p>
 * <h3>The <code>ACTION_SET_BRIGHTNESS</code> Action</h3> The
 * {@link #ACTION_SET_BRIGHTNESS} action indicates that the service should set
 * the brightness of the capacitive buttons backlight. An extra with the name
 * {@link #EXTRA_NAME_LEVEL} must be set to the brightness level to set. A
 * {@link Messenger} may be specified in the {@link #EXTRA_NAME_MESSENGER}
 * attribute if the caller is interested in the result of the operation.
 */
public class SetBrightnessService extends IntentService {

    /**
     * The action to be used in intents to indicate that the service should set
     * the brightness of the capacitive buttons backlight.
     */
    public static final String ACTION_SET_BRIGHTNESS = "ACTION_SET_BRIGHTNESS";

    /**
     * The name of a string extra on the intent that specifies the brightness
     * level to set. The value must be the name of one of the constants defined
     * in {@link Level}.
     */
    public static final String EXTRA_NAME_LEVEL = "level";

    /**
     * The name of a Parcelable extra that can be used to send status updates to
     * the caller. This must be a {@link Messenger} object.
     */
    public static final String EXTRA_NAME_MESSENGER = "messenger";

    /**
     * The "what" attribute of a {@link Message} that indicates that the
     * operation completed successfully.
     */
    public static final int WHAT_SUCCESS = 100;

    /**
     * The "what" attribute of a {@link Message} that indicates that the
     * operation failed. The "data" of the message will be a bundle with a
     * string key, {@link #KEY_MESSAGE}, whose value is an error message
     * suitable for display to a user.
     */
    public static final int WHAT_FAILED = 200;

    /**
     * The "what" attribute of a {@link Message} that indicates that a request
     * for superuser (a.k.a. "root") privileges has started.
     */
    public static final int WHAT_ROOT_REQUEST_STARTED = 300;

    /**
     * The "what" attribute of a {@link Message} that indicates that a request
     * for superuser (a.k.a. "root") privileges has completed.
     */
    public static final int WHAT_ROOT_REQUEST_COMPLETED = 301;

    /**
     * The key in a {@link Bundle} whose value is a string that is a message
     * suitable for display to a user.
     */
    public static final String KEY_MESSAGE = "message";

    /**
     * The brightness levels.
     */
    public static enum Level {
        OFF, DIM, BRIGHT, DEFAULT,
    }

    /**
     * Creates a new instance of SetBrightnessService.
     */
    public SetBrightnessService() {
        super("SetBrightnessService");
    }

    /**
     * Processes a request to set the backlight brightness.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        if (ACTION_SET_BRIGHTNESS.equals(action)) {
            this.setBrightness(intent);
        } else {
            throw new RuntimeException("unsupported action: " + action);
        }
    }

    private void setBrightness(Level level, Messenger messenger) {
        Log.i(Constants.LOG_TAG, "Setting capacitive buttons brightness to: "
            + level);

        final DeviceInfoDatabase devices = new DeviceInfoDatabase();
        final DeviceInfo device = devices.getForCurrentDevice();
        final CapacitiveButtonsBacklightBrightness buttons =
            device.getCapacitiveButtonsBacklightBrightness();

        // fail immediately if setting the brightness is not supported
        if (buttons == null) {
            final String messageText =
                this.getString(R.string.set_error_unsupported);
            reportError(messenger, messageText, null);
            return;
        }

        // convert the given level to an integer
        final Integer levelValue;
        switch (level) {
            case OFF:
                levelValue = 0;
                break;
            case DIM:
                levelValue = buttons.getDefaultDimLevel();
                break;
            case BRIGHT:
                levelValue = 100;
                break;
            case DEFAULT:
                levelValue = null;
                break;
            default:
                throw new AssertionError("unsupported level: " + level);
        }

        // save the new brightness level in the settings
        final Settings settings = new Settings(this);
        settings.setLevel(levelValue);

        // send a notification to the widgets to update their image
        ButtonBrightnessAppWidgetProvider.postUpdateWidgets(this);

        // set the brightness level
        final OperationNotifierMessageSender notifier =
            new OperationNotifierMessageSender(messenger);
        try {
            if (levelValue == null) {
                buttons.setDefault(notifier);
            } else {
                buttons.set(levelValue, 0, notifier);
            }
        } catch (final Exception e) {
            final String message = formatSetBrightnessErrorMessage(e, this);
            reportError(messenger, message, e);
            return;
        }

        // report success to the caller
        reportSuccess(messenger);

        // start or stop the service to respond to the screen turning on
        final Intent serviceIntent = new Intent();
        serviceIntent.setClass(this, ScreenPowerOnService.class);
        if (levelValue != null) {
            this.startService(serviceIntent);
        } else {
            this.stopService(serviceIntent);
        }
    }

    private void setBrightness(Intent intent) {
        final String levelName = intent.getStringExtra(EXTRA_NAME_LEVEL);
        if (levelName == null) {
            throw new RuntimeException("intent must define extra: "
                + EXTRA_NAME_LEVEL);
        }
        final Level level = Level.valueOf(levelName);

        final Messenger messenger =
            intent.getParcelableExtra(EXTRA_NAME_MESSENGER);
        this.setBrightness(level, messenger);
    }

    public static String formatSetBrightnessErrorMessage(Exception e,
            Context context) {
        final String exMessage = e.getMessage();
        final String message;
        if (e instanceof DimBrightnessNotSupportedException) {
            message =
                context.getString(R.string.set_error_dim_not_supported,
                    exMessage);
        } else if (e instanceof IntFileRootHelper.ChmodFailedException) {
            final String path =
                ((IntFileRootHelper.ChmodFailedException) e).getPath();
            message =
                context.getString(R.string.set_error_chmod_failed, path,
                    exMessage);
        } else if (e instanceof IntFileRootHelper.ChownExitCodeException) {
            final IntFileRootHelper.ChownExitCodeException e2 =
                (IntFileRootHelper.ChownExitCodeException) e;
            final String output = e2.getMessage();
            final String path = e2.getPath();
            final int exitCode = e2.getExitCode();
            message =
                context.getString(R.string.set_error_chown_exit_code, path,
                    exitCode, output);
        } else if (e instanceof IntFileRootHelper.ChownLaunchException) {
            final IntFileRootHelper.ChownLaunchException e2 =
                (IntFileRootHelper.ChownLaunchException) e;
            final String path = e2.getPath();
            message =
                context.getString(R.string.set_error_chown_launch, path,
                    exMessage);
        } else if (e instanceof IntFileRootHelper.ChownWaitInterruptedException) {
            final IntFileRootHelper.ChownWaitInterruptedException e2 =
                (IntFileRootHelper.ChownWaitInterruptedException) e;
            final String path = e2.getPath();
            message =
                context.getString(R.string.set_error_chown_wait, path,
                    exMessage);
        } else if (e instanceof IntFileRootHelper.StatFailedException) {
            final IntFileRootHelper.StatFailedException e2 =
                (IntFileRootHelper.StatFailedException) e;
            final String path = e2.getPath();
            message =
                context.getString(R.string.set_error_stat, path, exMessage);
        } else if (e instanceof IntFileRootHelper.IntFileNotFoundException) {
            final IntFileRootHelper.IntFileNotFoundException e2 =
                (IntFileRootHelper.IntFileNotFoundException) e;
            final String path = e2.getPath();
            message =
                context.getString(R.string.set_error_file_not_found, path);
        } else if (e instanceof IntFileRootHelper.IntFileIOException) {
            final IntFileRootHelper.IntFileIOException e2 =
                (IntFileRootHelper.IntFileIOException) e;
            final String path = e2.getPath();
            message = context.getString(R.string.set_error_io, path, exMessage);
        } else if (e instanceof IntFileRootHelper.RootShellCreateDeniedException) {
            message =
                context.getString(R.string.set_error_root_denied, exMessage);
        } else if (e instanceof IntFileRootHelper.RootShellCreateIOException) {
            message =
                context.getString(R.string.set_error_root_error, exMessage);
        } else if (e instanceof IntFileRootHelper.RootShellCreateTimeoutException) {
            message =
                context.getString(R.string.set_error_root_timeout, exMessage);
        } else if (e instanceof IntFileRootHelper.RootShellNotRootedException) {
            message = context.getString(R.string.set_error_root_not_rooted);
        } else if (e instanceof IntFileRootHelper.IntFileWriteException) {
            final IntFileRootHelper.IntFileWriteException e2 =
                (IntFileRootHelper.IntFileWriteException) e;
            final String path = e2.getPath();
            message =
                context.getString(R.string.set_error_generic, path, exMessage);
        } else {
            message = context.getString(R.string.set_error_unexpected, e);
        }

        return message;
    }

    /**
     * Schedules setting of the capacitive buttons backlight brightness to be
     * performed asynchronously in another thread.
     *
     * @param level the brightness level to set.
     * @param context the context to use to launch the service.
     * @param messenger an optional messenger to be notified of events that
     * occur while attempting to set the brightness; may be null to not receive
     * any such notifications.
     * @throws NullPointerException if level==null.
     */
    public static void queueButtonBacklightBrightnessChange(Level level,
            Context context, Messenger messenger) {
        if (level == null) {
            throw new NullPointerException("level==null");
        }

        final Intent intent = new Intent();
        intent.setAction(ACTION_SET_BRIGHTNESS);
        intent.setClass(context, SetBrightnessService.class);

        final String levelName = level.name();
        intent.putExtra(EXTRA_NAME_LEVEL, levelName);

        if (messenger != null) {
            intent.putExtra(EXTRA_NAME_MESSENGER, messenger);
        }

        context.startService(intent);
    }

    private static void reportError(Messenger messenger, String messageText,
            Exception exception) {

        Log.e(Constants.LOG_TAG,
            "Setting capacitive buttons brightness failed: " + messageText,
            exception);

        // nothing to do if no messenger was provided
        if (messenger == null) {
            return;
        }

        final Message message = Message.obtain();
        message.what = WHAT_FAILED;

        final Bundle bundle = new Bundle();
        bundle.putString(KEY_MESSAGE, messageText);
        message.setData(bundle);

        try {
            messenger.send(message);
        } catch (final RemoteException e) {
            Log.e(Constants.LOG_TAG, "Sending message failed", e);
        }
    }

    private static void reportSuccess(Messenger messenger) {
        reportWhat(messenger, WHAT_SUCCESS);
    }

    private static void reportWhat(Messenger messenger, int what) {
        // nothing to do if no messenger was provided
        if (messenger == null) {
            return;
        }

        final Message message = Message.obtain();
        message.what = what;

        try {
            messenger.send(message);
        } catch (final RemoteException e) {
            Log.e(Constants.LOG_TAG, "Sending message failed", e);
        }
    }

    private static class OperationNotifierMessageSender implements
            IntFileRootHelper.OperationNotifier {

        private final Messenger messenger;

        public OperationNotifierMessageSender(Messenger messenger) {
            this.messenger = messenger;
        }

        @Override
        public void rootRequestCompleted() {
            reportWhat(this.messenger, WHAT_ROOT_REQUEST_COMPLETED);
        }

        @Override
        public void rootRequestStarted() {
            reportWhat(this.messenger, WHAT_ROOT_REQUEST_STARTED);
        }
    }
}
