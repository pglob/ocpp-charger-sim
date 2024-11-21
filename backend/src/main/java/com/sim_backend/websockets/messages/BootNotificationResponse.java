/**
 * Represents an OCPP 1.6 Boot Notification Response sent by the Central System to acknowledge a
 * Boot Notification Request and provide registration status and interval.
 */
package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.OCPPMessage;
import com.sim_backend.websockets.OCPPMessageInfo;
import com.sim_backend.websockets.enums.BootStatus;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "BootNotificationResponse")
public class BootNotificationResponse extends OCPPMessage {

  @SerializedName("status")
  private BootStatus status; // Status of the BootNotification (Accepted, Rejected)

  @SerializedName("currentTime")
  private ZonedDateTime currentTime; // Current time in ISO 8601 format

  @SerializedName("interval")
  private int interval; // Interval (time in seconds) for next BootNotification

  public BootNotificationResponse(String status, ZonedDateTime currentTime, int interval) {
    this.status = BootStatus.fromString(status);
    this.currentTime = currentTime;
    this.interval = interval;
  }
}
