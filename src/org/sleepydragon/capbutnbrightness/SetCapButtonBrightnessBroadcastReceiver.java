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
import org.sleepydragon.capbutnbrightness.devices.DeviceInfo;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfoDatabase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * A broadcast receiver that sets the capacitive button brightness to the saved
 * value.
 */
public class SetCapButtonBrightnessBroadcastReceiver extends BroadcastReceiver {

    /**
     * Sets the capacitive button brightness to the level retrieved from the
     * given Settings. If the level retrieved from the settings is null, then
     * this method does nothing. Any exceptions thrown during settings of the
     * capacitive button brightness will be logged in logcat.
     *
     * @param settings the settings from which to retrieve the level of the
     * capacitive button brightness to set.
     * @param setOptions is an int that will be specified as the "options"
     * parameter to {@link CapacitiveButtonsBacklightBrightness#set(int, int)}
     * when invoked.
     * @throws NullPointerException if settings==null.
     */
    public void doSetCapButtonBrightness(Settings settings, int setOptions) {
        if (settings == null) {
            throw new NullPointerException("settings==null");
        }
        final Integer level = settings.getLevel();
        if (level != null) {
            final DeviceInfoDatabase db = new DeviceInfoDatabase();
            final DeviceInfo device = db.getForCurrentDevice();
            final CapacitiveButtonsBacklightBrightness buttons =
                device.getCapacitiveButtonsBacklightBrightness();

            if (buttons != null) {
                try {
                    buttons.set(level, setOptions);
                } catch (final Exception e) {
                    Log.e(Constants.LOG_TAG, "unable to set capacitive button "
                        + "brightness on boot in " + this.getClass().getName(),
                        e);
                }
            }
        }
    }

    /**
     * Runs this broadcast receiver. This method first gets the Settings object,
     * then specifies it along with the given intent to
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Constants.LOG_TAG,
            "SetCapButtonBrightnessBroadcastReceiver.onReceive() " + "action="
                + intent.getAction());

        final Settings settings = new Settings(context);

        final String action = intent.getAction();
        final boolean shouldRun;
        if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            shouldRun = settings.isSetBrightnessOnBootEnabled();

            // start the service to respond to the screen turning on
            final Intent serviceIntent = new Intent();
            serviceIntent.setClass(context, ScreenPowerOnService.class);
            context.startService(serviceIntent);
        } else {
            shouldRun = true;
        }

        if (shouldRun) {
            final int setOptions;
            if (action != null && action.equals(Intent.ACTION_SCREEN_ON)) {
                setOptions =
                    CapacitiveButtonsBacklightBrightness.OPTION_SCREEN_ON;
            } else {
                setOptions = 0;
            }

            this.doSetCapButtonBrightness(settings, setOptions);
        }
    }

}
