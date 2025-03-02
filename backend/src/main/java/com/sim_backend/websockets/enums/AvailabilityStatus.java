package com.sim_backend.websockets.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AvailabilityStatus {
  ACCEPTED("Accepted"),
  REJECTED("Rejected"),
  Scheduled("Scheduled");

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
