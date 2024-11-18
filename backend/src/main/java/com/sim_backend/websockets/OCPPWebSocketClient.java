package com.sim_backend.websockets;

import com.google.gson.*;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.exceptions.OCPPBadCallID;
import com.sim_backend.websockets.exceptions.OCPPCannotProcessResponse;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;
import com.sim_backend.websockets.exceptions.OCPPUnsupportedMessage;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageError;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

public class OCPPWebSocketClient extends WebSocketClient {

  /** The time to wait to try to reconnect. */
  public static final int CONNECTION_LOST_TIMER = 5;

  /** The Connect Timeout. */
  private static final int CONNECT_TIMEOUT = 1;

  /** The index in the JsonArray for the call ID. */
  private static final int CALL_ID_INDEX = 0;

  /** The index in the JsonArray for the message ID. */
  private static final int MESSAGE_ID_INDEX = 1;

  /** The index in the JsonArray for the message type. */
  private static final int NAME_INDEX = 2;

  /** The index in the JsonArray for the payload. */
  public static final int PAYLOAD_INDEX = 3;

  /** The Package we will find our OCPPMessages in. */
  public static final String MESSAGE_PACKAGE = "com.sim_backend.websockets.messages";

  /** The OCPP Message Queue. */
  private final MessageQueue queue = new MessageQueue();

  /** Subscribe to when we receive an OCPP message. */
  private Map<Class<?>, ArrayList<OnOCPPMessageListener>> onReceiveMessage =
      new ConcurrentHashMap<>();

  /** The previous messages we have sent. * */
  private final Map<String, OCPPMessage> previousMessages = new ConcurrentHashMap<>();

  /**
   * Create an OCPP WebSocket Client.
   *
   * @param serverUri The Websocket Address.
   */
  public OCPPWebSocketClient(final URI serverUri) {
    super(serverUri, new Draft_6455(), null, CONNECT_TIMEOUT);
    this.setConnectionLostTimeout(CONNECTION_LOST_TIMER);
    this.startConnectionLostTimer();
  }

  @SuppressWarnings("checkstyle:FinalParameters")
  @Override
  public void onOpen(ServerHandshake serverHandshake) {}

  /**
   * When we receive a message from a sent ocpp message.
   *
   * @param s The received message as a string.
   */
  @SuppressWarnings("checkstyle:FinalParameters")
  @Override
  public void onMessage(String s) {
    Gson gson = GsonUtilities.getGson();
    JsonElement element = gson.fromJson(s, JsonElement.class);

    if (!element.isJsonArray()) {
      throw new JsonParseException("Expected array got " + element.toString());
    }

    JsonArray array = element.getAsJsonArray();
    String msgID = array.get(MESSAGE_ID_INDEX).getAsString();
    String messageName = null;
    JsonObject data = null;

    int callID = array.get(CALL_ID_INDEX).getAsInt();
    switch (callID) {
      case OCPPMessage.CALL_ID_REQUEST -> {
        // handling a simple Call
        messageName = array.get(NAME_INDEX).getAsString();
        data = array.get(PAYLOAD_INDEX).getAsJsonObject();
      }
      case OCPPMessage.CALL_ID_RESPONSE -> {
        // handling a CallResult
        if (this.previousMessages.get(msgID) == null) {
          throw new OCPPCannotProcessResponse(s, msgID);
        }

        OCPPMessageInfo info =
            this.previousMessages.remove(msgID).getClass().getAnnotation(OCPPMessageInfo.class);
        messageName = info.messageName() + "Response";
        data = array.get(PAYLOAD_INDEX - 1).getAsJsonObject();
      }
      case OCPPMessage.CALL_ID_ERROR -> {
        // handling a CallError.
        OCPPMessageError error = new OCPPMessageError(array);
        this.onReceiveMessage(OCPPMessageError.class, error);
        return;
      }
      default -> throw new OCPPBadCallID(callID, s);
    }

    if (messageName == null) {
      throw new OCPPUnsupportedMessage(s, "null");
    }

    // We found our class
    Class<?> messageClass = OCPPMessage.getMessageByName(messageName);
    if (messageClass == null) {
      throw new OCPPUnsupportedMessage(s, messageName);
    }

    OCPPMessage message = (OCPPMessage) gson.fromJson(data, messageClass);
    message.setMessageID(msgID);
    this.onReceiveMessage(messageClass, message);
  }

  @SuppressWarnings("checkstyle:FinalParameters")
  @Override
  public void onClose(int i, String s, boolean b) {}

  @SuppressWarnings("checkstyle:FinalParameters")
  @Override
  public void onError(Exception e) {}

  /**
   * Helper function for when we receive an OCPP message. This is not for registering a listener.
   *
   * @param currClass The class of the message we received.
   * @param message The Message we received
   */
  private void onReceiveMessage(final Class<?> currClass, final OCPPMessage message) {
    if (!OCPPMessage.class.isAssignableFrom(currClass)) {
      return;
    }
    Optional.ofNullable(this.onReceiveMessage.get(currClass))
        .ifPresent(
            listeners -> {
              for (OnOCPPMessageListener listener : listeners) {
                listener.onMessageReceieved(new OnOCPPMessage(message));
              }
            });
  }

  /**
   * Register a listener for when we receive an OCPP Message.
   *
   * @param onReceiveMessageListener The Received OCPPMessage.
   * @param currClass The class we want to set a listener for.
   */
  public void onReceiveMessage(
      final Class<?> currClass, final OnOCPPMessageListener onReceiveMessageListener) {
    if (!OCPPMessage.class.isAssignableFrom(currClass)) {
      return;
    }
    this.onReceiveMessage
        .computeIfAbsent(currClass, k -> new ArrayList<>())
        .add(onReceiveMessageListener);
  }

  /**
   * Add a OCPPMessage to our send queue.
   *
   * @param message the message to be sent.
   */
  public void pushMessage(final OCPPMessage message) {
    queue.pushMessage(message);
  }

  /**
   * Return the size of the send queue.
   *
   * @return size in int.
   */
  public int size() {
    return queue.size();
  }

  /**
   * Test if the send queue is empty.
   *
   * @return true if empty.
   */
  public boolean isEmpty() {
    return queue.isEmpty();
  }

  /**
   * Pop and send the message on top of the send queue.
   *
   * @return The Send OCPP Message.
   */
  public OCPPMessage popMessage() throws OCPPMessageFailure, InterruptedException {
    return queue.popMessage(this);
  }

  /** Pop the entire send queue. */
  public void popAllMessages() throws OCPPMessageFailure, InterruptedException {
    queue.popAllMessages(this);
  }

  /**
   * Add an OCPPMessage to the previous messages.
   *
   * @param msg The message we wish to add.
   */
  public void addPreviousMessage(final OCPPMessage msg) {
    this.previousMessages.put(msg.getMessageID(), msg);
  }
}
