package com.sim_backend.rest.model;

import com.google.gson.annotations.SerializedName;

@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "BootNotificationResponse")
public class BootNotificationResponse extends OCPPMessage {

  @SerializedName("status")
  private String status; // Status of the BootNotification (Accepted, Rejected)

  @SerializedName("interval")
  private int interval; // Interval (time in seconds) for next BootNotification

  // Constructor, getters, setters
  public BootNotificationResponse(String status, int interval) {
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
