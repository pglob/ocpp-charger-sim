package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.OCPPMessage;
import com.sim_backend.websockets.OCPPMessageInfo;

@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "HeartBeat")
public final class HeartBeat extends OCPPMessage {
  /***
   * A HeartBeat Message.
   */
  public HeartBeat() {
    super();
  }

}
