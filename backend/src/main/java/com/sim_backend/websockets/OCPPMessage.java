package com.sim_backend.websockets;

import com.google.gson.JsonArray;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public abstract class OCPPMessage {

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
  public JsonArray generateMessage() {
    assert this.getClass().isAnnotationPresent(OCPPMessageInfo.class);
    OCPPMessageInfo messageInfo = this.getClass().getAnnotation(OCPPMessageInfo.class);
    JsonArray array = new JsonArray();
    array.add(messageInfo.messageCallID());
    array.add(this.messageID);
    array.add(messageInfo.messageName());
    // HeartBeat request requires an empty JSON array, has no JSON fields itself
    if (messageInfo.messageName().equals("HeartBeat")){
      array.add(new JsonArray());
    }
    else {
      array.add(GsonUtilities.getGson().toJsonTree(this));
    }
    return array;
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
   * Generates a unique 36 character UUID
   *
   * @return A randomly generated message ID.
   */
  @NotNull
  private static String generateMessageID() {
    return UUID.randomUUID().toString();
  }
}
