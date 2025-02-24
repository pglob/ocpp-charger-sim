/**
 * Represents an OCPP 1.6 Clear Charging Profile Request sent by a Charge Point to clear a charging
 * profile.
 */
package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ChargingProfilePurpose;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** A OCPP Clear Charging Profile Request Message. */
@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "ClearChargingProfile")
public final class ClearChargingProfile extends OCPPMessageRequest {

  /** The ID of the charging profile to clear. */
  @SerializedName("id")
  private final Integer id;

  /** The ID of the connector for which to clear profiles. */
  @Min(value = 0, message = "ClearChargingProfile connectorId must be a non-negative integer")
  @SerializedName("connectorId")
  private final Integer connectorId;

  /** The purpose of the charging profile to clear. */
  @SerializedName("chargingProfilePurpose")
  private final ChargingProfilePurpose chargingProfilePurpose;

  /** The stack level of the charging profile to clear. */
  @Min(value = 0, message = "ClearChargingProfile stackLevel must be a non-negative integer")
  @SerializedName("stackLevel")
  private final Integer stackLevel;
}
