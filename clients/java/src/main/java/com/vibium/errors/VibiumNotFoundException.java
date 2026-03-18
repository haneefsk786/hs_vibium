package com.vibium.errors;

/**
 * Thrown when the vibium binary cannot be found on the system.
 */
public class VibiumNotFoundException extends VibiumException {
    public VibiumNotFoundException(String message) {
        super(message);
    }
}
