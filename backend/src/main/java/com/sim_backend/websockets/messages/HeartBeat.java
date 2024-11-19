package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessageRequest;

/** An OCPP HeartBeat Message. */
@OCPPMessageInfo(messageName = "HeartBeat")
public final class HeartBeat extends OCPPMessageRequest {
  /** A HeartBeat Message. */
  public HeartBeat() {
    super();
  }
}
