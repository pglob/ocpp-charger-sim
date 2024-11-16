package com.sim_backend.exceptions;

public class OCPPUnsupportedMessageException extends RuntimeException {

  /** The OCPP message that failed to process. */
  private final String message;

  /** The failed Message Type. */
  private final String messageType;

  /**
   * An Exception thrown when we fail to process an OCPPMessage.
   *
   * @param msg The message that could not be processed.
   */
  public OCPPUnsupportedMessageException(final String msg, final String msgType) {
    this.message = msg;
    this.messageType = msgType;
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
   * Get the message type of message we do not support.
   *
   * @return The Failed message type.
   */
  public String getMessageType() {
    return messageType;
  }
}
