package com.vibium;

import com.google.gson.JsonObject;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * WebSocket connection info.
 */
public class WebSocketInfo {

    private final String url;
    private volatile boolean closed = false;
    private final CopyOnWriteArrayList<BiConsumer<String, String>> messageListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<BiConsumer<Integer, String>> closeListeners = new CopyOnWriteArrayList<>();

    WebSocketInfo(JsonObject params) {
        this.url = params.has("url") ? params.get("url").getAsString() : "";
    }

    /** Get the WebSocket URL. */
    public String url() { return url; }

    /** Check if the WebSocket is closed. */
    public boolean isClosed() { return closed; }

    /** Listen for messages. The callback receives (data, direction) where direction is "sent" or "received". */
    public void onMessage(BiConsumer<String, String> callback) {
        messageListeners.add(callback);
    }

    /** Listen for close events. The callback receives (code, reason). */
    public void onClose(BiConsumer<Integer, String> callback) {
        closeListeners.add(callback);
    }

    // Internal methods called by Page event handler

    void emitMessage(String data, String direction) {
        for (BiConsumer<String, String> listener : messageListeners) {
            try { listener.accept(data, direction); } catch (Exception ignored) {}
        }
    }

    void emitClose(Integer code, String reason) {
        closed = true;
        for (BiConsumer<Integer, String> listener : closeListeners) {
            try { listener.accept(code, reason); } catch (Exception ignored) {}
        }
    }

    @Override
    public String toString() {
        return "WebSocketInfo{url='" + url + "', closed=" + closed + "}";
    }
}
