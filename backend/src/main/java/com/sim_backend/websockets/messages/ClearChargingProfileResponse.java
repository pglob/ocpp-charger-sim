/**
 * Represents an OCPP 1.6 Clear Charging Profile Response sent by a Charge Point in response to a
 * clear charging profile request.
 */
package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ClearProfileStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** A OCPP Clear Charging Profile Response Message. */
@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "ClearChargingProfileResponse")
public final class ClearChargingProfileResponse extends OCPPMessageResponse {

  /** The status of the clear charging profile request. */
  @SerializedName("status")
  @NotNull(message = "ClearChargingProfile status is required")
  private final ClearProfileStatus status;
}
