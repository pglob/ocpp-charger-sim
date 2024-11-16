package com.sim_backend.exceptions;

import com.sim_backend.websockets.messages.OCPPMessage;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

public class OCPPMessageFailureException extends RuntimeException {

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
  public OCPPMessageFailureException(
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
}
