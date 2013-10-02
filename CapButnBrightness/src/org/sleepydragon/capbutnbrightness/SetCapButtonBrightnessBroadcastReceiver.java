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
     * Runs this broadcast receiver. This method first gets the Settings object,
     * then specifies it along with the given intent to
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Constants.LOG_TAG,
            "SetCapButtonBrightnessBroadcastReceiver.onReceive() " + "action="
                + intent.getAction());

        final Settings settings = new Settings(context);

        final Integer level = settings.getLevel();
        if (level == null) {
            return;
        }

        final String action = intent.getAction();
        final boolean shouldRun;
        if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            shouldRun = settings.isSetBrightnessOnBootEnabled();
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

            final SetBrightnessService.Level levelObj;
            if (action != null && action.equals(Intent.ACTION_SCREEN_OFF)) {
                levelObj = SetBrightnessService.Level.OFF;
            } else if (level == 0) {
                levelObj = SetBrightnessService.Level.OFF;
            } else if (level == 100) {
                levelObj = SetBrightnessService.Level.BRIGHT;
            } else {
                levelObj = SetBrightnessService.Level.DIM;
            }

            SetBrightnessService.queueButtonBacklightBrightnessChange(levelObj,
                setOptions, false, context, null);
        }
    }

}
