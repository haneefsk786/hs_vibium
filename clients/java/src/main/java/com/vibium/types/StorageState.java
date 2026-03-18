package com.vibium.types;

import java.util.List;

/**
 * Browser storage state (cookies + origin storage).
 */
public class StorageState {
    private List<Cookie> cookies;
    private List<OriginState> origins;

    public List<Cookie> cookies() { return cookies; }
    public List<OriginState> origins() { return origins; }
}
