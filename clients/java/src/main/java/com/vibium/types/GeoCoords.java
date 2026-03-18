package com.vibium.types;

/**
 * Geolocation coordinates.
 */
public class GeoCoords {
    private final double latitude;
    private final double longitude;
    private Double accuracy;

    public GeoCoords(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GeoCoords accuracy(double accuracy) { this.accuracy = accuracy; return this; }

    public double latitude() { return latitude; }
    public double longitude() { return longitude; }
    public Double accuracy() { return accuracy; }
}
