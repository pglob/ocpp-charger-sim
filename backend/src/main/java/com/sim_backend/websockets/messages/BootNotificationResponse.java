package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.RegistrationStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
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

  @SerializedName("status")
  private RegistrationStatus status; // Status of the BootNotification (Accepted, Rejected)

  @SerializedName("currentTime")
  private ZonedDateTime currentTime; // Current time in ISO 8601 format

  @SerializedName("interval")
  private int interval; // Interval (time in seconds) for next BootNotification

  public BootNotificationResponse(String status, ZonedDateTime currentTime, int interval) {
    this.status = RegistrationStatus.fromString(status);
    this.currentTime = currentTime;
    this.interval = interval;
  }
}
