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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

/** A WebSocket client for handling OCPP Messages. */
@Slf4j
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
  @VisibleForTesting
  public final Map<Class<?>, CopyOnWriteArrayList<OnOCPPMessageListener>> onReceiveMessage =
      new ConcurrentHashMap<>();

  /** The previous messages we have sent. * */
  private final Map<String, OCPPMessage> previousMessages = new ConcurrentHashMap<>();

  /** Our message scheduler. */
  @Getter private final MessageScheduler scheduler = new MessageScheduler(this);

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
      log.error("Received Bad OCPP Message: ", exception);
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
    String messageName = "";
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
          log.warn("Received OCPP message with message unknown ID {}: {}", msgId, s);
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
        this.handleReceivedMessage(OCPPMessageError.class, error);
        log.warn("Received OCPPError {}", error.toString());
        return;
      }
      default -> throw new OCPPBadCallID(callId, s);
    }

    // We found our class
    Class<?> messageClass = OCPPMessage.getMessageByName(messageName);
    if (messageClass == null) {
      log.warn("Could not find matching class for message name {}: {}", messageName, s);
      throw new OCPPUnsupportedMessage(s, messageName);
    }

    OCPPMessage message = (OCPPMessage) gson.fromJson(data, messageClass);
    message.setMessageID(msgId);
    this.handleReceivedMessage(messageClass, message);
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
   * @throws OCPPBadClass Class given was not a OCPPMessage.
   */
  private void handleReceivedMessage(final Class<?> currClass, final OCPPMessage message)
      throws OCPPBadClass {
    if (!OCPPMessage.class.isAssignableFrom(currClass)) {
      log.warn("Bad Class given to handleReceivedMessage: {}", currClass);
      throw new OCPPBadClass();
    }
    Optional.ofNullable(this.onReceiveMessage.get(currClass))
        .ifPresent(
            listeners -> {
              for (OnOCPPMessageListener listener : listeners) {
                listener.onMessageReceived(new OnOCPPMessage(message, this));
              }
            });
  }

  /**
   * Register a listener for when we receive an OCPP Message.
   *
   * @param onReceiveMessageListener The Received OCPPMessage.
   * @param currClass The class we want to set a listener for.
   * @throws OCPPBadClass Class given was not a OCPPMessage.
   */
  public void onReceiveMessage(
      final Class<?> currClass, final OnOCPPMessageListener onReceiveMessageListener)
      throws OCPPBadClass {
    if (!OCPPMessage.class.isAssignableFrom(currClass)) {
      log.warn("Bad Class given to onReceiveMessage: {}", currClass);
      throw new OCPPBadClass();
    }
    this.onReceiveMessage
        .computeIfAbsent(currClass, k -> new CopyOnWriteArrayList<>())
        .add(onReceiveMessageListener);
  }

  /**
   * Remove all listeners for an OCPP message name.
   *
   * @param classToClear The message Name to clear.
   * @throws OCPPBadClass Class given was not a OCPPMessage.
   */
  public void clearOnReceiveMessage(final Class<?> classToClear) throws OCPPBadClass {
    if (!OCPPMessage.class.isAssignableFrom(classToClear)) {
      log.warn("Bad Class given to clearOnReceiveMessage: {}", classToClear);
      throw new OCPPBadClass();
    }

    this.onReceiveMessage.remove(classToClear);
  }

  /**
   * Remove an OCPPMessageListener.
   *
   * @param classToDelete The class the listener is registered.
   * @param listener The listener to delete.
   * @throws OCPPBadClass Class given was not a OCPPMessage.
   */
  public void deleteOnReceiveMessage(final Class<?> classToDelete, OnOCPPMessageListener listener)
      throws OCPPBadClass {
    if (!OCPPMessage.class.isAssignableFrom(classToDelete)) {
      log.warn("Bad Class given to clearOnReceiveMessage: {} {}", classToDelete, listener);
      throw new OCPPBadClass();
    }

    this.onReceiveMessage.compute(
        classToDelete,
        (key, currentValue) -> {
          if (currentValue != null) {
            currentValue.remove(listener);

            return currentValue.isEmpty() ? null : currentValue;
          }
          return null;
        });
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
