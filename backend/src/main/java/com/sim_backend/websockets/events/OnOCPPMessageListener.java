package com.sim_backend.websockets.events;

public interface OnOCPPMessageListener {
  /**
   * The method to be called.
   *
   * @param message The OCPP message event.
   */
  void onMessageReceieved(OnOCPPMessage message);
}
