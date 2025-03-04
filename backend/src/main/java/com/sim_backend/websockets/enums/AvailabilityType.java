package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AvailabilityType {
  @SerializedName("Operative")
  OPERATIVE("Operative"),

  @SerializedName("Inoperative")
  INOPERATIVE("Inoperative");

  private final String value;

  public static AvailabilityType fromString(String value) {
    for (AvailabilityType availabilityType : AvailabilityType.values()) {
      if (availabilityType.value.equalsIgnoreCase(value)) {
        return availabilityType;
      }
    }
    throw new IllegalArgumentException("Unexpected AvailabilityType: " + value);
  }
}
