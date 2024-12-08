package com.sim_backend.websockets.events;

import com.sim_backend.websockets.messages.AuthorizeResponse;

/** A Listener for an OnOCPPMessage event. */
public interface OnOCPPMessageListener {
  /**
   * The method to be called.
   *
   * @param message The OCPP message event.
   */
  void onMessageReceived(OnOCPPMessage message);

  void onAuthorizeReceived(AuthorizeResponse response);
}
