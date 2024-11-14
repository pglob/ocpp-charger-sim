package com.sim_backend.websockets;

public interface OnOCPPMessageListener {
  /**
   * The method to be called.
   *
   * @param message The OCPP message event.
   */
  void onMessageReceieved(OnOCPPMessage message);
}
