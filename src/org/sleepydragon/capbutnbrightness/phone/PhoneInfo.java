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
 * Information necessary for setting the capacitive button brightness on a
 * specific phone model.
 */
public abstract class PhoneInfo {

    private final String name;
    private final String path;
    private final int numLevels;

    /**
     * Creates a new instance of this class.
     *
     * @param name is a human-friendly name for this phone model.
     * @param path the path of the file to which the brightness level is
     * written.
     * @param numLevels the number of brightness levels supported by this phone.
     * @throws NullPointerException if any argument is null.
     */
    public PhoneInfo(String name, String path, int numLevels) {
        if (name == null) {
            throw new NullPointerException("name==null");
        } else if (path == null) {
            throw new NullPointerException("path==null");
        }
        this.name = name;
        this.path = path;
        this.numLevels = numLevels;
    }

    /**
     * Converts a brightness "level" to a value to write to the file returned
     * from getPath().
     *
     * @param level the level to convert.
     * @return the value to write to the file to set the brightness to the given
     * level.
     */
    public abstract int getLevelToWrite(Level level);

    /**
     * Returns a human-friendly name for this phone.
     *
     * @return the "name" that was specified to the constructor; never returns
     * null.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the number of brightness levels supported by this phone.
     *
     * @return the "numLevels" that was specified to the constructor.
     */
    public int getNumLevels() {
        return this.numLevels;
    }

    /**
     * Returns the path of the file to which the brightness level is written.
     *
     * @return the "path" that was specified to the constructor; never returns
     * null.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return returns exactly {@link #getName()}.
     */
    @Override
    public String toString() {
        return this.getName();
    }
}
