package com.sim_backend.websockets.types;

import com.google.gson.annotations.SerializedName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Defines a specific period within a charging schedule. */
@Getter
@AllArgsConstructor
public class ChargingSchedulePeriod {

    /** Start time of the period in seconds from the beginning of the schedule. */
    @NotNull(message = "ChargingSchedulePeriod startPeriod is required")
    @SerializedName("startPeriod")
    private int startPeriod;

    /** Maximum allowed charging limit during this period. */
    @NotNull(message = "ChargingSchedulePeriod limit is required")
    @SerializedName("limit")
    private double limit;

    /** Number of phases used for charging (optional). */
    @SerializedName("numberPhases")
    private Integer numberPhases;
}