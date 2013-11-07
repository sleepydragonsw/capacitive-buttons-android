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
package org.sleepydragon.capbutnbrightness;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.TimeoutException;

import org.sleepydragon.capbutnbrightness.clib.CLib;
import org.sleepydragon.capbutnbrightness.clib.CLibConstants;
import org.sleepydragon.capbutnbrightness.clib.ClibException;
import org.sleepydragon.capbutnbrightness.clib.Stat;

import android.os.Process;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

/**
 * A helper class to work with files whose contents consist of an integer value
 * and are owned by root. The way this class works is that when a write is
 * attempted to the file, if that write operation fails due to access denied
 * then the ownership of the file is changed to the UID of the current process
 * and then the write operation is attempted again. It is important to call
 * {@link #close()} when done with instances of this class to ensure that all
 * resources that were acquired have been freed.
 */
public class IntFileRootHelper {

    /**
     * The root shell to use to perform superuser operations. Will initially be
     * null and will be assigned to non-null when the first root operation is
     * required. The same shell will then be re-used for all subsequent root
     * operations. The close() method will close this object and set the
     * reference back to null.
     */
    private Shell shell;

    /**
     * Object that gets notified of events.
     */
    private OperationNotifier notifier;

    /**
     * The object to be synchronized on when accessing nextCommandId.
     */
    private static final Object nextIdLock = new Object();

    /**
     * The ID to use for the next CommandCapture object. All access to this
     * value *must* be done while synchronized on nextIdLock.
     */
    private static int nextId;

    /**
     * Creates a new instance of IntFileRootHelper.
     *
     * @param notifier the object to get notified of events; may be null to not
     * send notifications about events.
     */
    public IntFileRootHelper(OperationNotifier notifier) {
        this.notifier = notifier;
    }

    /**
     * Releases all resources acquired by this object. This method should be
     * invoked when this object is no longer needed.
     */
    public void close() {
        final Shell shell = this.shell;
        if (shell != null) {
            this.shell = null;
            try {
                shell.close();
            } catch (final IOException e) {
                // oh well
            }
        }
    }

    /**
     * Invokes {@link #close()}.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            this.close();
        } finally {
            super.finalize();
        }
    }

    private Shell getOrCreateRootShell() throws RootShellCreateException {
        Shell shell = this.shell;
        if (shell == null) {
            final OperationNotifier notifier = this.notifier;
            if (notifier != null) {
                notifier.rootRequestStarted();
            }
            try {
                shell = RootTools.getShell(true, 60000);
            } catch (final IOException e) {
                if (!RootTools.isRootAvailable()) {
                    throw new RootShellNotRootedException(
                        "device needs to be rooted");
                } else {
                    throw new RootShellCreateIOException(e.getMessage());
                }
            } catch (final TimeoutException e) {
                throw new RootShellCreateTimeoutException(e.getMessage());
            } catch (final RootDeniedException e) {
                throw new RootShellCreateDeniedException(e.getMessage());
            } finally {
                if (notifier != null) {
                    notifier.rootRequestCompleted();
                }
            }
        }
        assert shell != null;
        this.shell = shell;
        return shell;
    }

    /**
     * Changes the UID and permissions of a file so that the OS cannot change
     * its value. This method basically does the same thing as
     * {@link #write(String, int)} without actually writing to the file.
     *
     * @param path the path of the file to which to write.
     * @throws IntWriteException if an error occurs changing the permissions or
     * UID of the file, such as requesting root permissions or getting the UID
     * of the file.
     * @throws NullPointerException if path==null.
     */
    public void protectFileFromOs(String path) throws IntWriteException {
        if (path == null) {
            throw new NullPointerException("path==null");
        }

        // verify that the file exists
        final File file = new File(path);
        if (!file.exists()) {
            throw new IntFileNotFoundException("file not found: " + path, path);
        }

        // ensure that the UID of the file is equal to the UID of the process;
        // if they are different, then attempt to change the UID of the file
        final int processUid = Process.myUid();
        final long fileUid = getFileUID(path);
        if (processUid != fileUid) {
            final Shell shell = this.getOrCreateRootShell();
            setFileUID(path, processUid, shell);
        }

        makeReadOnly(path);
    }

    /**
     * Reads the contents of a file as an integer.
     *
     * @param path the path of the file to read.
     * @return the integer value read from the file with the given path.
     * @throws IOException if an error occurs reading from the file.
     * @throws FileNotFoundException if the file does not exist.
     * @throws IntParseException if the contents of the file failed to be parsed
     * an an integer.
     * @throws NullPointerException if path==null.
     */
    public int read(String path) throws IOException, FileNotFoundException,
            IntParseException {
        if (path == null) {
            throw new NullPointerException("path==null");
        }

        // read bytes from the file
        final byte[] buffer = new byte[128];
        final int bufferLength = readBytesFromFile(path, buffer);

        // decode the bytes from the file into a string
        final String valueStr = decodeStr(buffer, bufferLength);
        final String valueStrTrimmed = valueStr.trim();

        // parse an integer from the string read from the file
        final int value;
        try {
            value = Integer.parseInt(valueStrTrimmed);
        } catch (final NumberFormatException e) {
            throw new IntParseException(valueStr);
        }

        return value;
    }

    /**
     * Writes an integer to a file as its entire contents. If the file is owned
     * by a user other than the UID of the current process then first chown is
     * run as root to change the owner to the current process. Then, its
     * permissions are changed to read-write for the user and read-only for the
     * rest. Then the integer is written to the file. Then the file's
     * permissions are all changed to read-only.
     *
     * @param path the path of the file to which to write.
     * @param value the integer value to write to the file.
     * @throws IntWriteException if an error occurs writing to the file that is
     * not related to the write operation itself, such as requesting root
     * permissions or getting the UID of the file.
     * @throws NullPointerException if path==null.
     */
    public void write(String path, int value) throws IntWriteException {
        if (path == null) {
            throw new NullPointerException("path==null");
        }

        // verify that the file exists
        final File file = new File(path);
        if (!file.exists()) {
            throw new IntFileNotFoundException("file not found: " + path, path);
        }

        // ensure that the UID of the file is equal to the UID of the process;
        // if they are different, then attempt to change the UID of the file
        final int processUid = Process.myUid();
        final long fileUid = getFileUID(path);
        if (processUid != fileUid) {
            final Shell shell = this.getOrCreateRootShell();
            setFileUID(path, processUid, shell);
        }

        // set the permissions of the file such that it is user writable
        makeWritable(path);

        try {
            // write the integer value to the file
            writeIntToFile(path, value);
        } finally {
            makeReadOnly(path);
        }
    }

    private static void chmod(String path, int mode)
            throws ChmodFailedException {
        assert path != null;
        try {
            CLib.chmod(path, mode);
        } catch (final ClibException e) {
            final String message = e.getMessage();
            final int errno = e.getErrno();
            throw new ChmodFailedException(message, errno, path);
        }
    }

    private static String decodeStr(byte[] buffer, int bufferLength) {
        if (bufferLength == -1) {
            return "";
        }
        final Charset charset = Charset.forName("US-ASCII");
        final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bufferLength);
        final CharBuffer charBuffer = charset.decode(byteBuffer);
        final String s = charBuffer.toString();
        return s;
    }

    private static long getFileUID(String path) throws StatFailedException {
        final Stat stat = new Stat();
        try {
            CLib.stat(path, stat);
        } catch (final ClibException e) {
            final int errno = e.getErrno();
            final String message = e.getMessage();
            throw new StatFailedException(message, errno, path);
        }
        final long fileUid = stat.getUid();
        return fileUid;
    }

    private static int getNextId() {
        final int id;
        synchronized (nextIdLock) {
            id = nextId++;
        }
        return id;
    }

    private static void makeReadOnly(String path) throws ChmodFailedException {
        final int mode =
            CLibConstants.S_IRUSR | CLibConstants.S_IRGRP
                | CLibConstants.S_IROTH;
        chmod(path, mode);
    }

    /**
     * Changes the permissions of a file to be user-writable.
     *
     * @param path the path of the file whose permissions to change.
     * @throws ChmodFailedException if changing the permissions fails.
     */
    public static void makeWritable(String path) throws ChmodFailedException {
        final int mode =
            CLibConstants.S_IRUSR | CLibConstants.S_IWUSR
                | CLibConstants.S_IRGRP | CLibConstants.S_IROTH;
        chmod(path, mode);
    }

    private static int readBytesFromFile(String path, byte[] buffer)
            throws IOException, FileNotFoundException {
        final InputStream f = new FileInputStream(path);
        try {
            return f.read(buffer);
        } finally {
            try {
                f.close();
            } catch (final IOException e) {
                // oh well
            }
        }
    }

    private static void setFileUID(String path, int uid, Shell shell)
            throws ChownException {
        assert path != null;
        assert shell != null;

        // prepare to launch the chown command
        final String commandStr = "chown " + uid + " " + path;
        final int commandId = getNextId();
        final Command command = new CommandCapture(commandId, commandStr);

        // launch the chown command
        try {
            shell.add(command);
        } catch (final IOException e) {
            throw new ChownLaunchException(e.getMessage(), path);
        }

        // wait for the chown command to complete
        final int exitCode;
        try {
            command.waitForFinish();
            exitCode = command.exitCode();
        } catch (final InterruptedException e) {
            throw new ChownWaitInterruptedException(e.getMessage(), path);
        }

        // verify that chown completed with exit code 0, which indicates success
        if (exitCode != 0) {
            final String output = command.toString();
            final String outputTrimmed;
            if (output == null) {
                outputTrimmed = null;
            } else {
                outputTrimmed = output.trim();
            }
            throw new ChownExitCodeException(outputTrimmed, exitCode, path);
        }
    }

    private static void writeIntToFile(String path, int value)
            throws IntFileWriteException {
        assert path != null;

        // convert the int value to an encoded byte array
        final String valueStr = Integer.toString(value) + "\n";
        final byte[] buffer;
        try {
            buffer = valueStr.getBytes("US-ASCII");
        } catch (final UnsupportedEncodingException e) {
            // should never happen since US-ASCII is a built-in encoding
            throw new RuntimeException(e.toString());
        }

        // write the encoded byte array to the file
        final FileOutputStream f;
        try {
            f = new FileOutputStream(path);
        } catch (final FileNotFoundException e) {
            throw new IntFileNotFoundException(e.getMessage(), path);
        }

        try {
            f.write(buffer);
        } catch (final IOException e) {
            throw new IntFileIOException(e.getMessage(), path);
        } finally {
            try {
                f.close();
            } catch (final IOException e) {
                throw new IntFileIOException(e.getMessage(), path);
            }
        }
    }

    /**
     * Exception thrown if the chmod function call on a file fails.
     */
    public static class ChmodFailedException extends IntFileWriteException {

        private static final long serialVersionUID = -8951920083086526439L;
        private final int errno;

        /**
         * Creates a new instance of ChmodFailedException.
         *
         * @param message the message of this exception.
         * @param errno the errno value returned by the chmod function call.
         * @param path the path of the file whose chmod command failed.
         */
        public ChmodFailedException(String message, int errno, String path) {
            super(message, path);
            this.errno = errno;
        }

        /**
         * Returns the errno that was specified to the constructor.
         *
         * @return the errno that was specified to the constructor.
         */
        public int getErrno() {
            return this.errno;
        }
    }

    /**
     * Exception thrown if running the chown command fails.
     */
    public static class ChownException extends IntFileWriteException {

        private static final long serialVersionUID = -3070608061783729338L;

        /**
         * Creates a new instance of ChownException.
         *
         * @param message the message of this exception.
         * @param path the path of the file whose chown operation failed.
         */
        public ChownException(String message, String path) {
            super(message, path);
        }
    }

    /**
     * Exception thrown if the chown process completes with a non-zero exit
     * code.
     */
    public static class ChownExitCodeException extends ChownException {

        private static final long serialVersionUID = 3670736928898908905L;
        final int exitCode;

        /**
         * Creates a new instance of ChownExitCodeException.
         *
         * @param message the message of this exception.
         * @param exitCode the exit code with which the chown process
         * terminated.
         * @param path the path of the file whose chown operation failed.
         */
        public ChownExitCodeException(String message, int exitCode, String path) {
            super(message, path);
            this.exitCode = exitCode;
        }

        /**
         * Returns the exit code of the chown process.
         *
         * @return the exit code of the chown process that was specified to the
         * constructor.
         */
        public int getExitCode() {
            return this.exitCode;
        }
    }

    /**
     * Exception thrown if launching the chown process fails.
     */
    public static class ChownLaunchException extends ChownException {

        private static final long serialVersionUID = 2480011255506747546L;

        /**
         * Creates a new instance of ChownLaunchException.
         *
         * @param message the message of this exception.
         * @param path the path of the file whose chown operation failed.
         */
        public ChownLaunchException(String message, String path) {
            super(message, path);
        }
    }

    /**
     * Exception thrown if waiting for the chown process to complete is
     * interrupted.
     */
    public static class ChownWaitInterruptedException extends ChownException {

        private static final long serialVersionUID = 6877215266684541951L;

        /**
         * Creates a new instance of ChownWaitInterruptedException.
         *
         * @param message the message of this exception.
         * @param path the path of the file whose chown operation failed.
         */
        public ChownWaitInterruptedException(String message, String path) {
            super(message, path);
        }
    }

    /**
     * Exception thrown if an I/O operation an the int file occurs.
     */
    public static class IntFileIOException extends IntFileWriteException {

        private static final long serialVersionUID = -7277389637081910165L;

        /**
         * Creates a new instance of IntFileIOException.
         *
         * @param message the message of this exception.
         * @param path the path of the file that the IO error occured on.
         */
        public IntFileIOException(String message, String path) {
            super(message, path);
        }
    }

    /**
     * Exception thrown if the int file does not exist.
     */
    public static class IntFileNotFoundException extends IntFileWriteException {

        private static final long serialVersionUID = 786239249985919287L;

        /**
         * Creates a new instance of IntFileNotFoundException.
         *
         * @param message the message of this exception.
         * @param path the path of the file that does not exist.
         */
        public IntFileNotFoundException(String message, String path) {
            super(message, path);
        }
    }

    /**
     * Exception thrown if writing an integer to the file fails.
     */
    public static class IntFileWriteException extends IntWriteException {

        private static final long serialVersionUID = 8631683721974214363L;
        private final String path;

        /**
         * Creates a new instance of IntWriteException.
         *
         * @param message a message about the exception.
         * @param path the path of the file on which the operation failed.
         */
        public IntFileWriteException(String message, String path) {
            super(message);
            this.path = path;
        }

        /**
         * Returns the path of the file on which the operation failed.
         *
         * @return the path of the file on which the operation failed.
         */
        public String getPath() {
            return this.path;
        }

    }

    /**
     * Exception thrown if parsing the integer read from a file fails.
     */
    public static class IntParseException extends Exception {

        private static final long serialVersionUID = -4080928575561288998L;
        private final String s;

        /**
         * Creates a new instance of IntParseException.
         *
         * @param s the string that failed to be parsed as an integer.
         */
        public IntParseException(String s) {
            super("invalid integer: " + s);
            this.s = s;
        }

        /**
         * Returns the string that failed to be parsed as an integer.
         *
         * @return the string that failed to be parsed as an integer; returns
         * the exact string that was specified to the constructor, which may be
         * null.
         */
        public String getString() {
            return this.s;
        }
    }

    /**
     * Exception thrown if writing an integer to the file fails.
     */
    public static class IntWriteException extends Exception {

        private static final long serialVersionUID = 8631683721974214363L;

        /**
         * Creates a new instance of IntWriteException.
         *
         * @param message a message about the exception.
         */
        public IntWriteException(String message) {
            super(message);
        }

    }

    /**
     * Exception thrown if creating a root shell fails due to root access being
     * denied.
     */
    public static class RootShellCreateDeniedException extends
            RootShellCreateException {

        private static final long serialVersionUID = -6266688246326467761L;

        /**
         * Creates a new instance of RootShellCreateDeniedException.
         *
         * @param message the message of this exception.
         */
        public RootShellCreateDeniedException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown if creating a root shell fails.
     */
    public static class RootShellCreateException extends IntWriteException {

        private static final long serialVersionUID = -2921895935303932818L;

        /**
         * Creates a new instance of RootShellCreateException.
         *
         * @param message the message of this exception.
         */
        public RootShellCreateException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown if creating a root shell fails due to an I/O error.
     */
    public static class RootShellCreateIOException extends
            RootShellCreateException {

        private static final long serialVersionUID = -1324674354737498915L;

        /**
         * Creates a new instance of RootShellCreateIOException.
         *
         * @param message the message of this exception.
         */
        public RootShellCreateIOException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown if creating a root shell fails due to a timeout.
     */
    public static class RootShellCreateTimeoutException extends
            RootShellCreateException {

        private static final long serialVersionUID = -4808765149922308927L;

        /**
         * Creates a new instance of RootShellCreateTimeoutException.
         *
         * @param message the message of this exception.
         */
        public RootShellCreateTimeoutException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown if creating a root shell fails because the device is not
     * rooted.
     */
    public static class RootShellNotRootedException extends
            RootShellCreateException {

        private static final long serialVersionUID = -890421064967668858L;

        /**
         * Creates a new instance of RootShellNotRootedException.
         *
         * @param message the message of this exception.
         */
        public RootShellNotRootedException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown if the stat function call on a file fails.
     */
    public static class StatFailedException extends IntFileWriteException {

        private static final long serialVersionUID = 3185397095332788650L;
        private final int errno;

        /**
         * Creates a new instance of StatFailedException.
         *
         * @param message the message of this exception.
         * @param errno the errno value returned by the stat function call.
         * @param path the path of the file whose stat call failed.
         */
        public StatFailedException(String message, int errno, String path) {
            super(message, path);
            this.errno = errno;
        }

        /**
         * Returns the errno that was specified to the constructor.
         *
         * @return the errno that was specified to the constructor.
         */
        public int getErrno() {
            return this.errno;
        }
    }

    /**
     * Interface that can be implemented to be notified when events occur.
     */
    public static interface OperationNotifier {

        /**
         * Invoked when a request for root privileges starts.
         */
        public void rootRequestStarted();

        /**
         * Invoked when a request for root privileges completes.
         */
        public void rootRequestCompleted();

    }
}
