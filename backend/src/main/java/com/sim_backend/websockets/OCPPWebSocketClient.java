package com.sim_backend.websockets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sim_backend.websockets.messages.HeartBeat;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.reflections.Reflections;

import java.net.URI;
import java.util.Set;

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
     * The index in the JsonArray for the call ID.
     */
    private static final int CALL_ID_INDEX = 0;

    /**
     * The index in the JsonArray for the message ID.
     */
    private static final int MESSAGE_ID_INDEX = 1;

    /**
     * The index in the JsonArray for the message type.
     */
    private static final int TYPE_INDEX = 2;

    /**
     * The index in the JsonArray for the payload.
     */
    public static final int PAYLOAD_INDEX = 3;

    /**
     * The Package we will find our OCPPMessages in.
     */
    public static final String MESSAGE_PACKAGE =
            "com.sim_backend.websockets.messages";

    /**
     * The OCPP Message Queue.
     */
    private final MessageQueue queue = new MessageQueue();

    /**
     * Subscribe to when we receive an OCPP message.
     */
    private OnOCPPMessageListener onReceiveMessage;

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

    /**
     * When we receive a message from a sent ocpp message.
     * @param s The received message as a string.
     */
    @SuppressWarnings("checkstyle:FinalParameters")
    @Override
    public void onMessage(String s) {
        if (onReceiveMessage != null) {
            Gson gson = GsonUtilities.getGson();
            JsonElement element = gson.fromJson(s, JsonElement.class);
            assert element.isJsonArray();
            JsonArray array = element.getAsJsonArray();

            int callID = array.get(CALL_ID_INDEX).getAsInt();
            String msgID = array.get(MESSAGE_ID_INDEX).getAsString();
            String messageType = array.get(TYPE_INDEX).getAsString();
            JsonObject data = array.get(PAYLOAD_INDEX).getAsJsonObject();

            Reflections reflections = new Reflections(MESSAGE_PACKAGE);

            // Get all classes in our messages package,
            // that are annotated with OCPPMessageInfo.
            Set<Class<?>> classes =
                    reflections.getTypesAnnotatedWith(OCPPMessageInfo.class);

            // Find the one that matches the received message Type.
            for (Class<?> messageClass : classes) {
                OCPPMessageInfo annotation =
                        messageClass.getAnnotation(OCPPMessageInfo.class);
                // Check if it's a has a parent class of OCPPMessage.
                if (OCPPMessage.class.isAssignableFrom(messageClass)
                        && annotation.messageName().equals(messageType)) {
                    // Convert the payload String into the found class.
                    OCPPMessage message =
                            (OCPPMessage) gson.fromJson(data, messageClass);
                    onReceiveMessage.onMessageReceieved(
                            new OnOCPPMessage(message));
                    return;
                }
            }
            assert false;
        }
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
     * Set the function called when we receive an OCPP Message.
     * @param onReceiveMessageListener The Received OCPPMessage.
     */
    public void setOnRecieveMessage(
            final OnOCPPMessageListener onReceiveMessageListener) {
        this.onReceiveMessage = onReceiveMessageListener;
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
        return queue.popMessage(this);

    }

    /**
     * Pop the entire send queue.
     */
    public void popAllMessages()
            throws OCPPMessageFailure, InterruptedException {
        queue.popAllMessages(this);
    }
}
