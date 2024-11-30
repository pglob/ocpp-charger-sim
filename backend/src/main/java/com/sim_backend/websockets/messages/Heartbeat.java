package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;

/**
 * Represents an OCPP 1.6 Heartbeat Request sent by a Charge Point to notify the Central System that
 * it is operational and connected.
 */
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "Heartbeat")
public final class Heartbeat extends OCPPMessageRequest {
  /***
   * A Heartbeat Message.
   */
  public Heartbeat() {
    super();
  }
}
