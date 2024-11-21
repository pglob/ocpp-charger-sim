package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BootStatus {
  @SerializedName("Accepted")
  ACCEPTED("Accepted"),

  @SerializedName("Pending")
  PENDING("Pending"),

  @SerializedName("Rejected")
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
