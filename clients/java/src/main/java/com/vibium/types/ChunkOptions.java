package com.vibium.types;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Options for recording chunks.
 */
public class ChunkOptions {
    private String name;
    private String title;

    public ChunkOptions name(String name) { this.name = name; return this; }
    public ChunkOptions title(String title) { this.title = title; return this; }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        if (name != null) params.put("name", name);
        if (title != null) params.put("title", title);
        return params;
    }
}
