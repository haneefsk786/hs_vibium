package com.vibium.types;

/**
 * Basic info about a found element.
 */
public class ElementInfo {
    private final String tag;
    private final String text;
    private final BoundingBox box;

    public ElementInfo(String tag, String text, BoundingBox box) {
        this.tag = tag;
        this.text = text;
        this.box = box;
    }

    public String tag() { return tag; }
    public String text() { return text; }
    public BoundingBox box() { return box; }

    @Override
    public String toString() {
        return "ElementInfo{tag='" + tag + "', text='" + text + "', box=" + box + "}";
    }
}
