package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AvailabilityStatus {
  @SerializedName("Accepted")
  ACCEPTED("Accepted"),

  @SerializedName("Rejected")
  REJECTED("Rejected"),

  @SerializedName("Scheduled")
  SCHEDULED("Scheduled");

  private final String value;

  public static AvailabilityStatus fromString(String value) {
    for (AvailabilityStatus availabilityStatus : AvailabilityStatus.values()) {
      if (availabilityStatus.value.equalsIgnoreCase(value)) {
        return availabilityStatus;
      }
    }
    throw new IllegalArgumentException("Unexpected AvailabilityStatus value: " + value);
  }
}
