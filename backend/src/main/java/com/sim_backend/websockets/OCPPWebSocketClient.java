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
import java.util.Dictionary;
import java.util.Hashtable;
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
  private OnOCPPMessageListener onReceiveMessage;

  /** The previous messages we have sent. * */
  private final Dictionary<String, OCPPMessage> previousMessages = new Hashtable<>();

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

      if (!element.isJsonArray()) {
        throw new JsonParseException("Expected array got " + element.toString());
      }

      JsonArray array = element.getAsJsonArray();
      String msgID = array.get(MESSAGE_ID_INDEX).getAsString();
      String messageName = null;
      JsonObject data = null;

      int callID = array.get(CALL_ID_INDEX).getAsInt();
      if (callID == OCPPMessage.CALL_ID_REQUEST) {
        messageName = array.get(NAME_INDEX).getAsString();
        data = array.get(PAYLOAD_INDEX).getAsJsonObject();
      } else if (callID == OCPPMessage.CALL_ID_RESPONSE) {
        if (this.previousMessages.get(msgID) == null) {
          throw new OCPPCannotProcessResponse(s, msgID);
        }

        OCPPMessageInfo info =
            this.previousMessages.remove(msgID).getClass().getAnnotation(OCPPMessageInfo.class);
        messageName = info.messageName() + "Response";
        data = array.get(PAYLOAD_INDEX - 1).getAsJsonObject();
      } else if (callID == OCPPMessage.CALL_ID_ERROR) {
        OCPPMessageError error = new OCPPMessageError(array);
        onReceiveMessage.onMessageReceieved(new OnOCPPMessage(error));
        return;
      } else {
        throw new OCPPBadCallID(callID, s);
      }

      if (messageName == null) {
        throw new OCPPUnsupportedMessage(s, "null");
      }
      Class<?> messageClass = OCPPMessage.getMessageByName(messageName);
      if (messageClass == null) {
        throw new OCPPUnsupportedMessage(s, messageName);
      }

      OCPPMessage message = (OCPPMessage) gson.fromJson(data, messageClass);
      message.setMessageID(msgID);
      onReceiveMessage.onMessageReceieved(new OnOCPPMessage(message));
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
  public OCPPMessage popMessage() throws OCPPMessageFailure, InterruptedException {
    OCPPMessage message = queue.popMessage(this);

    if (message != null) {
      this.addMessageToPreviousMessage(message);
    }
    return message;
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
  public void addMessageToPreviousMessage(final OCPPMessage msg) {
    this.previousMessages.put(msg.getMessageID(), msg);
  }
}
