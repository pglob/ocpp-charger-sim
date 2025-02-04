package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotBlank;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents an OCPP 1.6 Heartbeat Response sent by the Central System to acknowledge a Heartbeat
 * Request and provide the current server time.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "HeartbeatResponse")
public final class HeartbeatResponse extends OCPPMessageResponse {

  /** The Heartbeat's time. */
  @NotBlank(message = "HeartbeatResponse current time is required")
  @SerializedName("currentTime")
  private final ZonedDateTime currentTime;

  /** The response message for a Heartbeat, currentTime will be set to now. */
  public HeartbeatResponse() {
    super();
    this.currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
  }
}
