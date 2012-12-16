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
 * Methods that control the brightness of the capacitive buttons backlight.
 */
public interface CapacitiveButtonsBacklightBrightness {

    /**
     * Returns whether or not setting the capacitive button brightness is
     * supported.
     *
     * @return true if setting the capacitive button brightness is supported;
     * false if it is not supported.
     */
    public boolean isSupported();

    /**
     * Sets the brightness of the capacitive buttons backlight.
     * <p>
     * If this device does not actually support the given brightness level then
     * the implementation will choose an appropriate alternative.
     *
     * @param level a value between 0 and 100, inclusive, where 0 is completely
     * off, 100 is completely on, and any value in between is on but not with
     * full brightness.
     * @throws SetException if setting the brightness of the capacitive buttons
     * backlight fails.
     * @throws IllegalArgumentException if the given level is less than 0 or
     * greater than 100.
     * @throws UnsupportedOperationException if isSupported() returns false.
     */
    public void set(int level) throws SetException;

    /**
     * Sets the brightness of the capacitive buttons backlight to its default
     * value and reverts and changes made to the system that were required to
     * sustain the custom value (eg. chmodding files).
     * <p>
     *
     * @throws SetException if setting the brightness of the capacitive buttons
     * backlight fails.
     */
    public void setDefault() throws SetException;

    /**
     * Exception thrown if setting the brightness of the capacitive buttons
     * backlight fails.
     */
    public class SetException extends Exception {

        private static final long serialVersionUID = 7551491163530984762L;

        public SetException(String message) {
            super(message);
        }
    }
}
