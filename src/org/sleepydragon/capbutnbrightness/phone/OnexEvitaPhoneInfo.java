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
package org.sleepydragon.capbutnbrightness.phone;

import org.sleepydragon.capbutnbrightness.CapButtonBrightness.Level;

/**
 * Information necessary for setting the capacitive button brightness on the
 * North American HTC One X (evita).
 */
public class OnexEvitaPhoneInfo extends PhoneInfo {

    public static final String PATH = "/sys/devices/platform/msm_ssbi.0/"
        + "pm8921-core/pm8xxx-led/leds/button-backlight/currents";

    public static final String NAME = "HTC One X (evita)";

    /**
     * Creates a new instance of this class.
     */
    public OnexEvitaPhoneInfo() {
        super(NAME, PATH, 3);
    }

    @Override
    public int getLevelToWrite(Level level) {
        switch (level) {
            case OFF:
                return 0;
            case DIM:
                return 1;
            case BRIGHT:
                return 2;
            default:
                throw new AssertionError("unknown level: " + level);
        }
    }
}
