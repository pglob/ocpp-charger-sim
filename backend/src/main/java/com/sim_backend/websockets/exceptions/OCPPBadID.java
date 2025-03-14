package com.sim_backend.websockets.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OCPPBadID extends Exception {
  /** The calL ID we could not process. */
  private final String badID;

  /** The full message we received. */
  private final String message;

  /**
   * Get a string representation nof a OCPPBadID.
   *
   * @return The exception as a string.
   */
  @Override
  public String getMessage() {
    return String.format("Bad ID %s: %s", badID, message);
  }
}
