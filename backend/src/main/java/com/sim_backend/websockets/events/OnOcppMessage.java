package com.sim_backend.websockets.events;

import com.sim_backend.websockets.types.OcppMessage;

/** An event for when we receive an OCPP message. */
public class OnOcppMessage {
  /** The received ocpp message. */
  private final OcppMessage message;

  /**
   * Create an OCPPMessage event.
   *
   * @param inMessage The received message.
   */
  public OnOcppMessage(final OcppMessage inMessage) {
    this.message = inMessage;
  }

  /**
   * Get the received OCPPMessage.
   *
   * @return The received message.
   */
  public OcppMessage getMessage() {
    return message;
  }
}
