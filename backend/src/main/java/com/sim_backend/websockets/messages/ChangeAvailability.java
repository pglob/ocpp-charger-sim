package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.enums.AvailabilityType;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * Represents an OCPP 1.6 ChangeAvailability Request sent to a Charge Point to change the current availability of a charger.
 */
@Getter
@AllArgsConstructor
public class ChangeAvailability extends OCPPMessageRequest implements Cloneable {
  @NotNull
  @Min(0)
  @SerializedName("connectorId")
  private int connectorID;

  @NotNull
  @SerializedName("type")
  private AvailabilityType type;

  @Override
  public ChangeAvailability clone() {
    return (ChangeAvailability) super.clone();
  }
}
