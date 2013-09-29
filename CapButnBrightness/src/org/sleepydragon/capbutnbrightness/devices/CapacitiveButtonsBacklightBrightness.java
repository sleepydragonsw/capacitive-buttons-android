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

import org.sleepydragon.capbutnbrightness.IntFileRootHelper;

/**
 * Methods that control the brightness of the capacitive buttons backlight.
 */
public interface CapacitiveButtonsBacklightBrightness {

    /**
     * Option to set() which indicates that the method is being invoked in
     * response to the screen turning on. In this case, the method may perform
     * some optimizations compared to when the brightness is set normally that
     * only apply in this situation.
     *
     * @see #set(int, int)
     */
    public static final int OPTION_SCREEN_ON = 0x00000001;

    /**
     * Returns the default brightness level to use when the "dim" option is
     * selected by a user.
     *
     * @return the default brightness level to use when the "dim" option is
     * selected by a user; this value will be greater than or equal to 0 and
     * less than or equal to 100.
     */
    public int getDefaultDimLevel();

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
     * @param options must be a combination of the OPTION_ constants defined in
     * this class, which influences the behaviour of the method; if none of the
     * options apply, specify 0.
     * @throws IntFileRootHelper.IntWriteException if setting the brightness of
     * the capacitive buttons backlight fails.
     * @throws IllegalArgumentException if the given level is less than 0 or
     * greater than 100.
     * @throws UnsupportedOperationException if isSupported() returns false.
     * @throws DimBrightnessNotSupportedException if the given level means a
     * "dim" setting but the device does not support dim.
     */
    public void set(int level, int options)
            throws IntFileRootHelper.IntWriteException,
            DimBrightnessNotSupportedException;

    /**
     * Sets the brightness of the capacitive buttons backlight to its default
     * value and reverts and changes made to the system that were required to
     * sustain the custom value (eg. chmodding files).
     *
     * @throws IntFileRootHelper.IntWriteException if setting the brightness of
     * the capacitive buttons backlight fails.
     */
    public void setDefault() throws IntFileRootHelper.IntWriteException;

    /**
     * Exception thrown if setting the brightness of the capacitive buttons
     * backlight fails due to a "dim" brightness being requested (ie. neither
     * "bright" nor "off") and the device does not support that brightness.
     */
    public class DimBrightnessNotSupportedException extends Exception {

        private static final long serialVersionUID = 7551491163530984762L;

        public DimBrightnessNotSupportedException(String message) {
            super(message);
        }
    }
}
