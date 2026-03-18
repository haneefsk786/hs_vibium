package com.vibium.types;

/**
 * Source code location (for console messages).
 */
public class SourceLocation {
    private String url;
    private int lineNumber;
    private int columnNumber;

    public SourceLocation(String url, int lineNumber, int columnNumber) {
        this.url = url;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public String url() { return url; }
    public int lineNumber() { return lineNumber; }
    public int columnNumber() { return columnNumber; }

    @Override
    public String toString() {
        return url + ":" + lineNumber + ":" + columnNumber;
    }
}
