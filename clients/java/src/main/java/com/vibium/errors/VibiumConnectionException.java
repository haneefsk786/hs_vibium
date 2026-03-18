package com.vibium.errors;

/**
 * Thrown when the pipe connection to the vibium binary fails.
 */
public class VibiumConnectionException extends VibiumException {
    public VibiumConnectionException(String message) {
        super(message);
    }

    public VibiumConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
