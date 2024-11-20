package com.sim_backend.websockets.enums;

public enum Status {
  ACCEPTED("Accepted"),
  PENDING("Pending"),
  REJECTED("Rejected");

  private final String value;

  Status(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Status fromString(String value) {
    for (Status status : Status.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unexpected status value: " + value);
  }
}
