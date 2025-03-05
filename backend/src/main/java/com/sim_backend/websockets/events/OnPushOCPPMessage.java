package com.sim_backend.websockets.events;

import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.types.OCPPMessage;
import lombok.Getter;

/** An event for when we receive an OCPP message. */
@Getter
public class OnPushOCPPMessage {
  /** The received ocpp message. */
  private final OCPPMessage message;

  /** The websocket client we received this message from. */
  private final OCPPWebSocketClient client;

  /**
   * Create an OCPPMessage event.
   *
   * @param inMessage The received message.
   * @param inClient The client we received this message on.
   */
  public OnPushOCPPMessage(final OCPPMessage inMessage, final OCPPWebSocketClient inClient) {
    this.message = inMessage;
    this.client = inClient;
  }
}
