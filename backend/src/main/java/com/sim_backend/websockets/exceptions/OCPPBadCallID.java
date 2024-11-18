package com.sim_backend.websockets.exceptions;

public class OCPPBadCallID extends RuntimeException {
  /** The calL ID we could not process. */
  private final int badCallID;

  /** The full message we received. */
  private final String message;

  /**
   * An exception thrown when we receive a call ID we cannot process.
   *
   * @param badCall The bad call ID.
   * @param msg The full message.
   */
  public OCPPBadCallID(final int badCall, final String msg) {
    this.badCallID = badCall;
    this.message = msg;
  }

  /**
   * Get the full message.
   *
   * @return The full OCPP message we received.
   */
  public String getFullMessage() {
    return message;
  }

  /**
   * The callID we cannot process.
   *
   * @return The bad call ID.
   */
  public int getBadCallID() {
    return badCallID;
  }
}
