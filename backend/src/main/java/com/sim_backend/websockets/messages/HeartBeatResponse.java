package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents an OCPP 1.6 Heartbeat Response sent by the Central System to acknowledge a Heartbeat
 * Request and provide the current server time.
 */
@AllArgsConstructor
@Getter
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "HeartBeatResponse")
public final class HeartBeatResponse extends OCPPMessageResponse {

  /** The HeartBeat's time. */
  @SerializedName("currentTime")
  private final ZonedDateTime currentTime;

  /** The response message for a HeartBeat, currentTime will be set to now. */
  public HeartBeatResponse() {
    super();
    this.currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
  }
}
