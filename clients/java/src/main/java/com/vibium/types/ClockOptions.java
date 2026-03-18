package com.vibium.types;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Options for installing fake timers.
 */
public class ClockOptions {
    private String time;
    private String timezone;

    public ClockOptions time(String time) { this.time = time; return this; }
    public ClockOptions timezone(String timezone) { this.timezone = timezone; return this; }

    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        if (time != null) params.put("time", time);
        if (timezone != null) params.put("timezone", timezone);
        return params;
    }
}
