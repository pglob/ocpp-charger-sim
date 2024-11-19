package com.sim_backend.websockets.exceptions;

import com.sim_backend.websockets.types.OCPPMessage;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

/** Thrown when we could not send a message after a certain number of reattempts. */
public class OCPPMessageFailure extends Exception {

  /** The Inner WebsocketException. */
  private final WebsocketNotConnectedException innerException;

  /** The failed Message. */
  private final OCPPMessage failedMessage;

  /**
   * A wrapper for a WebsocketNotConnectedException.
   *
   * @param message The message.
   * @param innerExp The inner exception.
   */
  public OCPPMessageFailure(
      final OCPPMessage message, final WebsocketNotConnectedException innerExp) {
    super(message.toString());
    this.failedMessage = message;
    innerException = innerExp;
  }

  /**
   * Get the Inner WebsocketException.
   *
   * @return The WebsocketNotConnectedException.
   */
  public WebsocketNotConnectedException getInnerException() {
    return innerException;
  }

  /**
   * Get the failed Message.
   *
   * @return The OCPPMessage that failed to send.
   */
  public OCPPMessage getFailedMessage() {
    return failedMessage;
  }

  /**
   * Get a String representation of an exception thrown when we cannot connect to our websocket
   * server.
   *
   * @return The exception as a string.
   */
  @Override
  public String getMessage() {
    return String.format("%s: %s", failedMessage.toString(), innerException.getMessage());
  }
}
