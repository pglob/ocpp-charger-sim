package com.sim_backend.websockets.types;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.enums.ChargingRateUnit;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Represents a schedule defining how charging should occur over time. */
@Getter
@AllArgsConstructor
public class ChargingSchedule {

    /** Duration of the schedule in seconds (optional). */
    @SerializedName("duration")
    private Integer duration;

    /** Start time of the charging schedule. */
    @SerializedName("startSchedule")
    private ZonedDateTime startSchedule;

    /** The unit of charging rate (e.g., Amperes "A" or Watts "W"). */
    @NotNull(message = "ChargingSchedule chargingRateUnit is required")
    @SerializedName("chargingRateUnit")
    private ChargingRateUnit chargingRateUnit;

    /** List of periods defining different charging rates over time. */
    @NotNull(message = "ChargingSchedule chargingSchedulePeriod is required")
    @SerializedName("chargingSchedulePeriod")
    private List<ChargingSchedulePeriod> chargingSchedulePeriod;

    /** Minimum allowed charging rate. */
    @SerializedName("minChargingRate")
    private Double minChargingRate;
}