package com.sim_backend.websockets.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthStatus {
  ACCEPTED("Accepted"),
  BLOCKED("Blocked"),
  EXPIRED("Expired"),
  INVALID("Invalid"),
  CONCURRENT("ConcurrentTx");

  private final String value;

  public static AuthStatus fromString(String value) {
    for (AuthStatus status : AuthStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unexpected status value: " + value);
  }
}
