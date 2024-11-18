package com.sim_backend.websockets.events;

/** A Listener for an OnOCPPMessage event. */
public interface OnOcppMessageListener {
  /**
   * The method to be called.
   *
   * @param message The OCPP message event.
   */
  void onMessageReceieved(OnOcppMessage message);
}
