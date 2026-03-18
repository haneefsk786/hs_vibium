package com.vibium.types;

/**
 * Bounding box of an element on screen.
 */
public class BoundingBox {
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public BoundingBox(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double width() { return width; }
    public double height() { return height; }

    @Override
    public String toString() {
        return "BoundingBox{x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "}";
    }
}
