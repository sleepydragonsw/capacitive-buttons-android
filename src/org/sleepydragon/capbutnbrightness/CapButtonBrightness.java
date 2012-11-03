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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import android.util.Log;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.Shell;

/**
 * Set the brightness of the capacitive buttons.
 */
public class CapButtonBrightness {

    /**
     * The path of the file to which the current is written in order to set the
     * capacitive button brightness.
     */
    public static final String SET_PATH = "/sys/devices/platform/msm_ssbi.0/"
        + "pm8921-core/pm8xxx-led/leds/button-backlight/currents";

    /**
     * The possible brightness levels to set.
     */
    public enum Level {
        OFF, DIM, BRIGHT,
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private CapButtonBrightness() {
    }

    /**
     * Sets the brightness of the capacitive buttons.
     * 
     * @param level the brightness to set.
     * @throws SetCapButtonBrightnessException if an error occurs.
     * @throws InterruptedException if waiting for the command to complete is
     * interrupted.
     * @throws NullPointerException if level==null
     */
    public static void set(Level level) throws SetCapButtonBrightnessException,
            InterruptedException {
        if (level == null) {
            throw new NullPointerException("level==null");
        }

        Log.i(Constants.LOG_TAG, "Setting capacitive button brightness to "
            + level.name());

        if (!RootTools.isRootAvailable()) {
            throw new SetCapButtonBrightnessException(
                SetCapButtonBrightnessException.ErrorId.SU_BINARY_MISSING,
                "device is not rooted", null);
        } else if (!RootTools.isAccessGiven()) {
            throw new SetCapButtonBrightnessException(
                SetCapButtonBrightnessException.ErrorId.ROOT_ACCESS_DENIED,
                "root access denied", null);
        }

        final int current = getCurrent(level);
        final String commandStr = getSetBrightnessCommand(current);
        final Shell shell;
        try {
            shell = RootTools.getShell(true);
        } catch (TimeoutException e) {
            throw new SetCapButtonBrightnessException(
                SetCapButtonBrightnessException.ErrorId.ROOT_ACCESS_TIMEOUT,
                "unable to get root shell: timeout expired: " + e.getMessage(),
                e);
        } catch (IOException e) {
            throw new SetCapButtonBrightnessException(
                SetCapButtonBrightnessException.ErrorId.ROOT_ACCESS_IOEXCEPTION,
                "unable to get root shell: IO error: " + e.getMessage(), e);
        }

        final Command command = new CommandNoCapture(1, commandStr);
        try {
            shell.add(command);
        } catch (IOException e) {
            throw new SetCapButtonBrightnessException(
                SetCapButtonBrightnessException.ErrorId.ROOT_ACCESS_IOEXCEPTION,
                "running command as root failed: " + e.getMessage(), e);
        }

        command.waitForFinish();
    }

    /**
     * Creates and returns a string whose value is the command to execute in
     * order to set the capacitive button brightness.
     * 
     * @param current the current to set.
     * @return the command to run to set the capacitive button brightness to the
     * given current.
     */
    public static String getSetBrightnessCommand(int current) {
        StringBuilder sb = new StringBuilder();
        sb.append("echo ");
        sb.append(current);
        sb.append(" > ");
        sb.append(SET_PATH);
        String s = sb.toString();
        return s;
    }

    /**
     * Returns the "current" to set in SET_PATH in order to set the brightness
     * of the capacitive buttons to the given level.
     * 
     * @param level the brightness level whose current value to get.
     * @return the current value of the given brightness level.
     */
    public static int getCurrent(Level level) {
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

    private static class CommandNoCapture extends Command {

        public CommandNoCapture(int id, String... command) {
            super(id, command);
        }

        @Override
        public void output(int id, String line) {
            // discard output
        }
    }

}
