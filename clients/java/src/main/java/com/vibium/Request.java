package com.vibium;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vibium.internal.BiDiClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Network request info.
 */
public class Request {

    private final BiDiClient client;
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final String requestId;

    Request(BiDiClient client, JsonObject params) {
        this.client = client;

        // Extract from either top-level or nested request object
        JsonObject req = params.has("request") && params.get("request").isJsonObject()
            ? params.getAsJsonObject("request")
            : params;

        this.url = req.has("url") ? req.get("url").getAsString() : "";
        this.method = req.has("method") ? req.get("method").getAsString() : "";
        this.requestId = req.has("requestId") ? req.get("requestId").getAsString()
            : (params.has("requestId") ? params.get("requestId").getAsString() : "");
        this.headers = parseHeaders(req);
    }

    /** Get the request URL. */
    public String url() { return url; }

    /** Get the HTTP method. */
    public String method() { return method; }

    /** Get the request headers. */
    public Map<String, String> headers() { return headers; }

    /** Get the request ID. */
    public String requestId() { return requestId; }

    private static Map<String, String> parseHeaders(JsonObject obj) {
        Map<String, String> map = new LinkedHashMap<>();
        if (obj.has("headers") && obj.get("headers").isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray("headers");
            for (JsonElement el : arr) {
                JsonObject header = el.getAsJsonObject();
                String name = header.get("name").getAsString();
                JsonObject value = header.getAsJsonObject("value");
                map.put(name, value.get("value").getAsString());
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return "Request{method='" + method + "', url='" + url + "'}";
    }
}
