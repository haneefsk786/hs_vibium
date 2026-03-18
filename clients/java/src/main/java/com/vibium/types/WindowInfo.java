package com.vibium.types;

/**
 * Browser window info (size, position, state).
 */
public class WindowInfo {
    private String state;
    private int width;
    private int height;
    private int x;
    private int y;

    public String state() { return state; }
    public int width() { return width; }
    public int height() { return height; }
    public int x() { return x; }
    public int y() { return y; }

    @Override
    public String toString() {
        return "WindowInfo{state='" + state + "', width=" + width + ", height=" + height + "}";
    }
}
