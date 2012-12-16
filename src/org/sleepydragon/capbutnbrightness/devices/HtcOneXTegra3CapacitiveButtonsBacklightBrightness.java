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
 * A specialization of FileCapacitiveButtonsBacklightBrightness which uses the
 * properties specific to the International variant of the HTC One X and the HTC
 * One X+, which use the Tegra3 SOC.
 */
public class HtcOneXTegra3CapacitiveButtonsBacklightBrightness extends
        FileCapacitiveButtonsBacklightBrightness {

    public static final String BUTTONS_BACKLIGHT_DIR =
        "/sys/class/leds/button-backlight";

    public static final String ON_OFF_PATH = BUTTONS_BACKLIGHT_DIR
        + "/brightness";

    public static final String BRIGHTNESS_PATH = BUTTONS_BACKLIGHT_DIR
        + "/lut_coefficient";

    public static final int ON_VALUE = 255;
    public static final int OFF_VALUE = 0;
    public static final int DEFAULT_BRIGHTNESS = 100;
    public static final int DEFAULT_DIM_BRIGHTNESS = 20;

    public HtcOneXTegra3CapacitiveButtonsBacklightBrightness() {
        super(ON_OFF_PATH, BRIGHTNESS_PATH, ON_VALUE, OFF_VALUE,
            DEFAULT_BRIGHTNESS, DEFAULT_DIM_BRIGHTNESS);
    }
}
