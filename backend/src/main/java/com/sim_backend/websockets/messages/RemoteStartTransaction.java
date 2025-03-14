package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.messages.SetChargingProfile.*;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** OCPP 1.6 RemoteStartTransaction Request */
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_REQUEST,
    messageName = "RemoteStartTransaction")
public class RemoteStartTransaction extends OCPPMessageRequest implements Cloneable {
  @NotBlank(message = "idTag is required and cannot be blank")
  @Size(max = 20, message = "RemoteStartTransaction idTag must not exceed 20 characters")
  @SerializedName("idTag")
  private final String idTag;

  @Min(value = 0, message = "RemoteStartTransaction connectorId must be greater than or equal to 0")
  @SerializedName("connectorId")
  private final Integer connectorId; // Optional field

  @SerializedName("chargingProfile")
  private final ChargingProfile chargingProfile; // Charging profile (Optional)

  // Constructor
  public RemoteStartTransaction(
      @NotNull String idTag, Integer connectorId, ChargingProfile chargingProfile) {
    this.idTag = idTag;
    this.connectorId = connectorId;
    this.chargingProfile = chargingProfile;
  }

  @Override
  protected RemoteStartTransaction clone() {
    return (RemoteStartTransaction) super.clone();
  }
}
