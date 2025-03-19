package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Enum representing the purpose of a charging profile. */
@Getter
@RequiredArgsConstructor
public enum ChargingProfilePurpose {
  @SerializedName("ChargePointMaxProfile")
  CHARGE_POINT_MAX_PROFILE("ChargePointMaxProfile"),

  @SerializedName("TxDefaultProfile")
  TX_DEFAULT_PROFILE("TxDefaultProfile"),

  @SerializedName("TxProfile")
  TX_PROFILE("TxProfile");

  private final String value;

  /**
   * Converts a string to its corresponding enum value.
   *
   * @param value The string representation of the enum.
   * @return The corresponding ChargingProfilePurpose enum.
   * @throws IllegalArgumentException if the value is invalid.
   */
  public static ChargingProfilePurpose fromValue(String value) {
    for (ChargingProfilePurpose purpose : values()) {
      if (purpose.value.equals(value)) {
        return purpose;
      }
    }
    throw new IllegalArgumentException("Invalid ChargingProfilePurpose: " + value);
  }
}
