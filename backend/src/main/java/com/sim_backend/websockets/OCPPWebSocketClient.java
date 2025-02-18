package com.sim_backend.websockets;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.*;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ErrorCode;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.exceptions.*;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageError;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
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
  @Getter private final MessageQueue queue = new MessageQueue();

  /** Our online status */
  @Getter private boolean Online = true;

  /** Subscribe to when we receive an OCPP message. */
  @VisibleForTesting
  public final Map<Class<?>, CopyOnWriteArrayList<OnOCPPMessageListener>> onReceiveMessage =
      new ConcurrentHashMap<>();

  /** Our message scheduler. */
  @Getter private final MessageScheduler scheduler = new MessageScheduler(this);

  /** The headers we send with our Websocket connection */
  public static final Map<String, String> headers = Map.of("Sec-WebSocket-Protocol", "ocpp1.6");

  /** List to store transmitted messages. */
  private final List<String> txMessages = new CopyOnWriteArrayList<>();

  /** List to store received messages. */
  private final List<String> rxMessages = new CopyOnWriteArrayList<>();

  /**
   * Record a transmitted message.
   *
   * @param message The transmitted message.
   */
  public void recordTxMessage(String message) {
    String timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
    String messageWithTimestamp = message.replaceFirst("\\[", "[\"" + timestamp + "\", ");
    txMessages.add(messageWithTimestamp);
    if (txMessages.size() > 50) {
      txMessages.remove(0);
    }
  }

  /**
   * Record a received message.
   *
   * @param message The received message.
   */
  public void recordRxMessage(String message, String messageName) {
    String timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
    String modifiedMessage =
        message.replaceFirst("\\[", "[\"" + messageName + "\", \"" + timestamp + "\", ");
    rxMessages.add(modifiedMessage);
    if (rxMessages.size() > 50) {
      rxMessages.remove(0);
    }
  }

  /**
   * Get the list of transmitted messages.
   *
   * @return List of transmitted messages.
   */
  public List<String> getSentMessages() {
    return txMessages;
  }

  /**
   * Get the list of received messages.
   *
   * @return List of received messages.
   */
  public List<String> getReceivedMessages() {
    return rxMessages;
  }

  /**
   * Create an OCPP WebSocket Client.
   *
   * @param serverUri The Websocket Address.
   */
  public OCPPWebSocketClient(final URI serverUri) {
    super(serverUri, new Draft_6455(), headers, CONNECT_TIMEOUT);
    this.setConnectionLostTimeout(CONNECTION_LOST_TIMER);
    this.startConnectionLostTimer();
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    String protocol = serverHandshake.getFieldValue("Sec-WebSocket-Protocol");
    if (protocol == null || !protocol.contains("ocpp1.6")) {
      log.error("Handshake failed no supported protocols provided: {}", protocol);
      throw new OCPPUnsupportedProtocol(protocol);
    }
  }

  /**
   * When we receive an OCPP message.
   *
   * @param s The received message as a string.
   */
  @Override
  public void onMessage(String s) {
    if (!isOnline()) {
      return;
    }

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
    try {
      JsonElement element = gson.fromJson(s, JsonElement.class);

      if (element == null) {
        this.pushMessage(
            new OCPPMessageError(
                ErrorCode.FormatViolation, "Provided empty string", new JsonObject()));
        return;
      }

      if (!element.isJsonArray()) {
        this.pushMessage(
            new OCPPMessageError(
                ErrorCode.FormatViolation, "Root Element should be an array", new JsonObject()));
        throw new JsonParseException("Expected array got " + element);
      }

      JsonArray array = element.getAsJsonArray();
      String msgId = array.get(MESSAGE_ID_INDEX).getAsString();
      String messageName = "";
      String messageType = "";
      JsonObject data;

      int callId = array.get(CALL_ID_INDEX).getAsInt();
      switch (callId) {
        case OCPPMessage.CALL_ID_REQUEST -> {
          if (array.size() != 4) {
            this.pushMessage(
                new OCPPMessageError(
                    ErrorCode.OccurenceConstraintViolation,
                    "Request provided wrong number of array elements",
                    new JsonObject()));
            throw new OCPPBadMessage("Request had invalid array length");
          }
          // handling a simple Call
          messageName = array.get(NAME_INDEX).getAsString();
          data = array.get(PAYLOAD_INDEX).getAsJsonObject();
        }
        case OCPPMessage.CALL_ID_RESPONSE -> {
          if (array.size() != 3) {
            this.pushMessage(
                new OCPPMessageError(
                    ErrorCode.OccurenceConstraintViolation,
                    "Response provided wrong number of array elements",
                    new JsonObject()));
            throw new OCPPBadMessage("Response had invalid array length");
          }
          // handling a CallResult
          OCPPMessage prevMessage = this.queue.getPreviousMessage(msgId);
          if (prevMessage == null) {
            log.warn("Received OCPP response message with an unknown ID {}: {}", msgId, s);
            throw new OCPPCannotProcessMessage(s, msgId);
          }

          this.queue.clearPreviousMessage(prevMessage);
          OCPPMessageInfo info = prevMessage.getClass().getAnnotation(OCPPMessageInfo.class);
          messageName = info.messageName() + "Response";
          messageType = info.messageName();
          this.recordRxMessage(s, messageType);
          data = array.get(PAYLOAD_INDEX - 1).getAsJsonObject();
        }
        case OCPPMessage.CALL_ID_ERROR -> {
          if (array.size() != 5) {
            this.pushMessage(
                new OCPPMessageError(
                    ErrorCode.OccurenceConstraintViolation,
                    "Error provided wrong number of array elements",
                    new JsonObject()));
            throw new OCPPBadMessage("Error had invalid array length");
          }

          OCPPMessage prevMessage = this.queue.getPreviousMessage(msgId);
          if (prevMessage == null) {
            log.warn("Received OCPP error message with an unknown ID {}: {}", msgId, s);
            throw new OCPPCannotProcessMessage(s, msgId);
          }

          this.queue.clearPreviousMessage(prevMessage);
          OCPPMessageError error = new OCPPMessageError(array);
          error.setErroredMessage(prevMessage);
          this.handleReceivedMessage(OCPPMessageError.class, error);
          log.warn("Received OCPPError {}", error.toString());
          OCPPMessageInfo info = prevMessage.getClass().getAnnotation(OCPPMessageInfo.class);
          messageType = info.messageName();
          this.recordRxMessage(s, messageType);
          return;
        }
        default -> {
          this.pushMessage(
              new OCPPMessageError(
                  ErrorCode.PropertyConstraintViolation, "Provided bad Call ID", new JsonObject()));
          throw new OCPPBadCallID(callId, s);
        }
      }

      // We found our class
      Class<?> messageClass = OCPPMessage.getMessageByName(messageName);
      if (messageClass == null) {
        log.warn("Could not find matching class for message name {}: {}", messageName, s);
        this.pushMessage(
            new OCPPMessageError(ErrorCode.NotSupported, "Unsupported action", new JsonObject()));
        throw new OCPPUnsupportedMessage(s, messageName);
      }

      OCPPMessage message = (OCPPMessage) gson.fromJson(data, messageClass);
      message.setMessageID(msgId);
      this.handleReceivedMessage(messageClass, message);
    } catch (JsonSyntaxException exception) {
      OCPPMessageError error =
          new OCPPMessageError(
              ErrorCode.FormatViolation, exception.getLocalizedMessage(), new JsonObject());
      this.pushMessage(error);
    }
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
  public boolean pushMessage(final OCPPMessage message) {
    recordTxMessage(message.toJsonString()); // Record transmitted message
    return queue.pushMessage(message);
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
   * Return if the send queue is busy.
   *
   * @return True if it's busy
   */
  public boolean isBusy() {
    return queue.isBusy();
  }

  /**
   * Pop and send the message on top of the send queue.
   *
   * @return The Send OCPP Message.
   */
  public OCPPMessage popMessage() throws OCPPMessageFailure, InterruptedException {
    if (!this.isOnline()) {
      return null;
    }
    return queue.popMessage(this);
  }

  /** Pop the entire send queue. */
  public void popAllMessages() throws OCPPMessageFailure, InterruptedException {
    if (!this.isOnline()) {
      return;
    }
    queue.popAllMessages(this);
  }

  /**
   * Add an OCPPMessage to the previous messages.
   *
   * @param msg The message we wish to add.
   */
  public void addPreviousMessage(final OCPPMessage msg) {
    queue.addPreviousMessage(msg);
  }

  /**
   * Deletes an OCPPMessage from the previous messages.
   *
   * @param msg The message we wish to delete.
   */
  public void clearPreviousMessage(final OCPPMessage msg) {
    queue.clearPreviousMessage(msg);
  }

  /** Take the websocket client offline. */
  public void goOffline() {
    this.stopConnectionLostTimer();
    this.Online = false;
  }

  /** Take our websocket client back online */
  public void goOnline() {
    this.startConnectionLostTimer();
    this.Online = true;
  }
}
