package com.vibium.types;

/**
 * Options for taking screenshots.
 */
public class ScreenshotOptions {
    private Boolean fullPage;
    private ClipRegion clip;

    public ScreenshotOptions fullPage(boolean fullPage) { this.fullPage = fullPage; return this; }
    public ScreenshotOptions clip(double x, double y, double width, double height) {
        this.clip = new ClipRegion(x, y, width, height);
        return this;
    }

    public Boolean fullPage() { return fullPage; }
    public ClipRegion clip() { return clip; }

    public static class ClipRegion {
        private final double x, y, width, height;

        public ClipRegion(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public double x() { return x; }
        public double y() { return y; }
        public double width() { return width; }
        public double height() { return height; }
    }
}
