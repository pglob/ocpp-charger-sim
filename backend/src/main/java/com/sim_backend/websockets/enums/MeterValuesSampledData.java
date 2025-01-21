package com.sim_backend.websockets.enums;

import lombok.Getter;

/** The sampled data meter values for a charger */
@Getter
public enum MeterValuesSampledData {
    CURRENT_OFFERED("Current.Offered"),
    CURRENT_IMPORT("Current.Import"),
    ENERGY_ACTIVE_IMPORT_REGISTER("Energy.Active.Import.Register"),
    ENERGY_ACTIVE_IMPORT_INTERVAL("Energy.Active.Import.Interval"),
    POWER_ACTIVE_IMPORT("Power.Active.Import"),
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