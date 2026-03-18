package com.vibium;

import com.google.gson.JsonObject;
import com.vibium.internal.BiDiClient;

/**
 * Browser dialog (alert, confirm, prompt, beforeunload).
 */
public class Dialog {

    private final BiDiClient client;
    private final String contextId;
    private final String type;
    private final String message;
    private final String defaultValue;

    Dialog(BiDiClient client, JsonObject params) {
        this.client = client;
        this.contextId = params.has("context") ? params.get("context").getAsString() : "";
        this.type = params.has("type") ? params.get("type").getAsString() : "alert";
        this.message = params.has("message") ? params.get("message").getAsString() : "";
        this.defaultValue = params.has("defaultValue") ? params.get("defaultValue").getAsString() : "";
    }

    /** Get the dialog message text. */
    public String message() { return message; }

    /** Get the dialog type (alert, confirm, prompt, beforeunload). */
    public String type() { return type; }

    /** Get the default value (for prompt dialogs). */
    public String defaultValue() { return defaultValue; }

    /** Accept the dialog. */
    public void accept() {
        accept(null);
    }

    /** Accept the dialog with prompt text. */
    public void accept(String promptText) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("context", contextId);
            params.addProperty("accept", true);
            if (promptText != null) {
                params.addProperty("userText", promptText);
            }
            // Use raw BiDi command to avoid dispatch mutex deadlock with element.click
            client.send("browsingContext.handleUserPrompt", params);
        } catch (Exception ignored) {
            // Silently handle race conditions
        }
    }

    /** Dismiss the dialog. */
    public void dismiss() {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("context", contextId);
            params.addProperty("accept", false);
            // Use raw BiDi command to avoid dispatch mutex deadlock with element.click
            client.send("browsingContext.handleUserPrompt", params);
        } catch (Exception ignored) {
            // Silently handle race conditions
        }
    }

    @Override
    public String toString() {
        return "Dialog{type='" + type + "', message='" + message + "'}";
    }
}
