package com.sim_backend.websockets;

import com.google.gson.JsonElement;

public abstract class OCPPMessage {
    /**
     * Number of attempts to send this message.
     */
    private transient int tries = 0;

    /**
     * Create a JSON element representing the message.
     * @return The generated message JSON.
     */
    public JsonElement generateMessage() {
        return GsonUtilities.getGson().toJsonTree(this);
    }

    /**
     * Emit the message to the given client.
     * @param client The websocket client.
     */
    public void sendMessage(final OCPPWebSocketClient client) {
        client.send(GsonUtilities.toString(this.generateMessage()));
    }

    /**
     * Increment the number of tries this message has been sent.
     * @return The new tries number.
     */
    public int incrementTries() {
        tries += 1;
        return tries;
    }

}
