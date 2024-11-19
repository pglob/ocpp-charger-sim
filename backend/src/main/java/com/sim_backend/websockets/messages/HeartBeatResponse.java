package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** An OCPP HeartBeatResponse message. */
@OCPPMessageInfo(messageName = "HeartBeatResponse", messageCallID = OCPPMessage.CALL_ID_RESPONSE)
public final class HeartBeatResponse extends OCPPMessageResponse {

  /** The HeartBeat's time. */
  private final ZonedDateTime currentTime;

  /** The response message for a HeartBeat, currentTime will be set to now. */
  public HeartBeatResponse() {
    super();
    this.currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
  }

  /**
   * The Response Message for a HeartBeat.
   *
   * @param time A Provided Time.
   */
  public HeartBeatResponse(final ZonedDateTime time) {
    super();
    this.currentTime = time;
  }

  /**
   * The HeartBeat's time.
   *
   * @return The current time in this object.
   */
  public ZonedDateTime getCurrentTime() {
    return currentTime;
  }
}
