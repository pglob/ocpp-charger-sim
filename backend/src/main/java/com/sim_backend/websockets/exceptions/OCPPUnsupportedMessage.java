package com.sim_backend.websockets.exceptions;

/** Thrown when we receive an OCPP message name we cannot serialize. */
public class OCPPUnsupportedMessage extends Exception {

  /** The OCPP message that failed to process. */
  private final String message;

  /** The failed Message Name. */
  private final String messageName;

  /**
   * An Exception thrown when we fail to process an OCPPMessage.
   *
   * @param msg The message that could not be processed.
   */
  public OCPPUnsupportedMessage(final String msg, final String msgName) {
    this.message = msg;
    this.messageName = msgName;
  }

  /**
   * Get the received message that failed to process.
   *
   * @return The failed message.
   */
  public String getFullMessage() {
    return message;
  }

  /**
   * Get the message name of message we do not support.
   *
   * @return The Failed message name.
   */
  public String getMessageName() {
    return messageName;
  }

  /**
   * Get a String representation of an exception thrown when we get a Message Name we do not
   * support.
   *
   * @return The exception as a string.
   */
  @Override
  public String getMessage() {
    return String.format("Received unknown message name \"%s\": %s", messageName, message);
  }
}
