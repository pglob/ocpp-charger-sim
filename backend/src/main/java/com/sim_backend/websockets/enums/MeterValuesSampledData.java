package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/** The sampled data meter values for a charger */
@Getter
public enum MeterValuesSampledData {
  @SerializedName("Current.Offered")
  CURRENT_OFFERED("Current.Offered"),

  @SerializedName("Current.Import")
  CURRENT_IMPORT("Current.Import"),

  @SerializedName("Energy.Active.Import.Register")
  ENERGY_ACTIVE_IMPORT_REGISTER("Energy.Active.Import.Register"),

  @SerializedName("Energy.Active.Import.Interval")
  ENERGY_ACTIVE_IMPORT_INTERVAL("Energy.Active.Import.Interval"),

  @SerializedName("Power.Active.Import")
  POWER_ACTIVE_IMPORT("Power.Active.Import"),

  @SerializedName("Power.Offered")
  POWER_OFFERED("Power.Offered");

  private final String value;

  MeterValuesSampledData(String value) {
    this.value = value;
  }

  public static MeterValuesSampledData fromString(String value) {
    for (MeterValuesSampledData data : MeterValuesSampledData.values()) {
      if (data.getValue().equals(value)) {
        return data;
      }
    }
    throw new IllegalArgumentException("Unknown MeterValuesSampledData: " + value);
  }
}
