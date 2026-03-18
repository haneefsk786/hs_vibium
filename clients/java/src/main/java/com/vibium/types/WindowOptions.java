package com.vibium.types;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Options for setting window size/position.
 */
public class WindowOptions {
    private Integer width;
    private Integer height;
    private Integer x;
    private Integer y;
    private String state;

    public WindowOptions width(int width) { this.width = width; return this; }
    public WindowOptions height(int height) { this.height = height; return this; }
    public WindowOptions x(int x) { this.x = x; return this; }
    public WindowOptions y(int y) { this.y = y; return this; }
    public WindowOptions state(String state) { this.state = state; return this; }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        if (width != null) params.put("width", width);
        if (height != null) params.put("height", height);
        if (x != null) params.put("x", x);
        if (y != null) params.put("y", y);
        if (state != null) params.put("state", state);
        return params;
    }
}
