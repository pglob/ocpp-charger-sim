package com.sim_backend.websockets;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class OCPPWebSocketClient extends WebSocketClient {

    /**
     * The time to wait to try to reconnect.
     */
    public static final int CONNECTION_LOST_TIMER = 5;

    /**
     * The Connect Timeout.
     */
    private static final int CONNECT_TIMEOUT = 1;

    /**
     * The OCPP Message Queue.
     */
    private final MessageQueue queue = new MessageQueue();

    /**
     * Create an OCPP WebSocket Client.
     * @param serverUri The Websocket Address.
     */
    public OCPPWebSocketClient(final URI serverUri) {
        super(serverUri, new Draft_6455(), null, CONNECT_TIMEOUT);
        this.setConnectionLostTimeout(CONNECTION_LOST_TIMER);
        this.startConnectionLostTimer();
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
     * Add a OCPPMessage to our send queue.
     * @param message the message to be sent.
     */
    public void pushMessage(final OCPPMessage message) {
        queue.pushMessage(message);
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
     * @return The Send OCPP Message.
     */
    public OCPPMessage popMessage()
            throws OCPPMessageFailure, InterruptedException {
        try {
            return queue.popMessage(this);
        } catch (OCPPMessageFailure failure) {
            this.reconnectBlocking();
            throw failure;
        }

    }

    /**
     * Pop the entire send queue.
     */
    public void popAllMessages()
            throws OCPPMessageFailure, InterruptedException {
        try {
            queue.popAllMessages(this);
        } catch (OCPPMessageFailure failure) {
            this.reconnectBlocking();
            throw failure;
        }
    }
}
