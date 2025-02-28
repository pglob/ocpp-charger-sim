package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ConfigurationStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an OCPP 1.6 ChangeConfiguration Response from a Charge Point to the Central System to
 * confirm the key
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "ChangeConfigurationResponse")
public class ChangeConfigurationResponse extends OCPPMessageResponse implements Cloneable {
  @NotNull(message = "ChangeConfigurationResponse status is required")
  @SerializedName("status")
  private ConfigurationStatus status; // Accepted, NotSupported, Rejected

  public ChangeConfigurationResponse(ChangeConfiguration request, String status) {
    super(request);
    this.status = ConfigurationStatus.fromString(status);
  }

  @Override
  protected ChangeConfigurationResponse clone() {
    return (ChangeConfigurationResponse) super.clone();
  }
}
