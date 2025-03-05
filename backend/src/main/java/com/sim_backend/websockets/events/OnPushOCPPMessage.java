package com.sim_backend.websockets.events;

import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.types.OCPPMessage;
import lombok.Getter;

/** An event for when we push an OCPP message. */
@Getter
public class OnPushOCPPMessage {
  /** The pushed ocpp message. */
  private final OCPPMessage message;

  /** The websocket client we pushed this message from. */
  private final OCPPWebSocketClient client;

  /**
   * Create a OnPushOCPPMessage event.
   *
   * @param inMessage The pushed message.
   * @param inClient The client we received this message on.
   */
  public OnPushOCPPMessage(final OCPPMessage inMessage, final OCPPWebSocketClient inClient) {
    this.message = inMessage;
    this.client = inClient;
  }
}
