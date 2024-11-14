package com.sim_backend.websockets;

import com.google.gson.JsonArray;
import java.security.SecureRandom;
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
  public JsonArray generateMessage() {
    assert this.getClass().isAnnotationPresent(OCPPMessageInfo.class);
    OCPPMessageInfo messageInfo = this.getClass().getAnnotation(OCPPMessageInfo.class);
    JsonArray array = new JsonArray();
    array.add(messageInfo.messageCallID());
    array.add(this.messageID);
    array.add(messageInfo.messageName());
    array.add(GsonUtilities.getGson().toJsonTree(this));
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

  /** The Characters we are allowed in a Message ID. */
  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  /** Our random generator. */
  private static final SecureRandom RANDOM = new SecureRandom();

  /**
   * Generates a message ID of a given length.
   *
   * @return A randomly generated message ID.
   */
  @NotNull
  private static String generateMessageID() {
    StringBuilder sb = new StringBuilder(OCPPMessage.MAX_MESSAGE_ID_LENGTH);
    for (int i = 0; i < OCPPMessage.MAX_MESSAGE_ID_LENGTH; i++) {
      int randomIndex = RANDOM.nextInt(CHARACTERS.length());
      sb.append(CHARACTERS.charAt(randomIndex));
    }
    return sb.toString();
  }
}
