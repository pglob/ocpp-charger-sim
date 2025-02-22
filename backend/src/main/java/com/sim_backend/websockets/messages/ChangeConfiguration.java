package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/** Represents an OCPP 1.6 ChangeConfiguration Request from Central System to a Charge Point */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "ChangeConfiguration")
public class ChangeConfiguration extends OCPPMessageRequest implements Cloneable {
  @Size(max = 50, message = "ChangeConfiguration key must not exceed 50 characters")
  @NotBlank(message = "ChangeConfiguration key is required")
  @SerializedName("key")
  private final String key;

  @Size(max = 500, message = "ChangeConfiguration value must not exceed 500 characters")
  @NotBlank(message = "ChangeConfiguration value is required")
  @SerializedName("value")
  private final String value;

  // Constructor
  public ChangeConfiguration(String key, String value) {
    this.key = key;
    this.value = value;
  }

  @Override
  protected ChangeConfiguration clone() {
    return (ChangeConfiguration) super.clone();
  }
}
