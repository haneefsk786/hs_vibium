package com.vibium;

import com.google.gson.JsonObject;
import com.vibium.internal.BiDiClient;
import com.vibium.types.ClockOptions;

import java.util.Map;

/**
 * Fake timer / Date control.
 */
public class Clock {

    private final BiDiClient client;
    private final String contextId;

    Clock(BiDiClient client, String contextId) {
        this.client = client;
        this.contextId = contextId;
    }

    /** Install fake timers. */
    public void install() {
        install(null);
    }

    /** Install fake timers with options. */
    public void install(ClockOptions options) {
        JsonObject params = params();
        if (options != null) {
            for (Map.Entry<String, Object> entry : options.toParams().entrySet()) {
                // ClockOptions values are all strings
                params.addProperty(entry.getKey(), (String) entry.getValue());
            }
        }
        client.send("vibium:clock.install", params);
    }

    /** Fast-forward time by milliseconds. */
    public void fastForward(long ticks) {
        JsonObject params = params();
        params.addProperty("ticks", ticks);
        client.send("vibium:clock.fastForward", params);
    }

    /** Run timers for a duration in milliseconds. */
    public void runFor(long ticks) {
        JsonObject params = params();
        params.addProperty("ticks", ticks);
        client.send("vibium:clock.runFor", params);
    }

    /** Pause the clock at a specific time. */
    public void pauseAt(String time) {
        JsonObject params = params();
        params.addProperty("time", time);
        client.send("vibium:clock.pauseAt", params);
    }

    /** Resume the clock. */
    public void resume() {
        client.send("vibium:clock.resume", params());
    }

    /** Set fixed fake time. */
    public void setFixedTime(String time) {
        JsonObject params = params();
        params.addProperty("time", time);
        client.send("vibium:clock.setFixedTime", params);
    }

    /** Set system time. */
    public void setSystemTime(String time) {
        JsonObject params = params();
        params.addProperty("time", time);
        client.send("vibium:clock.setSystemTime", params);
    }

    /** Set timezone. */
    public void setTimezone(String timezone) {
        JsonObject params = params();
        params.addProperty("timezone", timezone);
        client.send("vibium:clock.setTimezone", params);
    }

    private JsonObject params() {
        JsonObject p = new JsonObject();
        p.addProperty("context", contextId);
        return p;
    }
}
