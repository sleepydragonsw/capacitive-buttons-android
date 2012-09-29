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
