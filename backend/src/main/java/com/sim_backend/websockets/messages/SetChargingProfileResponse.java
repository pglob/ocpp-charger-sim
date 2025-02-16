package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ChargingProfileStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Represents a response for setting a charging profile. */
@Getter
@AllArgsConstructor
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "SetChargingProfileResponse")
public class SetChargingProfileResponse {

  /** The status of the charging profile response. */
  @NotBlank(message = "SetChargingProfileResponse status is required and cannot be blank")
  private ChargingProfileStatus status;
}
