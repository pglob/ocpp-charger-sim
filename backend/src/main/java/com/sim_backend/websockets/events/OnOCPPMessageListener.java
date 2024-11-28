package com.sim_backend.websockets.events;

/** A Listener for an OnOCPPMessage event. */
public interface OnOCPPMessageListener {
  /**
   * The method to be called.
   *
   * @param message The OCPP message event.
   */
  void onMessageReceived(OnOCPPMessage message);
}
