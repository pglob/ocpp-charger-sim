package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an OCPP 1.6 Authorize Request sent by a Charge Point to request authorization for a
 * given idTag.
 */
@Getter
@Setter
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "Authorize")
public class Authorize extends OCPPMessageRequest {

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

  public String generateIdTag() {
    // Generate a UUID
    String uuid = UUID.randomUUID().toString();
    // Remove hyphens and truncate to 20 characters
    return uuid.replace("-", "").substring(0, 20);
  }
}
