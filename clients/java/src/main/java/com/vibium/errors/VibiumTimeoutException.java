package com.vibium.errors;

/**
 * Thrown when an element wait or waitForFunction times out.
 */
public class VibiumTimeoutException extends VibiumException {
    public VibiumTimeoutException(String message) {
        super(message);
    }

    public VibiumTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
