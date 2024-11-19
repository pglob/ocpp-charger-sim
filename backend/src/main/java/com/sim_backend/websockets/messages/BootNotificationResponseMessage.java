package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.OCPPMessage;
import com.sim_backend.websockets.OCPPMessageInfo;

@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "BootNotificationResponse")
public class BootNotificationResponseMessage extends OCPPMessage {

  @SerializedName("status")
  private String status; // Status of the BootNotification (Accepted, Rejected)

  @SerializedName("interval")
  private int interval; // Interval (time in seconds) for next BootNotification

  // Constructor, getters, setters
  public BootNotificationResponseMessage(String status, int interval) {
    super();
    this.status = status;
    this.interval = interval;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }
}
