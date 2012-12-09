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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.sleepydragon.capbutnbrightness.phone.PhoneInfo;
import org.sleepydragon.capbutnbrightness.phone.PhoneInfoChooser;

import android.util.Log;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.Shell;

/**
 * Set the brightness of the capacitive buttons.
 */
public class CapButtonBrightness {

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

        final PhoneInfo phoneInfo = PhoneInfoChooser.getForCurrentPhone();
        if (phoneInfo == null) {
            throw new SetCapButtonBrightnessException(
                SetCapButtonBrightnessException.ErrorId.UNSUPPORTED_PHONE,
                "current phone is unsupported", null);
        }

        if (!RootTools.isRootAvailable()) {
            throw new SetCapButtonBrightnessException(
                SetCapButtonBrightnessException.ErrorId.SU_BINARY_MISSING,
                "device is not rooted", null);
        } else if (!RootTools.isAccessGiven()) {
            throw new SetCapButtonBrightnessException(
                SetCapButtonBrightnessException.ErrorId.ROOT_ACCESS_DENIED,
                "root access denied", null);
        }

        // on endeavoru, the backlight does not turn back on with the screen
        // if we set the brightness to "on"; to work around this, only set the
        // file's permissions to read-only if setting the brightness to "off"
        final boolean makeReadOnly = (level == Level.OFF);

        final int current = phoneInfo.getLevelToWrite(level);
        final String path = phoneInfo.getPath();
        final String[] commandStrs =
            getSetBrightnessCommands(current, path, makeReadOnly);
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

        for (final String commandStr : commandStrs) {
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
    }

    /**
     * Creates and returns a list of strings whose values are the commands to
     * execute in * order to set the capacitive button brightness.
     * 
     * @param current the current to set.
     * @param path the path of the file to which to write the current.
     * @param makeReadOnly if true, then set the file to read-only permissions
     * as the final command; if false, then leave the file permissions as
     * read-write.
     * @return the commands to run to set the capacitive button brightness to
     * the given current.
     * @throws NullPointerException if path==null.
     */
    public static String[] getSetBrightnessCommands(int current, String path,
            boolean makeReadOnly) {
        if (path == null) {
            throw new NullPointerException("path==null");
        }
        final List<String> list = new ArrayList<String>();
        list.add("chmod 666 " + path);
        list.add("echo " + current + " > " + path);
        if (makeReadOnly) {
            list.add("chmod 444 " + path);
        }
        return list.toArray(new String[list.size()]);
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
