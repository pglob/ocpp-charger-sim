package com.sim_backend.websockets.exceptions;

/** A bad class was given to OCPPWebSocketClient */
public class OCPPBadClass extends RuntimeException {
  /**
   * Get a String resetation of an exception thrown when an OCPPWebSocketClient is provided a bad
   * class.
   *
   * @return The exception as a string.
   */
  @Override
  public String getMessage() {
    return "Bad Class supplied to OCPPWebsocketClient";
  }
}
