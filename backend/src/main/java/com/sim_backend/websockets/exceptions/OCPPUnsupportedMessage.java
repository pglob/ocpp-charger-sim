package com.sim_backend.websockets.exceptions;

public class OCPPUnsupportedMessage extends RuntimeException {

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
  public String getMessage() {
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
}
