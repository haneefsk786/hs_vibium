package com.vibium;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vibium.types.SourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Console message from the page (console.log, console.error, etc.).
 */
public class ConsoleMessage {

    private final String type;
    private final String text;
    private final List<Object> args;
    private final SourceLocation location;

    ConsoleMessage(JsonObject params) {
        this.type = params.has("type") ? params.get("type").getAsString()
            : (params.has("level") ? params.get("level").getAsString() : "log");
        this.text = params.has("text") ? params.get("text").getAsString() : "";

        // Parse args
        this.args = new ArrayList<>();
        if (params.has("args") && params.get("args").isJsonArray()) {
            JsonArray argsArr = params.getAsJsonArray("args");
            for (JsonElement el : argsArr) {
                if (el.isJsonObject()) {
                    JsonObject argObj = el.getAsJsonObject();
                    if (argObj.has("value")) {
                        args.add(Page.jsonToJava(argObj.get("value")));
                    } else {
                        args.add(Page.jsonToJava(el));
                    }
                } else {
                    args.add(Page.jsonToJava(el));
                }
            }
        }

        // Parse source location
        if (params.has("stackTrace") && params.get("stackTrace").isJsonObject()) {
            JsonObject stack = params.getAsJsonObject("stackTrace");
            if (stack.has("callFrames") && stack.get("callFrames").isJsonArray()) {
                JsonArray frames = stack.getAsJsonArray("callFrames");
                if (frames.size() > 0) {
                    this.location = parseLocation(frames.get(0).getAsJsonObject());
                } else {
                    this.location = null;
                }
            } else {
                this.location = null;
            }
        } else {
            this.location = null;
        }
    }

    /** Get the message type (log, warn, error, debug, info). */
    public String type() { return type; }

    /** Get the message text. */
    public String text() { return text; }

    /** Get the message arguments. */
    public List<Object> args() { return args; }

    /** Get the source location. */
    public SourceLocation location() { return location; }

    private static SourceLocation parseLocation(JsonObject frame) {
        String url = frame.has("url") ? frame.get("url").getAsString() : "";
        int line = frame.has("lineNumber") ? frame.get("lineNumber").getAsInt() : 0;
        int col = frame.has("columnNumber") ? frame.get("columnNumber").getAsInt() : 0;
        return new SourceLocation(url, line, col);
    }

    @Override
    public String toString() {
        return "ConsoleMessage{type='" + type + "', text='" + text + "'}";
    }
}
