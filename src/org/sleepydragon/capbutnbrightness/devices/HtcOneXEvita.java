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
package org.sleepydragon.capbutnbrightness.devices;

import org.sleepydragon.capbutnbrightness.debug.DebugFilesProvider;

/**
 * A specialization of CapacitiveButtonsBacklightBrightness for the HTC One X.
 */
public class HtcOneXEvita implements CapacitiveButtonsBacklightBrightness,
        DebugFilesProvider {

    public static final String BUTTONS_BACKLIGHT_DIR =
        "/sys/class/leds/button-backlight";
    public static final String CURRENTS_PATH = BUTTONS_BACKLIGHT_DIR
        + "/currents";
    public static final String BRIGHTNESS_PATH = BUTTONS_BACKLIGHT_DIR
        + "/brightness";
    public static final String LUT_COEFFICIENT_PATH = BUTTONS_BACKLIGHT_DIR
        + "/lut_coefficient";

    public HtcOneXEvita() {
    }

    public FileInfo[] getDebugFiles() {
        return new FileInfo[] { new FileInfo(CURRENTS_PATH, FileContents.INT),
            new FileInfo(BRIGHTNESS_PATH, FileContents.INT),
            new FileInfo(LUT_COEFFICIENT_PATH, FileContents.INT), };
    }

    public int getDefaultDimLevel() {
        return 50;
    }

    public boolean isSupported() {
        final boolean exists = FileHelper.fileExists(CURRENTS_PATH);
        return exists;
    }

    public void set(int level, int options) throws SetException {
        if (level < 0 || level > 100) {
            throw new IllegalArgumentException("invalid level: " + level);
        }

        // on evita, there is nothing special to do when the screen turns on
        final boolean inResponseToScreenOn =
            ((options & OPTION_SCREEN_ON) == OPTION_SCREEN_ON);
        if (inResponseToScreenOn) {
            return;
        }

        RootHelper.verifyRooted();
        RootHelper.verifyRootAccessGranted();

        RootHelper.chmod("666", CURRENTS_PATH);
        final boolean backlightOn = (level != 0);
        if (!backlightOn) {
            FileHelper.writeToFile(0, CURRENTS_PATH);
        } else {
            final boolean dim = (level != 100);
            final int currents = dim ? 1 : 3;
            FileHelper.writeToFile(currents, CURRENTS_PATH);
        }
        RootHelper.chmod("444", CURRENTS_PATH);
    }

    public void setDefault() throws SetException {
        this.set(100, 0);
        RootHelper.chmod("644", CURRENTS_PATH);
    }
}
