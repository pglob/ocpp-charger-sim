package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ChargingProfileKind;
import com.sim_backend.websockets.enums.ChargingProfilePurpose;
import com.sim_backend.websockets.enums.ChargingRateUnit;
import com.sim_backend.websockets.enums.RecurrencyKind;
import com.sim_backend.websockets.types.OCPPMessage;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Represents a request to set a charging profile for an EV charging station. */
@Getter
@AllArgsConstructor
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "SetChargingProfile")
public class SetChargingProfile {

  /** The ID of the connector to which the profile applies. */
  @NotNull(message = "SetChargingProfile connectorId is required")
  @SerializedName("connectorId")
  private int connectorId;

  /** The charging profile details. */
  @NotNull(message = "SetChargingProfile csChargingProfiles is required")
  @SerializedName("csChargingProfiles")
  private ChargingProfile csChargingProfiles;

  /** Represents a charging profile that defines how a vehicle should be charged. */
  @Getter
  @AllArgsConstructor
  public static class ChargingProfile {

    /** Unique identifier for the charging profile. */
    @NotNull(message = "ChargingProfile chargingProfileId is required")
    @SerializedName("chargingProfileId")
    private int chargingProfileId;

    /** The transaction ID associated with the charging profile (optional). */
    @SerializedName("transactionId")
    private Integer transactionId;

    /** Priority level of the profile in the stack. */
    @NotNull(message = "ChargingProfile stackLevel is required")
    @SerializedName("stackLevel")
    private int stackLevel;

    /** Purpose of the charging profile (e.g., ChargePointMaxProfile, TxProfile). */
    @NotNull(message = "ChargingProfile chargingProfilePurpose is required")
    @SerializedName("chargingProfilePurpose")
    private ChargingProfilePurpose chargingProfilePurpose;

    /** Type of charging profile (e.g., Absolute, Recurring, Relative). */
    @NotNull(message = "ChargingProfile chargingProfileKind is required")
    @SerializedName("chargingProfileKind")
    private ChargingProfileKind chargingProfileKind;

    /** Recurrence pattern of the charging schedule (e.g., Daily, Weekly). */
    @SerializedName("recurrencyKind")
    private RecurrencyKind recurrencyKind;

    /** The start date and time when the profile becomes valid. */
    @SerializedName("validFrom")
    private ZonedDateTime validFrom;

    /** The end date and time when the profile expires. */
    @SerializedName("validTo")
    private ZonedDateTime validTo;

    /** The detailed charging schedule associated with this profile. */
    @NotNull(message = "ChargingProfile chargingSchedule is required")
    @SerializedName("chargingSchedule")
    private ChargingSchedule chargingSchedule;
  }

  /** Represents a schedule defining how charging should occur over time. */
  @Getter
  @AllArgsConstructor
  public static class ChargingSchedule {

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
    @SerializedName("status")
    private Double minChargingRate;
  }

  /** Defines a specific period within a charging schedule. */
  @Getter
  @AllArgsConstructor
  public static class ChargingSchedulePeriod {

    /** Start time of the period in seconds from the beginning of the schedule. */
    @SerializedName("startPeriod")
    private int startPeriod;

    /** Maximum allowed charging limit during this period. */
    @SerializedName("limit")
    private double limit;

    /** Number of phases used for charging (optional). */
    @SerializedName("numberPhases")
    private Integer numberPhases;
  }
}
