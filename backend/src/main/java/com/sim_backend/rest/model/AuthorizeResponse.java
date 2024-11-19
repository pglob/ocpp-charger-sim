package com.sim_backend.rest.model;

import com.google.gson.annotations.SerializedName;

@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "AuthorizeResponse")
public class AuthorizeResponse extends OCPPMessage {

  @SerializedName("status")
  private String status;

  // Constructor
  public AuthorizeResponse(String status) {
    super();
    this.status = status;
  }

  // Getter
  public String getStatus() {
    return status;
  }

  // Setter
  public void setStatus(String status) {
    this.status = status;
  }
}
