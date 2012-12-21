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
package org.sleepydragon.capbutnbrightness.devices;

/**
 * A specialization of CapacitiveButtonsBacklightBrightness for the HTC One X.
 */
public class HtcOneXEndeavoru implements CapacitiveButtonsBacklightBrightness {

    public static final String BUTTONS_BACKLIGHT_DIR =
        "/sys/class/leds/button-backlight";
    public static final String CURRENTS_PATH = BUTTONS_BACKLIGHT_DIR
        + "/currents";
    public static final String BRIGHTNESS_PATH = BUTTONS_BACKLIGHT_DIR
        + "/brightness";

    public HtcOneXEndeavoru() {
    }

    public int getDefaultDimLevel() {
        return 50;
    }

    public boolean isSupported() {
        final boolean cExists = FileHelper.fileExists(CURRENTS_PATH);
        final boolean bExists = FileHelper.fileExists(BRIGHTNESS_PATH);
        final boolean supported = (cExists && bExists);
        return supported;
    }

    public void set(int level, int options) throws SetException {
        if (level < 0 || level > 100) {
            throw new IllegalArgumentException("invalid level: " + level);
        }

        // on endeavoru, there is nothing to do when the screen turns on
        // and the brightness level is set to "off".
        final boolean inResponseToScreenOn =
            ((options & OPTION_SCREEN_ON) == OPTION_SCREEN_ON);
        if (inResponseToScreenOn && level == 0) {
            return;
        }

        RootHelper.verifyRooted();
        RootHelper.verifyRootAccessGranted();

        RootHelper.chmod("666", BRIGHTNESS_PATH);
        RootHelper.chmod("666", CURRENTS_PATH);

        final boolean backlightOn = (level != 0);
        if (!backlightOn) {
            FileHelper.writeToFile(0, BRIGHTNESS_PATH);
            FileHelper.writeToFile(0, CURRENTS_PATH);
        } else {
            final boolean dim = (level != 100);
            final int currents = dim ? 1 : 3;
            if (!inResponseToScreenOn) {
                FileHelper.writeToFile(currents, CURRENTS_PATH);
            }
            FileHelper.writeToFile(1, BRIGHTNESS_PATH);
            if (!inResponseToScreenOn) {
                FileHelper.writeToFile(currents, CURRENTS_PATH);
            }
        }
        RootHelper.chmod("444", BRIGHTNESS_PATH);
        RootHelper.chmod("444", CURRENTS_PATH);
    }

    public void setDefault() throws SetException {
        this.set(100, 0);
        RootHelper.chmod("644", CURRENTS_PATH);
        RootHelper.chmod("644", BRIGHTNESS_PATH);
    }
}
