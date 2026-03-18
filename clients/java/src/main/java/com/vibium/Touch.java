package com.vibium;

import com.google.gson.JsonObject;
import com.vibium.internal.BiDiClient;

/**
 * Page-level touch input.
 */
public class Touch {

    private final BiDiClient client;
    private final String contextId;

    Touch(BiDiClient client, String contextId) {
        this.client = client;
        this.contextId = contextId;
    }

    /** Tap at coordinates. */
    public void tap(double x, double y) {
        JsonObject params = new JsonObject();
        params.addProperty("context", contextId);
        params.addProperty("x", x);
        params.addProperty("y", y);
        client.send("vibium:touch.tap", params);
    }
}
