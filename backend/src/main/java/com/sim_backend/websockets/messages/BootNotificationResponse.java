package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.RegistrationStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an OCPP 1.6 Boot Notification Response sent by the Central System to acknowledge a
 * Boot Notification Request and provide registration status and interval.
 */
@Getter
@Setter
@AllArgsConstructor
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "BootNotificationResponse")
@EqualsAndHashCode(callSuper = true)
public class BootNotificationResponse extends OCPPMessageResponse {

  /** Status of the BootNotification (Accepted, Pending, Rejected). Required. */
  @NotNull(message = "Registration status is required")
  @SerializedName("status")
  private RegistrationStatus status;

  /** Current time in ISO 8601 format. Required. */
  @NotNull(message = "Current time is required")
  @SerializedName("currentTime")
  private ZonedDateTime currentTime;

  /** Interval (time in seconds) for next BootNotification. Must be positive. */
  @NotNull(message = "Interval is required")
  @SerializedName("interval")
  private int interval;

  public BootNotificationResponse(String status, ZonedDateTime currentTime, int interval) {
    this.status = RegistrationStatus.fromString(status);
    this.currentTime = currentTime;
    this.interval = interval;
  }
}
