package com.sim_backend.websockets;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.exceptions.OCPPBadCallID;
import com.sim_backend.websockets.exceptions.OCPPBadClass;
import com.sim_backend.websockets.exceptions.OCPPCannotProcessResponse;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;
import com.sim_backend.websockets.exceptions.OCPPUnsupportedMessage;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageError;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

/** A WebSocket client for handling OCPP Messages. */
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
  private final Map<Class<?>, List<OnOCPPMessageListener>> onReceiveMessage =
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
    this.setConnect0ionLostTimeout(CONNECTION_LOST_TIMER);
    this.startConnectionLostTimer();
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {}

  /**
   * When we receive an OCPP message.
   *
   * @param s The received message as a string.
   */
  @Override
  public void onMessage(String s) {
    try {
      this.handleMessage(s);
    } catch (Exception exception) {
      System.err.println(exception.getMessage());
    }
  }

  /**
   * Handle an OCPP Message.
   *
   * @param s The received message as a string.
   */
  @VisibleForTesting
  void handleMessage(final String s) throws Exception {
    Gson gson = GsonUtilities.getGson();
    JsonElement element = gson.fromJson(s, JsonElement.class);

    if (!element.isJsonArray()) {
      throw new JsonParseException("Expected array got " + element);
    }

    JsonArray array = element.getAsJsonArray();
    String msgId = array.get(MESSAGE_ID_INDEX).getAsString();
    String messageName;
    JsonObject data;

    int callId = array.get(CALL_ID_INDEX).getAsInt();
    switch (callId) {
      case OCPPMessage.CALL_ID_REQUEST -> {
        // handling a simple Call
        messageName = array.get(NAME_INDEX).getAsString();
        data = array.get(PAYLOAD_INDEX).getAsJsonObject();
      }
      case OCPPMessage.CALL_ID_RESPONSE -> {
        // handling a CallResult
        if (this.previousMessages.get(msgId) == null) {
          throw new OCPPCannotProcessResponse(s, msgId);
        }

        OCPPMessageInfo info =
            this.previousMessages.remove(msgId).getClass().getAnnotation(OCPPMessageInfo.class);
        messageName = info.messageName() + "Response";
        data = array.get(PAYLOAD_INDEX - 1).getAsJsonObject();
      }
      case OCPPMessage.CALL_ID_ERROR -> {
        // handling a CallError.
        OCPPMessageError error = new OCPPMessageError(array);
        this.onReceiveMessage(OCPPMessageError.class, error);
        return;
      }
      default -> throw new OCPPBadCallID(callId, s);
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
    message.setMessageID(msgId);
    this.onReceiveMessage(messageClass, message);
  }

  @Override
  public void onClose(int i, String s, boolean b) {}

  @Override
  public void onError(Exception e) {}

  /**
   * Helper function for when we receive an OCPP message. This is not for registering a listener.
   *
   * @param currClass The class of the message we received.
   * @param message The Message we received
   */
  private void onReceiveMessage(final Class<?> currClass, final OCPPMessage message)
      throws OCPPBadClass {
    if (!OCPPMessage.class.isAssignableFrom(currClass)) {
      throw new OCPPBadClass();
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
      final Class<?> currClass, final OnOCPPMessageListener onReceiveMessageListener)
      throws OCPPBadClass {
    if (!OCPPMessage.class.isAssignableFrom(currClass)) {
      throw new OCPPBadClass();
    }
    this.onReceiveMessage
        .computeIfAbsent(currClass, k -> new Vector<>())
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
