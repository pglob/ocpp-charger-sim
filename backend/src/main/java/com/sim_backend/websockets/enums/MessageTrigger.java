package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageTrigger {
  @SerializedName("BootNotification")
  BootNotification("BootNotification"),

  @SerializedName("DiagnosticsStatusNotification")
  DiagnosticsStatusNotification("DiagnosticsStatusNotification"),

  @SerializedName("FirmwareStatusNotification")
  FirmwareStatusNotification("FirmwareStatusNotification"),

  @SerializedName("Heartbeat")
  Heartbeat("Heartbeat"),

  @SerializedName("MeterValues")
  MeterValues("MeterValues"),

  @SerializedName("StatusNotification")
  StatusNotification("StatusNotification");

  private final String value;

  public static MessageTrigger fromString(String value) {
    for (MessageTrigger trigger : MessageTrigger.values()) {
      if (trigger.value.equalsIgnoreCase(value)) {
        return trigger;
      }
    }
    throw new IllegalArgumentException("Unexpected MessageTrigger value: " + value);
  }
}
