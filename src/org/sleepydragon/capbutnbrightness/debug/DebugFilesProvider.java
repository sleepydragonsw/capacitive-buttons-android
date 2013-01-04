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
package org.sleepydragon.capbutnbrightness.debug;

/**
 * Objects that implement this interface provide a list of file paths whose
 * contents affect their runtime behaviour.
 */
public interface DebugFilesProvider {

    /**
     * Returns the list of files that are relevant to the runtime behaviour of
     * this object.
     *
     * @return distinct FileInfo objects, each describing one of the files;
     * never returns null and no elements of the returned array will be null.
     */
    public FileInfo[] getDebugFiles();

    /**
     * Constants that indicate the type of data that the file is expected to
     * contain.
     */
    enum FileContents {

        /**
         * The contents of the file are expected to be parsed as a single base
         * 10 integer, encoded in US-ASCII.
         */
        INT,
    }

    /**
     * Information about a file.
     */
    public static class FileInfo {

        private final String path;
        private final FileContents contents;

        /**
         * Creates a new instance of FileInfo.
         *
         * @param path the path of the file.
         * @param contents the type of contents that are expected to be
         * contained in the file.
         * @throws NullPointerException if any argument is null.
         */
        public FileInfo(String path, FileContents contents) {
            if (path == null) {
                throw new NullPointerException("path==null");
            } else if (contents == null) {
                throw new NullPointerException("contents==null");
            }
            this.path = path;
            this.contents = contents;
        }

        /**
         * Returns the type of contents that are expected to be contained in the
         * file.
         *
         * @return the value for "contents" that was given to the constructor;
         * never returns null.
         */
        public FileContents getContents() {
            return this.contents;
        }

        /**
         * Returns the path of the file.
         *
         * @return the value for "path" that was given to the constructor; never
         * returns null.
         */
        public String getPath() {
            return this.path;
        }
    }

}
