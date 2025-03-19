package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Enum representing the type of charging profile. */
@Getter
@RequiredArgsConstructor
public enum ChargingProfileKind {
  @SerializedName("Absolute")
  ABSOLUTE("Absolute"),
  @SerializedName("Recurring")
  RECURRING("Recurring"),
  @SerializedName("Relative")
  RELATIVE("Relative");

  private final String value;

  /**
   * Converts a string to its corresponding enum value.
   *
   * @param value The string representation of the enum.
   * @return The corresponding ChargingProfileKind enum.
   * @throws IllegalArgumentException if the value is invalid.
   */
  public static ChargingProfileKind fromValue(String value) {
    for (ChargingProfileKind kind : values()) {
      if (kind.value.equals(value)) {
        return kind;
      }
    }
    throw new IllegalArgumentException("Invalid ChargingProfileKind: " + value);
  }
}
