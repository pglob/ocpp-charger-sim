package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthorizationStatus {
  @SerializedName("Accepted")
  ACCEPTED("Accepted"),

  @SerializedName("Blocked")
  BLOCKED("Blocked"),

  @SerializedName("Expired")
  EXPIRED("Expired"),

  @SerializedName("Invalid")
  INVALID("Invalid"),

  @SerializedName("ConcurrentTx")
  CONCURRENT("ConcurrentTx");

  private final String value;

  public static AuthorizationStatus fromString(String value) {
    for (AuthorizationStatus status : AuthorizationStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unexpected status value: " + value);
  }
}
