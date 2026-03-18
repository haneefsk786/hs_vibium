package com.vibium.types;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Options for mouse operations.
 */
public class MouseOptions {
    private String button;
    private Integer clickCount;

    public MouseOptions button(String button) { this.button = button; return this; }
    public MouseOptions clickCount(int clickCount) { this.clickCount = clickCount; return this; }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        if (button != null) params.put("button", button);
        if (clickCount != null) params.put("clickCount", clickCount);
        return params;
    }
}
