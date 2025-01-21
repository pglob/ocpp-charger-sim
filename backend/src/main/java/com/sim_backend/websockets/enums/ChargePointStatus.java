package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChargePointStatus {
  @SerializedName("Available")
  Available("Available"), // When a Connector becomes available for a new user.

  @SerializedName("Preparing")
  Preparing("Preparing"), // When a Connector is preparing for a new user.

  @SerializedName("Charging")
  Charging("Charging"), // When the contactor of a Connector closes, allowing the vehicle to charge.

  @SerializedName("SuspendedEVSE")
  SuspendedEVSE("SuspendedEVSE"), // When the EV is connected but not offering energy.

  @SerializedName("SuspendedEV")
  SuspendedEV("SuspendedEV"), // When the EV is connected and offering energy but not taking any.

  @SerializedName("Finishing")
  Finishing("Finishing"), // When a Transaction has stopped but not yet available for a new user.

  @SerializedName("Reserved")
  Reserved("Reserved"), // When a Connector becomes reserved.

  @SerializedName("Unavailable")
  Unavailable("Unavailable"), // When a Connector becomes unavailable.

  @SerializedName("Faulted")
  Faulted("Faulted"); // When a Charge Point or connector has reported an error.

  private final String value;

  public static ChargePointStatus fromString(String value) {
    for (ChargePointStatus status : ChargePointStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unexpected ChargePointStatus value: " + value);
  }
}
