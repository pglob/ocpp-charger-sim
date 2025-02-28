package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ChargingProfileStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/** Represents a response for setting a charging profile. */
@Getter
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "SetChargingProfileResponse")
public class SetChargingProfileResponse extends OCPPMessageResponse {
  public SetChargingProfileResponse(SetChargingProfile profile, ChargingProfileStatus status) {
    super(profile);
    this.status = status;
  }

  /** The status of the charging profile response. */
  @NotNull(message = "SetChargingProfileResponse status is required and cannot be blank")
  private final ChargingProfileStatus status;
}
