package com.sim_backend.websockets.messages;

import com.google.gson.JsonObject;
import com.sim_backend.utils.GsonUtilities;
import com.sim_backend.websockets.OCPPMessageInfo;
import com.sim_backend.websockets.client.OCPPWebSocketClient;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public abstract class OCPPMessage {
  /** The maximum allowed message ID length. */
  public static final int MAX_MESSAGE_ID_LENGTH = 20;

  /** The call ID for a request. */
  public static final int CALL_ID_REQUEST = 2;

  /** The call ID for a response. */
  public static final int CALL_ID_RESPONSE = 3;

  /** Number of attempts to send this message. */
  private transient int tries = 0;

  /** The Message ID we send this message with. */
  private final transient String messageID;

  /** The constructor for an OCPP message. */
  protected OCPPMessage() {
    this.messageID = generateMessageID();
  }

  /**
   * Create a JSON element representing the message.
   *
   * @return The generated message JSON.
   */
  public JsonObject generateMessage() {
    assert this.getClass().isAnnotationPresent(OCPPMessageInfo.class);
    OCPPMessageInfo messageInfo = this.getClass().getAnnotation(OCPPMessageInfo.class);
    JsonObject response = new JsonObject();
    response.addProperty("messageCallId", messageInfo.messageCallID());
    response.addProperty("messageId", this.messageID);
    response.addProperty("messageName", messageInfo.messageName());
    response.addProperty("messageType", messageInfo.messageType());
    response.add("body", GsonUtilities.getGson().toJsonTree(this));
    return response;
  }

  /**
   * Emit the message to the given client.
   *
   * @param client The websocket client.
   */
  public void sendMessage(final OCPPWebSocketClient client) {
    client.send(GsonUtilities.toString(this.generateMessage()));
  }

  /**
   * Increment the number of tries this message has been sent.
   *
   * @return The new tries number.
   */
  public int incrementTries() {
    tries += 1;
    return tries;
  }

  /**
   * Generates a message ID of a given length.
   *
   * @return A randomly generated message ID.
   */
  @NotNull
  private static String generateMessageID() {
    return UUID.randomUUID().toString();
  }
}
