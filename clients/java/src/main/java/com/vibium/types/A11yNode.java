package com.vibium.types;

import java.util.List;

/**
 * Accessibility tree node.
 */
public class A11yNode {
    private String role;
    private String name;
    private String value;
    private String description;
    private Boolean disabled;
    private Boolean expanded;
    private Boolean focused;
    private String checked;
    private Boolean pressed;
    private Boolean selected;
    private Integer level;
    private Boolean multiselectable;
    private List<A11yNode> children;

    public String role() { return role; }
    public String name() { return name; }
    public String value() { return value; }
    public String description() { return description; }
    public Boolean disabled() { return disabled; }
    public Boolean expanded() { return expanded; }
    public Boolean focused() { return focused; }
    public String checked() { return checked; }
    public Boolean pressed() { return pressed; }
    public Boolean selected() { return selected; }
    public Integer level() { return level; }
    public Boolean multiselectable() { return multiselectable; }
    public List<A11yNode> children() { return children; }

    @Override
    public String toString() {
        return "A11yNode{role='" + role + "', name='" + name + "'}";
    }
}
