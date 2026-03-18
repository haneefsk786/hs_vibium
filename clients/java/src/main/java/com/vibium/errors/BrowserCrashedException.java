package com.vibium.errors;

/**
 * Thrown when the browser process dies unexpectedly.
 */
public class BrowserCrashedException extends VibiumException {
    public BrowserCrashedException(String message) {
        super(message);
    }

    public BrowserCrashedException(String message, Throwable cause) {
        super(message, cause);
    }
}
