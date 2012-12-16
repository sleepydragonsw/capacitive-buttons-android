/*
 * This file is part of Capacitive Button Brightness.
 *
 * Capacitive Button Brightness is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Capacitive Button Brightness is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Capacitive Button Brightness.  If not, see <http://www.gnu.org/licenses/>.
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
     * @throws NullPointerException if settings==null.
     */
    public void doSetCapButtonBrightness(Settings settings) {
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
                    buttons.set(level);
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
     * {@link #shouldRun(Intent, Settings)}. If that returns true then the
     * settings are in turn specified to
     * {@link #doSetCapButtonBrightness(Settings)}.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final Settings settings = new Settings(context);
        if (shouldRun(intent, settings)) {
            this.doSetCapButtonBrightness(settings);
        }
    }

    /**
     * Determines whether or not the capacitive button brightness should be set
     * by this broadcast receiver based on the given Intent and settings.
     * <p>
     * If the action of the given intent is {@link Intent#ACTION_BOOT_COMPLETED}
     * then this method will return false if the
     * {@link Settings#isSetBrightnessOnBootEnabled()} return false. Returns
     * true under all other conditions.
     *
     * @param intent the intent to check.
     * @param settings the settings to check.
     * @return true if the capacitive button brightness should be set by this
     * broadcast receiver, false otherwise.
     * @throws NullPointerException if any argument is null.
     */
    public static boolean shouldRun(Intent intent, Settings settings) {
        if (intent == null) {
            throw new NullPointerException("intent==null");
        } else if (settings == null) {
            throw new NullPointerException("settings==null");
        }

        final String action = intent.getAction();
        final boolean shouldRun;
        if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            shouldRun = settings.isSetBrightnessOnBootEnabled();
        } else {
            shouldRun = true;
        }
        return shouldRun;
    }
}
