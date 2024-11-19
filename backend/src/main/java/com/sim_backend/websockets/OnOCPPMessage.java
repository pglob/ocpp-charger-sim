package com.sim_backend.websockets;

public class OnOCPPMessage {
  /** The received ocpp message. */
  private final OCPPMessage message;

  /**
   * Create an OCPPMessage event.
   *
   * @param inMessage The received message.
   */
  protected OnOCPPMessage(final OCPPMessage inMessage) {
    this.message = inMessage;
  }

  /**
   * Get the received OCPPMessage.
   *
   * @return The received message.
   */
  OCPPMessage getMessage() {
    return message;
  }
}
