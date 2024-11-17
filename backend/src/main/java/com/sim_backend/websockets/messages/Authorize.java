package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.OCPPMessage;
import com.sim_backend.websockets.OCPPMessageInfo;
import java.util.UUID;

@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "Authorize")
public class Authorize extends OCPPMessage {

  @SerializedName("idTag")
  private String idTag;

  // Constructor
  public Authorize(String idTag) {
    super();
    this.idTag = idTag;
  }

  // Constructor
  public Authorize() {
    super();
    this.idTag = generateIdTag();
  }

  // Getter and Setter
  public String getIdTag() {
    return idTag;
  }

  public void setIdTag(String idTag) {
    this.idTag = idTag;
  }

  public String generateIdTag() {
    // Generate a UUID
    String uuid = UUID.randomUUID().toString();
    // Remove hyphens and truncate to 20 characters
    return uuid.replace("-", "").substring(0, 20);
  }
}
