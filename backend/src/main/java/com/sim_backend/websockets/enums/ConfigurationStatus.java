package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConfigurationStatus {
  @SerializedName("Accepted")
  ACCEPTED("Accepted"),

  @SerializedName("NotSupported")
  NOTSUPPORTED("NotSupported"),

  @SerializedName("Rejected")
  REJECTED("Rejected");

  private final String value;

  public static ConfigurationStatus fromString(String value) {
    for (ConfigurationStatus status : ConfigurationStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unexpected status value: " + value);
  }
}
