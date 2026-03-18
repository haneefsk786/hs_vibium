package com.vibium.types;

import java.util.List;

/**
 * Storage state for a single origin.
 */
public class OriginState {
    private String origin;
    private List<StorageEntry> localStorage;
    private List<StorageEntry> sessionStorage;

    public String origin() { return origin; }
    public List<StorageEntry> localStorage() { return localStorage; }
    public List<StorageEntry> sessionStorage() { return sessionStorage; }

    public static class StorageEntry {
        private String name;
        private String value;

        public String name() { return name; }
        public String value() { return value; }
    }
}
