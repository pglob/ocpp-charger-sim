package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RemoteStartStopStatus {
  @SerializedName("Accepted")
  ACCEPTED("Accepted"),

  @SerializedName("Rejected")
  REJECTED("Rejected");

  private final String value;

  public static RemoteStartStopStatus fromString(String value) {
    for (RemoteStartStopStatus status : RemoteStartStopStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unexpected status value: " + value);
  }
}
