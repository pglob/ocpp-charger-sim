package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.OCPPMessageInfo;

@OCPPMessageInfo(messageType = "HeartBeat", messageName = "HeartBeat")
public final class HeartBeat extends OCPPMessage {
  /***
   * A HeartBeat Message.
   */
  public HeartBeat() {
    super();
  }
}
