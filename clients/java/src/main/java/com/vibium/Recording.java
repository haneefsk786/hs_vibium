package com.vibium;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vibium.internal.BiDiClient;
import com.vibium.types.ChunkOptions;
import com.vibium.types.RecordingOptions;

import java.util.Base64;
import java.util.Map;

/**
 * Trace recording control.
 */
public class Recording {

    private static final Gson GSON = new Gson();
    private final BiDiClient client;
    private final String userContextId;

    Recording(BiDiClient client, String userContextId) {
        this.client = client;
        this.userContextId = userContextId;
    }

    /** Start recording. */
    public void start() {
        start(null);
    }

    /** Start recording with options. */
    public void start(RecordingOptions options) {
        JsonObject params = params();
        if (options != null) {
            for (Map.Entry<String, Object> entry : options.toParams().entrySet()) {
                params.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
            }
        }
        client.send("vibium:recording.start", params);
    }

    /** Stop recording and return the trace data (ZIP). */
    public byte[] stop() {
        return stop(null);
    }

    /** Stop recording and save to path. Returns trace data (ZIP). */
    public byte[] stop(String path) {
        JsonObject params = params();
        if (path != null) {
            params.addProperty("path", path);
        }
        JsonObject result = client.send("vibium:recording.stop", params);
        if (result.has("data")) {
            return Base64.getDecoder().decode(result.get("data").getAsString());
        }
        return new byte[0];
    }

    /** Start a recording chunk. */
    public void startChunk() {
        startChunk(null);
    }

    /** Start a recording chunk with options. */
    public void startChunk(ChunkOptions options) {
        JsonObject params = params();
        if (options != null) {
            for (Map.Entry<String, Object> entry : options.toParams().entrySet()) {
                params.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
            }
        }
        client.send("vibium:recording.startChunk", params);
    }

    /** Stop a recording chunk and return trace data (ZIP). */
    public byte[] stopChunk() {
        return stopChunk(null);
    }

    /** Stop a recording chunk and save to path. */
    public byte[] stopChunk(String path) {
        JsonObject params = params();
        if (path != null) {
            params.addProperty("path", path);
        }
        JsonObject result = client.send("vibium:recording.stopChunk", params);
        if (result.has("data")) {
            return Base64.getDecoder().decode(result.get("data").getAsString());
        }
        return new byte[0];
    }

    /** Start a logical group. */
    public void startGroup(String name) {
        startGroup(name, null);
    }

    /** Start a logical group with location. */
    public void startGroup(String name, String location) {
        JsonObject params = params();
        params.addProperty("name", name);
        if (location != null) {
            params.addProperty("location", location);
        }
        client.send("vibium:recording.startGroup", params);
    }

    /** Stop the current logical group. */
    public void stopGroup() {
        client.send("vibium:recording.stopGroup", params());
    }

    private JsonObject params() {
        JsonObject p = new JsonObject();
        p.addProperty("userContext", userContextId);
        return p;
    }
}
