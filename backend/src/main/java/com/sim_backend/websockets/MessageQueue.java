package com.sim_backend.websockets;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.util.Deque;
import java.util.LinkedList;

public class MessageQueue {

    /**
     * The OCPP Message Queue.
     */
    private final Deque<OCPPMessage> queue = new LinkedList<>();

    /**
     * Create an OCPPMessage Queue.
     */
    public MessageQueue() {
    }
    /**
     * Add a OCPPMessage to our send queue.
     * @param message the message to be sent.
     */
    public void pushMessage(final OCPPMessage message) {
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
     * @param client The WebsocketClient to send it through.
     * @return The Send OCPP Message.
     */
    public OCPPMessage popMessage(final OCPPWebSocketClient client)
            throws OCPPMessageFailure {
        OCPPMessage message = queue.poll();
        if (message != null) {
            try {
                message.sendMessage(client);
            } catch (WebsocketNotConnectedException ex) {
                queue.addFirst(message);
                throw new OCPPMessageFailure(message, ex);
            }
        }
        return message;
    }

    /**
     * Pop the entire send queue.
     * @param client The WebsocketClient to send it through.
     */
    public void popAllMessages(final OCPPWebSocketClient client)
            throws OCPPMessageFailure {
        while (!queue.isEmpty()) {
            popMessage(client);
        }
    }
}
