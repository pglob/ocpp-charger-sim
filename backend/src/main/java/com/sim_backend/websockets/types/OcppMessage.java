package com.sim_backend.websockets.types;

import static com.sim_backend.websockets.OcppWebSocketClient.MESSAGE_PACKAGE;

import com.google.gson.JsonArray;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.OcppWebSocketClient;
import com.sim_backend.websockets.annotations.OcppMessageInfo;
import java.security.SecureRandom;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

/** An OCPP message. */
public abstract class OcppMessage {
  /** The maximum allowed message ID length. */
  public static final int MAX_MESSAGE_ID_LENGTH = 20;

  /** The call ID for a request. */
  public static final int CALL_ID_REQUEST = 2;

  /** The call ID for a response. */
  public static final int CALL_ID_RESPONSE = 3;

  /** The call ID for an error. */
  public static final int CALL_ID_ERROR = 4;

  /** Number of attempts to send this message. */
  private transient int tries = 0;

  /** The Message ID we send this message with. */
  protected transient String messageId;

  /** The constructor for an OCPP message. */
  protected OcppMessage() {
    this.messageId = generateMessageId();
  }

  /**
   * Create a JSON element representing the message.
   *
   * @return The generated message JSON.
   */
  public abstract JsonArray generateMessage();

  /**
   * Emit the message to the given client.
   *
   * @param client The websocket client.
   */
  public void sendMessage(final OcppWebSocketClient client) {
    client.send(GsonUtilities.toString(this.generateMessage()));
    if (this instanceof OcppMessageRequest) {
      client.addPreviousMessage(this);
    }
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
   * Get the message ID.
   *
   * @return the current message ID.
   */
  public String getMessageId() {
    return messageId;
  }

  /**
   * Set the current message ID.
   *
   * @param msgId Set the message ID.
   */
  public void setMessageId(final String msgId) {
    this.messageId = msgId;
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
  private static String generateMessageId() {
    StringBuilder sb = new StringBuilder(OcppMessage.MAX_MESSAGE_ID_LENGTH);
    for (int i = 0; i < OcppMessage.MAX_MESSAGE_ID_LENGTH; i++) {
      int randomIndex = RANDOM.nextInt(CHARACTERS.length());
      sb.append(CHARACTERS.charAt(randomIndex));
    }
    return sb.toString();
  }

  /**
   * Get a message Class by the message Name.
   *
   * @param messageName The message class name we are looking for.
   * @return The Found OCPPMessage Class or throws an exception if not found.
   */
  public static Class<?> getMessageByName(final String messageName) {
    Reflections reflections = new Reflections(MESSAGE_PACKAGE);

    // Get all classes in our messages package,
    // that are annotated with OCPPMessageInfo.
    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(OcppMessageInfo.class);

    // Find the one that matches the received message Type.
    for (Class<?> messageClass : classes) {
      OcppMessageInfo annotation = messageClass.getAnnotation(OcppMessageInfo.class);
      // Check if it's a has a parent class of OCPPMessage.
      if (OcppMessage.class.isAssignableFrom(messageClass)
          && annotation.messageName().equals(messageName)) {
        // Convert the payload String into the found class.
        return messageClass;
      }
    }
    return null;
  }
}
