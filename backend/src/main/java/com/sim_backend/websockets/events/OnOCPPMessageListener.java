package com.sim_backend.websockets.events;

/** A Listener for an OnOCPPMessage event. */
public interface OnOCPPMessageListener {
  /**
   * The method to be called when a response message is received.
   *
   * @param message The OCPP message event.
   */
  void onMessageReceived(OnOCPPMessage message);

  /**
   * Called when a response message times out.
   *
   * <p>Default implementation does nothing.
   */
  default void onTimeout() {
    // Default: do nothing
  }
}
