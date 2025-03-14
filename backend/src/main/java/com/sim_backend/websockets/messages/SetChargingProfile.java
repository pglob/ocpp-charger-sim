package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.ChargingProfile;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Represents a request to set a charging profile for an EV charging station. */
@Getter
@AllArgsConstructor
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "SetChargingProfile")
public class SetChargingProfile extends OCPPMessageRequest {
  /** The ID of the connector to which the profile applies. */
  @NotNull(message = "SetChargingProfile connectorId is required")
  @SerializedName("connectorId")
  private int connectorId;

  /** The charging profile details. */
  @NotNull(message = "SetChargingProfile csChargingProfiles is required")
  @SerializedName("csChargingProfiles")
  private ChargingProfile csChargingProfiles;
}
