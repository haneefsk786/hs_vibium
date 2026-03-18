package com.vibium.types;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Options for emulating CSS media features.
 */
public class MediaOptions {
    private String media;
    private String colorScheme;
    private String reducedMotion;
    private String forcedColors;
    private String contrast;

    public MediaOptions media(String media) { this.media = media; return this; }
    public MediaOptions colorScheme(String colorScheme) { this.colorScheme = colorScheme; return this; }
    public MediaOptions reducedMotion(String reducedMotion) { this.reducedMotion = reducedMotion; return this; }
    public MediaOptions forcedColors(String forcedColors) { this.forcedColors = forcedColors; return this; }
    public MediaOptions contrast(String contrast) { this.contrast = contrast; return this; }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        if (media != null) params.put("media", media);
        if (colorScheme != null) params.put("colorScheme", colorScheme);
        if (reducedMotion != null) params.put("reducedMotion", reducedMotion);
        if (forcedColors != null) params.put("forcedColors", forcedColors);
        if (contrast != null) params.put("contrast", contrast);
        return params;
    }
}
