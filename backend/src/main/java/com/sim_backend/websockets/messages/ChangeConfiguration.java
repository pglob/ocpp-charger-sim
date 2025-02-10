package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/** Represents an OCPP 1.6 ChangeConfiguration Request from Central System to a Charge Point */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "ChangeConfiguration")
public class ChangeConfiguration extends OCPPMessageRequest {
  @NotBlank(message = "ChangeConfiguration key is required")
  @SerializedName("key")
  private final String key;

  @NotBlank(message = "ChangeConfiguration value is required")
  @SerializedName("value")
  private final String value;

  // Constructor
  public ChangeConfiguration(String key, String value) {
    this.key = key;
    this.value = value;
  }
}
