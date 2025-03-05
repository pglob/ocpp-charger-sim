package com.sim_backend.websockets;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ErrorCode;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.events.OnPushOCPPMessage;
import com.sim_backend.websockets.events.OnPushOCPPMessageListener;
import com.sim_backend.websockets.exceptions.OCPPBadCallID;
import com.sim_backend.websockets.exceptions.OCPPBadClass;
import com.sim_backend.websockets.exceptions.OCPPBadMessage;
import com.sim_backend.websockets.exceptions.OCPPCannotProcessMessage;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;
import com.sim_backend.websockets.exceptions.OCPPUnsupportedMessage;
import com.sim_backend.websockets.exceptions.OCPPUnsupportedProtocol;
import com.sim_backend.websockets.messages.MessageValidator;
import com.sim_backend.websockets.observers.StatusNotificationObserver;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageError;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

/** A WebSocket client for handling OCPP Messages. */
@Slf4j
public class OCPPWebSocketClient extends WebSocketClient {

  @Getter
  @AllArgsConstructor
  private static class ParseResults {
    String MessageType;
    JsonObject data;
  }

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
  @Getter final MessageQueue queue = new MessageQueue();

  /** Our online status */
  @Getter private boolean Online = true;

  /** Subscribe to when we receive an OCPP message. */
  @VisibleForTesting
  public final Map<Class<?>, CopyOnWriteArrayList<OnOCPPMessageListener>> onReceiveMessage =
      new ConcurrentHashMap<>();

  /** Subscribe to when we receive an OCPP message. */
  @VisibleForTesting
  public final Map<Class<?>, CopyOnWriteArrayList<OnPushOCPPMessageListener>> onPushMessage =
      new ConcurrentHashMap<>();

  /** Our message scheduler. */
  @Getter private MessageScheduler scheduler = null;

  /** The headers we send with our Websocket connection */
  public static final Map<String, String> headers = Map.of("Sec-WebSocket-Protocol", "ocpp1.6");

  /** List to store transmitted messages. */
  private final List<String> txMessages = new CopyOnWriteArrayList<>();

  /** List to store received messages. */
  private final List<String> rxMessages = new CopyOnWriteArrayList<>();

  /** StatusNotification Observer */
  private final StatusNotificationObserver statusNotificationObserver;

  /** List of already received IDs. */
  public final Set<String> receivedIDs = ConcurrentHashMap.newKeySet();

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
      txMessages.removeFirst();
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
      rxMessages.removeFirst();
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
  public OCPPWebSocketClient(
      final URI serverUri, StatusNotificationObserver statusNotificationObserver) {
    super(serverUri, new Draft_6455(), headers, CONNECT_TIMEOUT);
    scheduler = new MessageScheduler(this);

    // Setup SSL if connecting over TLS
    if ("wss".equalsIgnoreCase(serverUri.getScheme())) {
      try {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);
        int port = serverUri.getPort();
        if (port == -1) port = 443;
        SSLSocketFactory sniFactory =
            new SniSSLSocketFactory(sslContext.getSocketFactory(), serverUri.getHost(), port);
        this.setSocketFactory(sniFactory);
      } catch (NoSuchAlgorithmException | KeyManagementException e) {
        log.error("Failed SSL Creation: ", e);
      }
    }

    try {
      this.connectBlocking();
    } catch (InterruptedException e) {
      // Do nothing, there are reconnectBlocking() calls later when sending messages
    }
    this.setConnectionLostTimeout(CONNECTION_LOST_TIMER);
    this.startConnectionLostTimer();

    this.statusNotificationObserver = statusNotificationObserver;
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
      this.pushCallError(
          ErrorCode.InternalError, "Charger threw an exception:" + exception.getLocalizedMessage());
    }
  }

  /**
   * Handle an OCPP Message.
   *
   * @param json The received message as a string.
   */
  @VisibleForTesting
  void handleMessage(final String json) throws Exception {
    Gson gson = GsonUtilities.getGson();
    try {
      JsonElement element = gson.fromJson(json, JsonElement.class);

      if (element == null) {
        this.pushCallError(ErrorCode.FormatViolation, "Provided empty string");
        return;
      }

      if (!element.isJsonArray()) {
        this.pushCallError(ErrorCode.FormatViolation, "Root Element should be an array");
        throw new JsonParseException("Expected array got " + element);
      }

      JsonArray array = element.getAsJsonArray();
      String msgId = array.get(MESSAGE_ID_INDEX).getAsString();

      if (this.receivedIDs.contains(msgId)) {
        this.pushCallError(ErrorCode.OccurenceConstraintViolation, "ID was already used", msgId);
        log.error("Received duplicate ID {}", msgId);
        return;
      }

      this.receivedIDs.add(msgId);

      ParseResults results;

      int callId = array.get(CALL_ID_INDEX).getAsInt();
      switch (callId) {
        case OCPPMessage.CALL_ID_REQUEST -> results = this.parseOCPPRequest(json, msgId, array);

        case OCPPMessage.CALL_ID_RESPONSE -> results = this.parseOCPPResponse(json, msgId, array);

        case OCPPMessage.CALL_ID_ERROR -> {
          this.handleOCPPMessageError(json, msgId, array);
          return;
        }

        default -> {
          this.pushCallError(ErrorCode.PropertyConstraintViolation, "Provided bad Call ID", msgId);
          throw new OCPPBadCallID(callId, json);
        }
      }

      if (results == null) {
        return;
      }

      Class<?> messageClass = OCPPMessage.getMessageByName(results.getMessageType());
      if (messageClass == null) {
        log.warn(
            "Could not find matching class for message name {}: {}",
            results.getMessageType(),
            json);
        this.pushCallError(ErrorCode.NotSupported, "Unsupported action", msgId);
        throw new OCPPUnsupportedMessage(json, results.getMessageType());
      }

      OCPPMessage message = (OCPPMessage) gson.fromJson(results.getData(), messageClass);
      message.setMessageID(msgId);

      if (!MessageValidator.isValid(message)) {
        this.pushCallError(ErrorCode.FormatViolation, MessageValidator.log_message(message), msgId);
        return;
      }

      this.handleReceivedMessage(messageClass, message);
    } catch (JsonSyntaxException exception) {
      this.pushCallError(ErrorCode.FormatViolation, exception.getLocalizedMessage());
    }
  }

  @Override
  public void onClose(int i, String s, boolean b) {
    log.info("Connection closed by {}: {} {}", (b ? "remote" : "local"), i, s);
  }

  @Override
  public void onError(Exception e) {
    log.error("WebsocketClient Errored: ", e);
  }

  /**
   * Parse a received OCPPRequest to extract its type and data.
   *
   * @param json The full message json.
   * @param msgId The received message ID.
   * @param array The JSONArray we received.
   * @return The parsed results.
   */
  private ParseResults parseOCPPRequest(String json, String msgId, JsonArray array) {
    if (array.size() != 4) {
      this.pushCallError(
          ErrorCode.OccurenceConstraintViolation,
          "Request provided wrong number of array elements",
          msgId);
      throw new OCPPBadMessage("Request had invalid array length");
    }

    if (!array.get(PAYLOAD_INDEX).isJsonObject()) {
      this.pushCallError(
          ErrorCode.PropertyConstraintViolation, "Request details was not a json object", msgId);
      return null;
    }

    String messageName = array.get(NAME_INDEX).getAsString();
    return new ParseResults(messageName, array.get(PAYLOAD_INDEX).getAsJsonObject());
  }

  /**
   * Parse an OCPPResponse to extract it's Json object and the given type of message.
   *
   * @param json The full message json.
   * @param msgId The message ID.
   * @param array The array of data.
   * @return The parsed results.
   * @throws OCPPCannotProcessMessage No previously sent messages found with a matching ID.
   */
  private ParseResults parseOCPPResponse(String json, String msgId, JsonArray array)
      throws OCPPCannotProcessMessage {
    if (array.size() != 3) {
      this.pushCallError(
          ErrorCode.OccurenceConstraintViolation,
          "Response provided wrong number of array elements",
          msgId);
      throw new OCPPBadMessage("Response had invalid array length");
    }

    OCPPMessage prevMessage = this.queue.getPreviousMessage(msgId);
    if (prevMessage == null) {
      this.pushCallError(ErrorCode.ProtocolError, "Received Response with an unknown ID", msgId);
      log.warn("Received OCPP response message with an unknown ID {}: {}", msgId, json);
      throw new OCPPCannotProcessMessage(json, msgId);
    }

    if (!array.get(PAYLOAD_INDEX - 1).isJsonObject()) {
      this.pushCallError(
          ErrorCode.PropertyConstraintViolation, "Response details was not a json object", msgId);
      return null;
    }

    this.queue.clearPreviousMessage(prevMessage);
    OCPPMessageInfo info = prevMessage.getClass().getAnnotation(OCPPMessageInfo.class);

    String messageName = info.messageName() + "Response";
    this.recordRxMessage(json, info.messageName());
    return new ParseResults(messageName, array.get(PAYLOAD_INDEX - 1).getAsJsonObject());
  }

  /**
   * Handle a received OCPPMessageError.
   *
   * @param json The full message json.
   * @param msgId The message ID.
   * @param array The array of data.
   * @throws OCPPCannotProcessMessage No previously sent messages found with a matching ID.
   */
  private void handleOCPPMessageError(String json, String msgId, JsonArray array)
      throws OCPPCannotProcessMessage {
    if (array.size() != 5) {
      this.pushCallError(
          ErrorCode.OccurenceConstraintViolation,
          "Error provided wrong number of array elements",
          msgId);
      throw new OCPPBadMessage("Error had invalid array length");
    }

    OCPPMessage prevMessage = this.queue.getPreviousMessage(msgId);
    if (prevMessage == null) {
      this.pushCallError(ErrorCode.ProtocolError, "Received Error with an unknown ID", msgId);
      log.warn("Received OCPP error message with an unknown ID {}: {}", msgId, json);
      throw new OCPPCannotProcessMessage(json, msgId);
    }

    try {
      if (!array.get(OCPPMessageError.DETAIL_INDEX).isJsonObject()) {
        this.pushCallError(
            ErrorCode.PropertyConstraintViolation, "Error details was not a json object", msgId);

        return;
      }

      Class<?> complementClass = prevMessage.getClass();
      this.queue.clearPreviousMessage(prevMessage);
      prevMessage.setErrored(true);
      if (this.onReceiveMessage.containsKey(complementClass)) {
        for (var listener : this.onReceiveMessage.get(complementClass)) {
          listener.onTimeout();
        }
      }

      OCPPMessageError error =
          new OCPPMessageError(
              ErrorCode.valueOf(array.get(OCPPMessageError.CODE_INDEX).getAsString()),
              array.get(OCPPMessageError.DESCRIPTION_INDEX).getAsString(),
              array.get(OCPPMessageError.DETAIL_INDEX).getAsJsonObject());
      error.setMessageID(msgId);
      error.setErroredMessage(prevMessage);

      if (!MessageValidator.isValid(error)) {
        this.pushCallError(ErrorCode.FormatViolation, MessageValidator.log_message(error), msgId);
        return;
      }

      this.handleReceivedMessage(OCPPMessageError.class, error);
      log.warn("Received OCPPError {}", error);

      OCPPMessageInfo info = prevMessage.getClass().getAnnotation(OCPPMessageInfo.class);
      this.recordRxMessage(json, info.messageName());

    } catch (IllegalArgumentException exception) {
      this.pushCallError(
          ErrorCode.PropertyConstraintViolation, "Received Unknown Error Code", msgId);
    }
  }

  /**
   * Push an OCPPMessageError to the stack.
   *
   * @param code The ErrorCode.
   * @param description The error's description.
   */
  public void pushCallError(ErrorCode code, String description) {
    this.pushMessage(new OCPPMessageError(code, description, new JsonObject()));
  }

  /**
   * Push an OCPPMessageError to the stack.
   *
   * @param code The ErrorCode.
   * @param description The error's description.
   * @param msgID The message ID to send the error with.
   */
  public void pushCallError(ErrorCode code, String description, String msgID) {
    OCPPMessageError error = new OCPPMessageError(code, description, new JsonObject());
    error.setMessageID(msgID);
    this.pushMessage(error);
  }

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
            listeners ->
                listeners.forEach(
                    listener -> {
                      listener.onMessageReceived(new OnOCPPMessage(message, this));
                    }));
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
   * Register a listener for when we push an OCPP Message.
   *
   * @param onReceiveMessageListener The Received OCPPMessage.
   * @param currClass The class we want to set a listener for.
   * @throws OCPPBadClass Class given was not a OCPPMessage.
   */
  public void onPushMessage(
      final Class<?> currClass, final OnPushOCPPMessageListener onReceiveMessageListener)
      throws OCPPBadClass {
    if (!OCPPMessage.class.isAssignableFrom(currClass)) {
      log.warn("Bad Class given to onReceiveMessage: {}", currClass);
      throw new OCPPBadClass();
    }
    this.onPushMessage
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
    if (!MessageValidator.isValid(message)) {
      throw new IllegalArgumentException(MessageValidator.log_message(message));
    }

    Optional.ofNullable(this.onPushMessage.get(message.getClass()))
        .ifPresent(
            listeners ->
                listeners.forEach(
                    listener -> {
                      listener.onPush(new OnPushOCPPMessage(message, this));
                    }));

    recordTxMessage(message.toJsonString()); // Record transmitted message
    return queue.pushMessage(message);
  }

  /**
   * Add a OCPPMessage to the front of our send queue.
   *
   * @param prioMessage the message to be sent.
   */
  public boolean pushPriorityMessage(final OCPPMessage prioMessage) {
    recordTxMessage(prioMessage.toJsonString());
    return queue.pushPriorityMessage(prioMessage);
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
    statusNotificationObserver.onClientGoOnline();
    this.startConnectionLostTimer();
    this.Online = true;
  }
}
