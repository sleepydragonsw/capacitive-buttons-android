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

/**
 * Thrown if setting the capacitive button brightness fails.
 */
public class SetCapButtonBrightnessException extends Exception {

    private static final long serialVersionUID = -3591539697444796075L;

    public enum ErrorId {

        /**
         * Error indicating that the "su" binary is missing, and therefore that
         * the device is not rooted.
         */
        SU_BINARY_MISSING,

        /**
         * Error indicating that the request for root permissions was denied.
         */
        ROOT_ACCESS_DENIED,

        /**
         * Error indicating that a timeout occurred waiting for root permissions
         * to be granted or the command to finish.
         */
        ROOT_ACCESS_TIMEOUT,

        /**
         * Error indicating that an IOException occurred while running the
         * command as root.
         */
        ROOT_ACCESS_IOEXCEPTION,
    }

    private final ErrorId errorId;

    public SetCapButtonBrightnessException(ErrorId errorId, String message,
            Throwable cause) {
        super(message, cause);
        this.errorId = errorId;
    }

    public ErrorId getErrorId() {
        return this.errorId;
    }

}
