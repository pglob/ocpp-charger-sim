package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ConfigurationStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an OCPP 1.6 ChangeConfiguration Response from a Charge Point to the Central System to
 * confirm the key
 */
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "ChangeConfigurationResponse")
public class ChangeConfigurationResponse extends OCPPMessageResponse {

  @SerializedName("status")
  private ConfigurationStatus status; // Accepted, NotSupported, Rejected

  public ChangeConfigurationResponse(String status) {
    this.status = ConfigurationStatus.fromString(status);
  }
}
