package com.vibium;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vibium.internal.BiDiClient;
import com.vibium.types.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Isolated browser context (cookies, storage, init scripts).
 */
public class BrowserContext {

    private static final Gson GSON = new Gson();

    private final BiDiClient client;
    private final String userContextId;
    private final Recording recording;

    BrowserContext(BiDiClient client, String userContextId) {
        this.client = client;
        this.userContextId = userContextId;
        this.recording = new Recording(client, userContextId);
    }

    /** Get the context ID. */
    public String id() { return userContextId; }

    /** Get the Recording for this context. */
    public Recording recording() { return recording; }

    /** Create a new page in this context. */
    public Page newPage() {
        JsonObject params = new JsonObject();
        params.addProperty("userContext", userContextId);
        JsonObject result = client.send("vibium:context.newPage", params);
        String contextId = result.get("context").getAsString();
        return new Page(client, contextId, this);
    }

    /** Close this context. */
    public void close() {
        JsonObject params = new JsonObject();
        params.addProperty("userContext", userContextId);
        client.send("browser.removeUserContext", params);
    }

    /** Get cookies, optionally filtered by URLs. */
    public List<Cookie> cookies(String... urls) {
        JsonObject params = contextParams();
        if (urls != null && urls.length > 0) {
            params.add("urls", GSON.toJsonTree(urls));
        }
        JsonObject result = client.send("vibium:context.cookies", params);
        JsonArray arr = result.getAsJsonArray("cookies");
        List<Cookie> cookies = new ArrayList<>();
        for (JsonElement el : arr) {
            cookies.add(GSON.fromJson(el, Cookie.class));
        }
        return cookies;
    }

    /** Set cookies. */
    public void setCookies(List<SetCookieParam> cookies) {
        JsonObject params = contextParams();
        params.add("cookies", GSON.toJsonTree(cookies));
        client.send("vibium:context.setCookies", params);
    }

    /** Clear all cookies. */
    public void clearCookies() {
        client.send("vibium:context.clearCookies", contextParams());
    }

    /** Get storage state. */
    public StorageState storage() {
        JsonObject result = client.send("vibium:context.storage", contextParams());
        return GSON.fromJson(result, StorageState.class);
    }

    /** Set storage state. */
    public void setStorage(StorageState state) {
        JsonObject params = contextParams();
        params.add("state", GSON.toJsonTree(state));
        client.send("vibium:context.setStorage", params);
    }

    /** Clear all storage. */
    public void clearStorage() {
        client.send("vibium:context.clearStorage", contextParams());
    }

    /** Add an init script. */
    public String addInitScript(String script) {
        JsonObject params = contextParams();
        params.addProperty("script", script);
        JsonObject result = client.send("vibium:context.addInitScript", params);
        return result.has("scriptId") ? result.get("scriptId").getAsString() : "";
    }

    private JsonObject contextParams() {
        JsonObject params = new JsonObject();
        params.addProperty("userContext", userContextId);
        return params;
    }
}
