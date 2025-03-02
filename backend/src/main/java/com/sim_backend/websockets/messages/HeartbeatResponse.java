package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotNull;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents an OCPP 1.6 Heartbeat Response sent by the Central System to acknowledge a Heartbeat
 * Request and provide the current server time.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "HeartbeatResponse")
public final class HeartbeatResponse extends OCPPMessageResponse implements Cloneable {

  /** The Heartbeat's time. */
  @NotNull(message = "HeartbeatResponse current time is required")
  @SerializedName("currentTime")
  private final ZonedDateTime currentTime;

  /** The response message for a Heartbeat, currentTime will be set to now. */
  public HeartbeatResponse(Heartbeat request) {
    super(request);
    this.currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
  }

  public HeartbeatResponse(Heartbeat request, ZonedDateTime currentTime) {
    super(request);
    this.currentTime = currentTime;
  }

  @Override
  protected HeartbeatResponse clone() {
    return (HeartbeatResponse) super.clone();
  }
}
