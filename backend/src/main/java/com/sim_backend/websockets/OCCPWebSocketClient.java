package com.sim_backend.websockets;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;

public class OCCPWebSocketClient extends WebSocketClient {
    /**
     * THe OCCP Message Queue.
     */
    private final Queue<OCCPMessage> queue = new LinkedList<>();

    /**
     * Create an OCCP WebSocket Client.
     * @param serverUri The Websocket Address.
     */
    public OCCPWebSocketClient(final URI serverUri) {
        super(serverUri);
    }

    @SuppressWarnings("checkstyle:FinalParameters")
    @Override
    public void onOpen(ServerHandshake serverHandshake) {

    }

    @SuppressWarnings("checkstyle:FinalParameters")
    @Override
    public void onMessage(String s) {

    }

    @SuppressWarnings("checkstyle:FinalParameters")
    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @SuppressWarnings("checkstyle:FinalParameters")
    @Override
    public void onError(Exception e) {

    }

    /**
     * Add a OCCPMessage to our send queue.
     * @param message the message to be sent.
     */
    public void pushMessage(final OCCPMessage message) {
        queue.add(message);
    }

    /**
     * Return the size of the send queue.
     * @return size in int.
     */
    public int size() {
        return queue.size();
    }

    /**
     * Test if the send queue is empty.
     * @return true if empty.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Pop and send the message on top of the send queue.
     * @return The Send OCCP Message.
     */
    public OCCPMessage popMessage() {
        OCCPMessage message = queue.poll();
        if (message != null) {
            message.sendMessage(this);
        }
        return message;
    }

    /**
     * Pop the entire send queue.
     */
    public void popAllMessages() {
        while (!queue.isEmpty()) {
            popMessage();
        }
    }
}
