package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an OCPP 1.6 Authorize Request sent by a Charge Point to request authorization for a
 * given idTag.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "Authorize")
public class Authorize extends OCPPMessageRequest {

  @SerializedName("idTag")
  @NotBlank(message = "Authorize idTag must not be blank")
  @Size(max = 20, message = "Authorize idTag must be at most 20 characters")
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

  private String generateIdTag() {
    // Generate a UUID, remove hyphens, and truncate to 20 characters
    return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
  }
}
