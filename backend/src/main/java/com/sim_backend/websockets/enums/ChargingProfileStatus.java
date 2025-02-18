package com.sim_backend.websockets.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the possible status values for a charging profile. The status indicates the
 * result or condition of a charging profile request.
 */
@Getter
@RequiredArgsConstructor
public enum ChargingProfileStatus {

  /** The charging profile request was accepted. */
  ACCEPTED("Accepted"),

  /** The charging profile request was rejected. */
  REJECTED("Rejected"),

  /** The charging profile request is not supported. */
  NOT_SUPPORTED("NotSupported");

  private final String value;

  /**
   * Converts a string value to the corresponding enum constant.
   *
   * @param value the string value to convert
   * @return the corresponding ChargingProfileStatus enum constant
   * @throws IllegalArgumentException if the string does not match any valid enum constant
   */
  public static ChargingProfileStatus fromString(String value) {
    for (ChargingProfileStatus status : ChargingProfileStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unexpected value: " + value);
  }
}
