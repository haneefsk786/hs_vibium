package com.vibium.types;

/**
 * Browser viewport dimensions.
 */
public class ViewportSize {
    private int width;
    private int height;

    public ViewportSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() { return width; }
    public int height() { return height; }

    @Override
    public String toString() {
        return "ViewportSize{width=" + width + ", height=" + height + "}";
    }
}
