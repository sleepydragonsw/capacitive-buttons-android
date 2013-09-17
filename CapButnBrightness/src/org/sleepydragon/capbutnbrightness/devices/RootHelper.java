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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.sleepydragon.capbutnbrightness.devices.CapacitiveButtonsBacklightBrightness.SetException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;

/**
 * A helper class for performing operations as root in
 * CapacitiveButtonsBacklightBrightness implementations.
 */
public class RootHelper {

    /**
     * Private constructor to prevent instantiation.
     */
    private RootHelper() {
    }

    /**
     * Changes the permissions of a file using the chmod command.
     *
     * @param permissions the permissions to set (eg. 644).
     * @param path the path of the file whose permissions to set.
     * @throws SetException if executing the command fails.
     * @throws NullPointerException if any argument is null.
     */
    public static void chmod(String permissions, String path)
            throws SetException {
        if (permissions == null) {
            throw new NullPointerException("permissions==null");
        } else if (path == null) {
            throw new NullPointerException("path==null");
        }
        final String command = "chmod " + permissions + " " + path;
        runAsRoot(command);
    }

    /**
     * Runs a command as root.
     *
     * @param command the command to execute, such as would be typed in a shell
     * (eg. "chmod 644 hello.txt").
     * @throws SetException if executing the command fails.
     * @throws NullPointerException if command==null.
     */
    public static void runAsRoot(String command) throws SetException {
        if (command == null) {
            throw new NullPointerException("command==null");
        }

        // get a root shell
        final Shell shell;
        try {
            shell = RootTools.getShell(true);
        } catch (final TimeoutException e) {
            throw new SetException("timeout waiting for root shell: "
                + e.getMessage());
        } catch (final RootDeniedException e) {
            throw new SetException("root access denied: " + e.getMessage());
        } catch (final IOException e) {
            throw new SetException("unable to get root shell: "
                + e.getMessage());
        }

        // start the command
        final Command commandObj = new CommandNoCapture(1, command);
        try {
            shell.add(commandObj);
        } catch (final IOException e) {
            throw new SetException("unable to run command as root: " + command
                + " (" + e.getMessage() + ")");
        }

        // wait for the command to complete
        try {
            commandObj.waitForFinish();
        } catch (final InterruptedException e) {
            // oh well?
        }
    }

    /**
     * Tests whether or not this application is granted permissions to run
     * commands as root. This method may prompt the user and, if it does, will
     * block waiting for the user's response.
     * <p>
     * If permission is granted then this method returns normally. Otherwise, it
     * throws RootAccessDeniedException.
     *
     * @throws RootAccessDeniedException if root access was denied.
     * @see #verifyRooted()
     */
    public static void verifyRootAccessGranted()
            throws RootAccessDeniedException {
        final boolean isAccessGranted = RootTools.isAccessGiven();
        if (!isAccessGranted) {
            throw new RootAccessDeniedException("root access was denied");
        }
    }

    /**
     * Tests whether or not the device on which this application is running is
     * rooted. This method will not prompt the user and therefore will return
     * quickly. Note that this method does not test whether or not this
     * application has been granted root permissions, but only tests if the
     * device is rooted and even capable of granting root access.
     * <p>
     * If the device is rooted then this method returns normally. Otherwise, it
     * throws NotRootedException.
     *
     * @throws NotRootedException if the device is not rooted.
     * @see #verifyRootAccessGranted()
     */
    public static void verifyRooted() throws NotRootedException {
        final boolean isRootAvailable = RootTools.isRootAvailable();
        if (!isRootAvailable) {
            throw new NotRootedException("device is not rooted");
        }
    }

    /**
     * Helper class used when running commands as root to simply discard any
     * output written to stdout or stderr by the command.
     */
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
