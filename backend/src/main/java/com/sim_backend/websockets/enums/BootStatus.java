package com.sim_backend.websockets.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BootStatus {
  ACCEPTED("Accepted"),
  PENDING("Pending"),
  REJECTED("Rejected");

  private final String value;

  public static BootStatus fromString(String value) {
    for (BootStatus status : BootStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unexpected status value: " + value);
  }
}
