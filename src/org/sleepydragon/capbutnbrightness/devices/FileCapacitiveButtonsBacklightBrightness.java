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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.Shell;

/**
 * An implementation of CapacitiveButtonsBacklightBrightness that sets the
 * brightness by writing to files as root.
 */
public class FileCapacitiveButtonsBacklightBrightness implements
        CapacitiveButtonsBacklightBrightness {

    private final String onOffPath;
    private final String brightnessPath;
    private final int onValue;
    private final int offValue;
    private final int defaultBrightness;
    private final int defaultDimBrightness;

    /**
     * Creates a new instance of FileCapacitiveButtonsBacklightBrightness.
     *
     * @param onOffPath the path of the file to which to write in order to
     * enable and/or disable the capacitive buttons backlight.
     * @param brightnessPath the path of the file to which to write in order to
     * set the brightness of the capacitive buttons backlight.
     * @param onValue the value to write to the file specified by onOffPath in
     * order to turn on the capacitive buttons backlight.
     * @param offValue the value to write to the file specified by onOffPath in
     * order to turn off the capacitive buttons backlight.
     * @param defaultBrightness the value to write to the file specified by
     * brightnessPath in order to set the brightness of the capacitive buttons
     * backlight to its default level.
     * @param defaultDimBrightness the value to write to the file specified by
     * brightnessPath in order to set the brightness of the capacitive buttons
     * backlight to its default level when the "dim" option is selected.
     * @throws NullPointerException if any argument is null.
     */
    public FileCapacitiveButtonsBacklightBrightness(String onOffPath,
            String brightnessPath, int onValue, int offValue,
            int defaultBrightness, int defaultDimBrightness) {
        if (onOffPath == null) {
            throw new NullPointerException("onOffPath==null");
        } else if (brightnessPath == null) {
            throw new NullPointerException("brightnessPath==null");
        }
        this.onOffPath = onOffPath;
        this.brightnessPath = brightnessPath;
        this.onValue = onValue;
        this.offValue = offValue;
        this.defaultBrightness = defaultBrightness;
        this.defaultDimBrightness = defaultDimBrightness;
    }

    public int getDefaultDimLevel() {
        return this.defaultDimBrightness;
    }

    /**
     * Returns whether or not the file given to the constructor for onOffPath
     * exists.
     *
     * @return true if the file given to the constructor for onOffPath exists,
     * false if it does not exist.
     */
    public boolean isSupported() {
        final File file = new File(this.onOffPath);
        final boolean exists = file.exists();
        return exists;
    }

    private void set(boolean enabled, int level, boolean makeOnOffFileReadOnly,
            boolean makeBrightnessFileReadOnly) throws SetException {
        if (!this.isSupported()) {
            throw new UnsupportedOperationException("not supported");
        }

        verifyRooted();
        verifyRootAccessGranted();

        // we need to set the brightness *before* setting the on/off; otherwise,
        // the brightness has no effect; also chmod the file irrespective of
        // whether or not we are actually writing to it so that it will always
        // be left with the expected permissions when this method returns
        chmod("666", this.brightnessPath);
        if (enabled) {
            writeToFile(level, this.brightnessPath);
        }
        if (makeBrightnessFileReadOnly) {
            chmod("444", this.brightnessPath);
        }

        chmod("666", this.onOffPath);
        final int onOffValue = enabled ? this.onValue : this.offValue;
        writeToFile(onOffValue, this.onOffPath);
        if (makeOnOffFileReadOnly) {
            chmod("444", this.onOffPath);
        }
    }

    /**
     * Sets the brightness of the capacitive buttons.
     * <p>
     * If the given level is 0 then the value given to the constructor for
     * offValue is written to the file given to the constructor for onOffPath.
     * The permissions of the onOffPath are also set to 444 to prevent other
     * processes from turning the backlight back on.
     * <p>
     * If the given level is greater than 0 then the value given to the
     * constructor for onValue is written to the file given to the constructor
     * for onOffPath. The permissions of the onOffPath are also set to 644 to
     * allow the operating system to turn the backlight back on when required.
     * Also, the given level is written to to the file given to the constructor
     * for brightnessPath, if it exists. The permissions of this file are set to
     * 444 as well in order to prevent the level from being changed.
     */
    public void set(int level) throws SetException {
        if (level < 0 || level > 100) {
            throw new IllegalArgumentException("invalid level: " + level);
        }
        final boolean enabled = (level != 0);
        final boolean makeOnOffFileReadOnly = (level == 0);
        final boolean makeBrightnessFileReadOnly = (level != 0);
        this.set(enabled, level, makeOnOffFileReadOnly,
            makeBrightnessFileReadOnly);
    }

    /**
     * Sets the capacitive buttons backlight back to its defaults.
     * <p>
     * Writes the value given to the constructor for onValue is to the file
     * given to the constructor for onOffPath. If brightnessPath exists, then
     * the value given to the constructor for defaultBrightness is to the file
     * given to the constructor for brightnessPath. The permissions of the files
     * in question are changed back to their default, 644.
     */
    public void setDefault() throws SetException {
        this.set(true, this.defaultBrightness, false, false);
    }

    private static void chmod(String permissions, String path)
            throws SetException {
        assert permissions != null;
        assert path != null;
        final String command = "chmod " + permissions + " " + path;
        runAsRoot(command);
    }

    private static void runAsRoot(String command) throws SetException {
        assert command != null;

        final Shell shell;
        try {
            shell = RootTools.getShell(true);
        } catch (final TimeoutException e) {
            throw new SetException("timeout waiting for root shell: "
                + e.getMessage());
        } catch (final IOException e) {
            throw new SetException("unable to get root shell: "
                + e.getMessage());
        }

        final Command commandObj = new CommandNoCapture(1, command);
        try {
            shell.add(commandObj);
        } catch (final IOException e) {
            throw new SetException("unable to run command as root: " + command
                + " (" + e.getMessage() + ")");
        }

        try {
            commandObj.waitForFinish();
        } catch (final InterruptedException e) {
            // oh well?
        }
    }

    private static void verifyRootAccessGranted()
            throws RootAccessDeniedException {
        final boolean isAccessGranted = RootTools.isAccessGiven();
        if (!isAccessGranted) {
            throw new RootAccessDeniedException("root access was denied");
        }
    }

    private static void verifyRooted() throws NotRootedException {
        final boolean isRootAvailable = RootTools.isRootAvailable();
        if (!isRootAvailable) {
            throw new NotRootedException("device is not rooted");
        }
    }

    private static void writeToFile(int value, String path) throws SetException {
        assert path != null;

        final StringBuilder sb = new StringBuilder();
        sb.append(value);
        sb.append('\n');
        final String valueToWrite = sb.toString();
        final byte[] bytesToWrite = valueToWrite.getBytes();

        try {
            final FileOutputStream out = new FileOutputStream(path);
            try {
                out.write(bytesToWrite);
            } finally {
                out.close();
            }
        } catch (final IOException e) {
            throw new SetException("unable to write " + value + " to file: "
                + path + " (" + e.getMessage() + ")");
        }
    }

    private static class CommandNoCapture extends Command {

        public CommandNoCapture(int id, String... command) {
            super(id, command);
        }

        @Override
        public void output(int id, String line) {
            // discard output
        }
    }

    /**
     * Exception thrown by set() if the device is not rooted.
     */
    public static class NotRootedException extends SetException {
        private static final long serialVersionUID = -1779660485744724818L;

        public NotRootedException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown by set() if root access is denied.
     */
    public static class RootAccessDeniedException extends SetException {
        private static final long serialVersionUID = -4220447372051893195L;

        public RootAccessDeniedException(String message) {
            super(message);
        }
    }
}
