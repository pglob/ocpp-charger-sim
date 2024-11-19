package com.sim_backend.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "HeartBeatResponse")
public final class HeartBeatResponse extends OCPPMessage {

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", timezone = "UTC")
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
