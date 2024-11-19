package com.sim_backend.websockets.messages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sim_backend.websockets.OCPPMessage;
import com.sim_backend.websockets.OCPPMessageInfo;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "HeartBeatResponse")
public final class HeartBeatResponseMessage extends OCPPMessage {

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", timezone = "UTC")
  private final ZonedDateTime currentTime;

  /** The response message for a HeartBeat, currentTime will be set to now. */
  public HeartBeatResponseMessage() {
    super();
    this.currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
  }

  /**
   * The Response Message for a HeartBeat.
   *
   * @param time A Provided Time.
   */
  public HeartBeatResponseMessage(final ZonedDateTime time) {
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
