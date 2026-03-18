package com.vibium.types;

/**
 * Options for find/waitFor operations.
 */
public class FindOptions {
    private Long timeout;

    public FindOptions timeout(long timeout) { this.timeout = timeout; return this; }
    public Long timeout() { return timeout; }
}
