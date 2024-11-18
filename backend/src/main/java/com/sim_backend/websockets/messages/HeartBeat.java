package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OcppMessageInfo;
import com.sim_backend.websockets.types.OcppMessageRequest;

/** An OCPP HeartBeat Message. */
@OcppMessageInfo(messageName = "HeartBeat")
public final class HeartBeat extends OcppMessageRequest {
  /** A HeartBeat Message. */
  public HeartBeat() {
    super();
  }
}
