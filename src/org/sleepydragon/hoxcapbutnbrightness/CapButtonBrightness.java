package org.sleepydragon.hoxcapbutnbrightness;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

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
     */
    public static void set(Level level) throws SetCapButtonBrightnessException,
            InterruptedException {
        final int current = getCurrent(level);
        final String commandStr = getSetBrightnessCommand(current);
        final Shell shell;
        try {
            shell = RootTools.getShell(true);
        } catch (TimeoutException e) {
            throw new SetCapButtonBrightnessException("unable to get root "
                + "shell: timeout expired: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new SetCapButtonBrightnessException("unable to get root "
                + "shell: IO error: " + e.getMessage(), e);
        }

        final Command command = new CommandNoCapture(1, commandStr);
        try {
            shell.add(command);
        } catch (IOException e) {
            throw new SetCapButtonBrightnessException("running command as "
                + "root failed: " + e.getMessage(), e);
        }

        command.wait();
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
