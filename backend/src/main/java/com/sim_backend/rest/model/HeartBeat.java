package com.sim_backend.rest.model;

@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "HeartBeat")
public final class HeartBeat extends OCPPMessage {
  /***
   * A HeartBeat Message.
   */
  public HeartBeat() {
    super();
  }
}
