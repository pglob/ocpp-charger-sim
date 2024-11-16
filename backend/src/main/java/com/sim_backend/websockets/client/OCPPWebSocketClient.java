package com.sim_backend.websockets.client;

import com.google.gson.*;
import com.sim_backend.exceptions.OCPPMessageFailureException;
import com.sim_backend.exceptions.OCPPUnsupportedMessageException;
import com.sim_backend.utils.GsonUtilities;
import com.sim_backend.websockets.*;
import com.sim_backend.websockets.messages.OCPPMessage;
import java.net.URI;
import java.util.Set;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.reflections.Reflections;

public class OCPPWebSocketClient extends WebSocketClient {

  /** The time to wait to try to reconnect. */
  public static final int CONNECTION_LOST_TIMER = 5;

  /** The Connect Timeout. */
  private static final int CONNECT_TIMEOUT = 1;

  /** The Package we will find our OCPPMessages in. */
  public static final String MESSAGE_PACKAGE = "com.sim_backend.websockets.messages";

  /** The OCPP Message Queue. */
  private final MessageQueue queue = new MessageQueue();

  /** Subscribe to when we receive an OCPP message. */
  private OnOCPPMessageListener onReceiveMessage;

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
    if (onReceiveMessage != null) {
      Gson gson = GsonUtilities.getGson();
      JsonElement element = gson.fromJson(s, JsonElement.class);
      System.out.println("Element: " + element);

      if (!element.isJsonObject()) {
        throw new JsonParseException("Expected object got " + element.toString());
      }

      JsonObject object = element.getAsJsonObject();
      if (!object.has("body") || object.get("body") == null) {
        throw new JsonParseException(s);
      }
      if (!object.has("messageType") || object.get("messageType") == null) {
        throw new OCPPUnsupportedMessageException(s, null);
      }

      String messageType = object.get("messageType").getAsString();
      JsonObject data = object.get("body").getAsJsonObject();

      Reflections reflections = new Reflections(MESSAGE_PACKAGE);

      // Get all classes in our messages package,
      // that are annotated with OCPPMessageInfo.
      Set<Class<?>> classes = reflections.getTypesAnnotatedWith(OCPPMessageInfo.class);

      // Find the one that matches the received message Type.
      for (Class<?> messageClass : classes) {
        OCPPMessageInfo annotation = messageClass.getAnnotation(OCPPMessageInfo.class);
        // Check if it's a has a parent class of OCPPMessage.
        if (OCPPMessage.class.isAssignableFrom(messageClass)
            && annotation.messageName().equals(messageType)) {
          // Convert the payload String into the found class.
          OCPPMessage message = (OCPPMessage) gson.fromJson(data, messageClass);
          onReceiveMessage.onMessageReceieved(new OnOCPPMessage(message));
          return;
        }
      }
      throw new OCPPUnsupportedMessageException(s, messageType);
    }
  }

  @SuppressWarnings("checkstyle:FinalParameters")
  @Override
  public void onClose(int i, String s, boolean b) {}

  @SuppressWarnings("checkstyle:FinalParameters")
  @Override
  public void onError(Exception e) {}

  /**
   * Set the function called when we receive an OCPP Message.
   *
   * @param onReceiveMessageListener The Received OCPPMessage.
   */
  public void setOnReceiveMessage(final OnOCPPMessageListener onReceiveMessageListener) {
    this.onReceiveMessage = onReceiveMessageListener;
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
  public OCPPMessage popMessage() throws OCPPMessageFailureException, InterruptedException {
    return queue.popMessage(this);
  }

  /** Pop the entire send queue. */
  public void popAllMessages() throws OCPPMessageFailureException, InterruptedException {
    queue.popAllMessages(this);
  }
}
