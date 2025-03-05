package com.sim_backend.websockets.events;

/** A Listener for an OnOCPPMessage event. */
public interface OnPushOCPPMessageListener {
  /**
   * The method to be called when a response message is received.
   *
   * @param message The OCPP message event.
   */
  void onPush(OnPushOCPPMessage message);
}
