package com.vibium;

import com.google.gson.JsonObject;
import com.vibium.internal.BiDiClient;

/**
 * Page-level keyboard input.
 */
public class Keyboard {

    private final BiDiClient client;
    private final String contextId;

    Keyboard(BiDiClient client, String contextId) {
        this.client = client;
        this.contextId = contextId;
    }

    /** Press a key (down + up). */
    public void press(String key) {
        JsonObject params = params();
        params.addProperty("key", key);
        client.send("vibium:keyboard.press", params);
    }

    /** Hold a key down. */
    public void down(String key) {
        JsonObject params = params();
        params.addProperty("key", key);
        client.send("vibium:keyboard.down", params);
    }

    /** Release a key. */
    public void up(String key) {
        JsonObject params = params();
        params.addProperty("key", key);
        client.send("vibium:keyboard.up", params);
    }

    /** Type text character by character. */
    public void type(String text) {
        JsonObject params = params();
        params.addProperty("text", text);
        client.send("vibium:keyboard.type", params);
    }

    private JsonObject params() {
        JsonObject p = new JsonObject();
        p.addProperty("context", contextId);
        return p;
    }
}
