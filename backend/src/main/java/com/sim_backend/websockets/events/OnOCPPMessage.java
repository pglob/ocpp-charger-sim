package com.sim_backend.websockets.events;

import com.sim_backend.websockets.types.OCPPMessage;
import lombok.Getter;

/** An event for when we receive an OCPP message. */
@Getter
public class OnOCPPMessage {
  /** The received ocpp message. */
  private final OCPPMessage message;

  /**
   * Create an OCPPMessage event.
   *
   * @param inMessage The received message.
   */
  public OnOCPPMessage(final OCPPMessage inMessage) {
    this.message = inMessage;
  }
}
