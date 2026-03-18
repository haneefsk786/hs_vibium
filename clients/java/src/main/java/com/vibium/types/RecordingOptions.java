package com.vibium.types;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Options for starting a recording.
 */
public class RecordingOptions {
    private String name;
    private Boolean screenshots;
    private Boolean snapshots;
    private Boolean sources;
    private String title;
    private Boolean bidi;
    private String format;
    private Double quality;

    public RecordingOptions name(String name) { this.name = name; return this; }
    public RecordingOptions screenshots(boolean screenshots) { this.screenshots = screenshots; return this; }
    public RecordingOptions snapshots(boolean snapshots) { this.snapshots = snapshots; return this; }
    public RecordingOptions sources(boolean sources) { this.sources = sources; return this; }
    public RecordingOptions title(String title) { this.title = title; return this; }
    public RecordingOptions bidi(boolean bidi) { this.bidi = bidi; return this; }
    public RecordingOptions format(String format) { this.format = format; return this; }
    public RecordingOptions quality(double quality) { this.quality = quality; return this; }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        if (name != null) params.put("name", name);
        if (screenshots != null) params.put("screenshots", screenshots);
        if (snapshots != null) params.put("snapshots", snapshots);
        if (sources != null) params.put("sources", sources);
        if (title != null) params.put("title", title);
        if (bidi != null) params.put("bidi", bidi);
        if (format != null) params.put("format", format);
        if (quality != null) params.put("quality", quality);
        return params;
    }
}
