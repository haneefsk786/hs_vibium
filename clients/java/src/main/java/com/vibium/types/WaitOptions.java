package com.vibium.types;

/**
 * Options for wait operations.
 */
public class WaitOptions {
    private Long timeout;

    public WaitOptions timeout(long timeout) { this.timeout = timeout; return this; }
    public Long timeout() { return timeout; }
}
