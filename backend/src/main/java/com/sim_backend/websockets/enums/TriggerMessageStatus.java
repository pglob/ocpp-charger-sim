package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TriggerMessageStatus {

  @SerializedName("Accepted")
  Accepted("Accepted"),

  @SerializedName("Rejected")
  Rejected("Rejected"),

  @SerializedName("NotImplemented")
  NotImplemented("NotImplemented");

  private final String value;

  public static TriggerMessageStatus fromString(String value) {
    for (TriggerMessageStatus status : TriggerMessageStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unexpected TriggerMessageStatus value: " + value);
  }
}