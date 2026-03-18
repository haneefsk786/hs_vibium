package com.vibium.types;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Options for getting the accessibility tree.
 */
public class A11yOptions {
    private Boolean interestingOnly;
    private String root;

    public A11yOptions interestingOnly(boolean interestingOnly) { this.interestingOnly = interestingOnly; return this; }
    public A11yOptions root(String root) { this.root = root; return this; }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        if (interestingOnly != null) params.put("interestingOnly", interestingOnly);
        if (root != null) params.put("root", root);
        return params;
    }
}
