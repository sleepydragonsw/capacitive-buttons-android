/*
 * This file is part of Set Capacitive Button Brightness for HTC One X.
 * 
 * Set Capacitive Button Brightness for HTC One X is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Set Capacitive Button Brightness for HTC One X is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Set Capacitive Button Brightness for HTC One X.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.sleepydragon.hoxcapbutnbrightness;

/**
 * Thrown if setting the capacitive button brightness fails.
 */
public class SetCapButtonBrightnessException extends Exception {
    private static final long serialVersionUID = -3591539697444796075L;
    public SetCapButtonBrightnessException(String message, Throwable cause) {
        super(message, cause);
    }
}
