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

import java.io.File;

import org.sleepydragon.capbutnbrightness.Constants;
import org.sleepydragon.capbutnbrightness.IntFileRootHelper;
import org.sleepydragon.capbutnbrightness.debug.DebugFilesProvider;

import android.util.Log;

/**
 * Methods that control the brightness of the capacitive buttons backlight.
 */
public abstract class CapacitiveButtonsBacklightBrightness implements
        DebugFilesProvider {

    public static final String BUTTONS_BACKLIGHT_DIR =
        "/sys/class/leds/button-backlight";

    public static final String CURRENTS_PATH = BUTTONS_BACKLIGHT_DIR
        + "/currents";

    public static final String BRIGHTNESS_PATH = BUTTONS_BACKLIGHT_DIR
        + "/brightness";

    public static final String LUT_COEFFICIENT_PATH = BUTTONS_BACKLIGHT_DIR
        + "/lut_coefficient";

    /**
     * Option to set() which indicates that the method is being invoked in
     * response to the screen turning on. In this case, the method may perform
     * some optimizations compared to when the brightness is set normally that
     * only apply in this situation.
     *
     * @see #set
     */
    public static final int OPTION_SCREEN_ON = 0x00000001;

    public FileInfo[] getDebugFiles() {
        return new FileInfo[] { new FileInfo(CURRENTS_PATH, FileContents.INT),
            new FileInfo(BRIGHTNESS_PATH, FileContents.INT),
            new FileInfo(LUT_COEFFICIENT_PATH, FileContents.INT), };
    }

    /**
     * Returns the default brightness level to use when the "dim" option is
     * selected by a user.
     *
     * @return the default brightness level to use when the "dim" option is
     * selected by a user; this value will be greater than or equal to 0 and
     * less than or equal to 100.
     */
    public int getDefaultDimLevel() {
        return 50;
    }

    /**
     * Returns a list of files that must exist in order for setting of the
     * capacitive buttons brightness of this device to <em>dim</em> to be
     * supported. This method is invoked by the implementation of
     * {@link #isDimSupported} to determine if this device is supported.
     * <p>
     * The implementation of this method in this class returns an empty array;
     * subclasses may return a different list.
     *
     * @return a non-null array of non-null Strings whose values are the paths
     * of the files that must exist in order for dim to be supported.
     */
    public String[] getRequiredDimFiles() {
        return new String[0];
    }

    /**
     * Returns a list of files that must exist in order for setting of the
     * capacitive buttons brightness of this device to be supported. This method
     * is invoked by the implementation of {@link #isSupported} to determine if
     * this device is supported.
     *
     * @return a non-null array of non-null Strings whose values are the paths
     * of the files that must exist in order to be supported.
     */
    public abstract String[] getRequiredFiles();

    /**
     * Returns whether or not setting the capacitive button brightness to
     * <em>dim</em> is supported. The implementation of this method in this
     * class returns true if and only if every files returned by
     * {@link #getRequiredDimFiles()} exists.
     *
     * @return true if setting the capacitive button brightness to dim is
     * supported; false if it is not supported.
     */
    public boolean isDimSupported() {
        final String[] paths = this.getRequiredDimFiles();
        final boolean supported = allFilesExist(paths);
        return supported;
    }

    /**
     * Returns whether or not setting the capacitive button brightness is
     * supported. The implementation of this method in this class returns true
     * if and only if every files returned by {@link #getRequiredFiles()}
     * exists.
     *
     * @return true if setting the capacitive button brightness is supported;
     * false if it is not supported.
     */
    public boolean isSupported() {
        final String[] paths = this.getRequiredFiles();
        final boolean supported = allFilesExist(paths);
        return supported;
    }

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
     * @param notifier an object to be notified when events occur; may be null
     * to not send notifications of events.
     * @throws IntFileRootHelper.IntWriteException if setting the brightness of
     * the capacitive buttons backlight fails.
     * @throws IllegalArgumentException if the given level is less than 0 or
     * greater than 100.
     * @throws UnsupportedOperationException if isSupported() returns false.
     * @throws DimBrightnessNotSupportedException if the given level means a
     * "dim" setting but the device does not support dim.
     */
    public abstract void set(int level, int options,
            IntFileRootHelper.OperationNotifier notifier)
            throws IntFileRootHelper.IntWriteException,
            DimBrightnessNotSupportedException;

    /**
     * Sets the brightness of the capacitive buttons backlight to its default
     * value and reverts and changes made to the system that were required to
     * sustain the custom value (eg. chmodding files).
     *
     * @param notifier an object to be notified when events occur; may be null
     * to not send notifications of events.
     * @throws IntFileRootHelper.IntWriteException if setting the brightness of
     * the capacitive buttons backlight fails.
     */
    public void setDefault(IntFileRootHelper.OperationNotifier notifier)
            throws IntFileRootHelper.IntWriteException {
        try {
            this.set(100, 0, notifier);
        } catch (final DimBrightnessNotSupportedException e) {
            throw new RuntimeException("should never happen: " + e);
        }

        try {
            IntFileRootHelper.makeWritable(CURRENTS_PATH);
        } catch (final IntFileRootHelper.ChmodFailedException e) {
            Log.w(Constants.LOG_TAG,
                "unable to make file writeable when restoring default: "
                    + CURRENTS_PATH, e);
        }
        try {
            IntFileRootHelper.makeWritable(BRIGHTNESS_PATH);
        } catch (final IntFileRootHelper.ChmodFailedException e) {
            Log.w(Constants.LOG_TAG,
                "unable to make file writeable when restoring default: "
                    + BRIGHTNESS_PATH, e);
        }
    }

    private static boolean allFilesExist(String[] paths) {
        for (final String path : paths) {
            final File file = new File(path);
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Changes the permissions of all files to read-only, to prevent the OS from
     * messing with them. On failure, a warning is logged but no exceptions are
     * thrown.
     *
     * @param intFile the IntFileRootHelper to use to access the files.
     * @throws NullPointerException if intFile==null.
     */
    protected static void makeAllFilesReadOnly(IntFileRootHelper intFile) {
        if (intFile == null) {
            throw new NullPointerException("intFile==null");
        }
        final String[] paths = { CURRENTS_PATH, BRIGHTNESS_PATH };
        for (final String path : paths) {
            final File file = new File(path);
            if (file.exists()) {
                try {
                    intFile.protectFileFromOs(path);
                } catch (final IntFileRootHelper.IntWriteException e) {
                    Log.w(Constants.LOG_TAG,
                        "unable to protect file from changes by the OS: "
                            + BRIGHTNESS_PATH, e);
                }
            }
        }
    }

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
