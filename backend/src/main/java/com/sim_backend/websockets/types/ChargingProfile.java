package com.sim_backend.websockets.types;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.enums.ChargingProfileKind;
import com.sim_backend.websockets.enums.ChargingProfilePurpose;
import com.sim_backend.websockets.enums.RecurrencyKind;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Represents a charging profile that defines how a vehicle should be charged. */
@Getter
@Setter
@AllArgsConstructor
public class ChargingProfile {

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
  @Getter
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
