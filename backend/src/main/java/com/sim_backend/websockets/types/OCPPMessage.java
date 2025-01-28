package com.sim_backend.websockets.types;

import static com.sim_backend.websockets.OCPPWebSocketClient.MESSAGE_PACKAGE;

import com.google.gson.JsonArray;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

/** An OCPP message. */
@EqualsAndHashCode(exclude = "messageID", callSuper = false)
public abstract class OCPPMessage {
  /** The call ID for a request. */
  public static final int CALL_ID_REQUEST = 2;

  /** The call ID for a response. */
  public static final int CALL_ID_RESPONSE = 3;

  /** The call ID for an error. */
  public static final int CALL_ID_ERROR = 4;

  /** Number of attempts to send this message. */
  private transient int tries = 0;

  /** The Message ID we send this message with. */
  @Getter @Setter protected transient String messageID;

  /** The constructor for an OCPP message. */
  protected OCPPMessage() {
    this.messageID = generateMessageID();
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
  public void sendMessage(final OCPPWebSocketClient client) {
    client.send(this.toJsonString());
    if (this instanceof OCPPMessageRequest) {
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
   * Generates a 36 Character UUID
   *
   * @return A randomly generated message ID.
   */
  @NotNull
  private static String generateMessageID() {
    return UUID.randomUUID().toString();
  }

  /** Refreshes a message, resetting its message ID to something new. */
  public void refreshMessage() {
    this.messageID = generateMessageID();
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
    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(OCPPMessageInfo.class);

    // Find the one that matches the received message Type.
    for (Class<?> messageClass : classes) {
      OCPPMessageInfo annotation = messageClass.getAnnotation(OCPPMessageInfo.class);
      // Check if it's a has a parent class of OCPPMessage.
      if (OCPPMessage.class.isAssignableFrom(messageClass)
          && annotation.messageName().equals(messageName)) {
        // Convert the payload String into the found class.
        return messageClass;
      }
    }
    return null;
  }

  /**
   * Get a JSON representation of a OCPP Message.
   *
   * @return The message in a json string.
   */
  public String toJsonString() {
    return GsonUtilities.toString(this.generateMessage());
  }

  /**
   * Get a String representation of a OCPP message.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return String.format("%s = %s", this.getClass(), this.toJsonString());
  }

}
