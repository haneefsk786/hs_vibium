package com.vibium;

import com.google.gson.JsonObject;
import com.vibium.internal.BiDiClient;
import com.vibium.types.MouseOptions;

import com.google.gson.Gson;

/**
 * Page-level mouse input.
 */
public class Mouse {

    private static final Gson GSON = new Gson();

    private final BiDiClient client;
    private final String contextId;

    Mouse(BiDiClient client, String contextId) {
        this.client = client;
        this.contextId = contextId;
    }

    /** Click at coordinates. */
    public void click(double x, double y) {
        click(x, y, null);
    }

    /** Click at coordinates with options. */
    public void click(double x, double y, MouseOptions options) {
        JsonObject params = params();
        params.addProperty("x", x);
        params.addProperty("y", y);
        mergeOptions(params, options);
        client.send("vibium:mouse.click", params);
    }

    /** Move mouse to coordinates. */
    public void move(double x, double y) {
        move(x, y, null);
    }

    /** Move mouse to coordinates with options. */
    public void move(double x, double y, MouseOptions options) {
        JsonObject params = params();
        params.addProperty("x", x);
        params.addProperty("y", y);
        mergeOptions(params, options);
        client.send("vibium:mouse.move", params);
    }

    /** Press mouse button down. */
    public void down() {
        down(null);
    }

    /** Press mouse button down with options. */
    public void down(MouseOptions options) {
        JsonObject params = params();
        mergeOptions(params, options);
        client.send("vibium:mouse.down", params);
    }

    /** Release mouse button. */
    public void up() {
        up(null);
    }

    /** Release mouse button with options. */
    public void up(MouseOptions options) {
        JsonObject params = params();
        mergeOptions(params, options);
        client.send("vibium:mouse.up", params);
    }

    /** Scroll the mouse wheel. */
    public void wheel(double deltaX, double deltaY) {
        JsonObject params = params();
        params.addProperty("deltaX", deltaX);
        params.addProperty("deltaY", deltaY);
        client.send("vibium:mouse.wheel", params);
    }

    private JsonObject params() {
        JsonObject p = new JsonObject();
        p.addProperty("context", contextId);
        return p;
    }

    private void mergeOptions(JsonObject params, MouseOptions options) {
        if (options == null) return;
        for (java.util.Map.Entry<String, Object> entry : options.toParams().entrySet()) {
            params.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
        }
    }
}
