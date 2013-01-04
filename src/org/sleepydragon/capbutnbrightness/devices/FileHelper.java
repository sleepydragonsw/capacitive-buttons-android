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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.sleepydragon.capbutnbrightness.devices.CapacitiveButtonsBacklightBrightness.SetException;

/**
 * A helper class for performing file operations in
 * CapacitiveButtonsBacklightBrightness implementations.
 */
public class FileHelper {

    /**
     * Private constructor to prevent instantiation.
     */
    private FileHelper() {
    }

    /**
     * Tests whether or not a file exists.
     *
     * @param path the path of the file to test for existence.
     * @return true if a file with the given path exists, false if not.
     * @throws NullPointerException if path==null.
     */
    public static boolean fileExists(String path) {
        if (path == null) {
            throw new NullPointerException("path==null");
        }

        final File file = new File(path);
        final boolean exists = file.exists();
        return exists;
    }

    /**
     * Reads the contents of a file and parses them as an integer.
     *
     * @param path the path of the file from which to read.
     * @return the integer parsed from the file's contents; returns null if
     * parsing the file's contents as an integer failed.
     * @throws SetException if reading the file's contents fails.
     * @throws NullPointerException if path==null.
     */
    public static Integer readIntFromFile(String path) throws SetException {
        if (path == null) {
            throw new NullPointerException("path==null");
        }

        // read the contents of the file into a byte array
        final byte[] data = new byte[1024];
        final int numBytesRead;
        try {
            final FileInputStream in = new FileInputStream(path);
            try {
                numBytesRead = in.read(data);
            } finally {
                in.close();
            }
        } catch (final IOException e) {
            throw new SetException("unable to read from file: " + path + " ("
                + e.getMessage() + ")");
        }

        // parse the contents of the file as an integer
        Integer dataInt;
        if (numBytesRead < 0) {
            dataInt = null; // file is empty; therefore cannot be parsed
        } else {
            final String dataStr = new String(data, 0, numBytesRead).trim();
            try {
                dataInt = Integer.parseInt(dataStr);
            } catch (final NumberFormatException e) {
                dataInt = null;
            }
        }

        return dataInt;
    }

    /**
     * Tests whether or not a file exists. If a file with the given path exists
     * then this method returns normally. Otherwise, it throws SetException.
     *
     * @param path the path of the file to test for existence.
     * @throws SetException if the file with the given path does not exist.
     * @throws NullPointerException if path==null.
     */
    public static void verifyFileExists(String path) throws SetException {
        if (path == null) {
            throw new NullPointerException("path==null");
        }
        final boolean exists = fileExists(path);
        if (!exists) {
            throw new SetException("file not found: " + path);
        }
    }

    /**
     * Writes an integer followed by a newline to a file.
     *
     * @param value the integer value to convert to a string and write.
     * @param path the path of the file to which to write.
     * @throws SetException if writing to the file fails.
     * @throws NullPointerException if path==null.
     */
    public static void writeToFile(int value, String path) throws SetException {
        if (path == null) {
            throw new NullPointerException("path==null");
        }

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
}
