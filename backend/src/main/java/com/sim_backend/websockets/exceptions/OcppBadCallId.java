package com.sim_backend.websockets.exceptions;

/** Thrown when we receive a CallID we cannot process or is invalid. */
public class OcppBadCallId extends RuntimeException {
  /** The calL ID we could not process. */
  private final int badCallId;

  /** The full message we received. */
  private final String message;

  /**
   * An exception thrown when we receive a call ID we cannot process.
   *
   * @param badCall The bad call ID.
   * @param msg The full message.
   */
  public OcppBadCallId(final int badCall, final String msg) {
    this.badCallId = badCall;
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
  public int getBadCallId() {
    return badCallId;
  }
}
