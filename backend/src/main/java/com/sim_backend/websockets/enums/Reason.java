package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Enum representing the reason for stopping a transaction in a StopTransaction. */
@Getter
@RequiredArgsConstructor
public enum Reason {

  /* The transaction was stopped because of the authorization status in a StartTransaction.conf */
  @SerializedName("DeAuthorized")
  DE_AUTHORIZED("DeAuthorized"),

  /* Emergency stop button was used */
  @SerializedName("EmergencyStop")
  EMERGENCY_STOP("EmergencyStop"),

  /* disconnecting of cable, vehicle moved away from inductive charge unit */
  @SerializedName("EVDisconnected")
  EV_DISCONNECTED("EVDisconnected"),

  /* A hard reset command was received */
  @SerializedName("HardReset")
  HARD_RESET("HardReset"),

  /* Stopped locally on request of the user at the Charge Point */
  @SerializedName("Local")
  LOCAL("Local"),

  /* Any other reason */
  @SerializedName("Other")
  OTHER("Other"),

  /* Complete loss of power */
  @SerializedName("PowerLoss")
  POWER_LOSS("PowerLoss"),

  /* A locally initiated reset/reboot occurred */
  @SerializedName("Reboot")
  REBOOT("Reboot"),

  /*Stopped remotely on request of the user */
  @SerializedName("Remote")
  REMOTE("Remote"),

  /* A soft reset command was received */
  @SerializedName("SoftReset")
  SOFT_RESET("SoftReset"),

  /* Central System sent an Unlock Connector command */
  @SerializedName("UnlockCommand")
  UNLOCK_COMMAND("UnlockCommand");

  private final String value;

  /**
   * Converts a string to its corresponding enum value.
   *
   * @param value The string representation of the enum.
   * @return The corresponding Reason enum.
   * @throws IllegalArgumentException if the value is invalid.
   */
  public static Reason fromValue(String value) {
    for (Reason reason : values()) {
      if (reason.value.equals(value)) {
        return reason;
      }
    }
    throw new IllegalArgumentException("Invalid Reason: " + value);
  }
}
