package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/** Enum representing a unit of measure. */
@Getter
public enum UnitOfMeasure {

  /** Watt-hours (energy). Default. */
  @SerializedName("Wh")
  WH("Wh"),

  /** kiloWatt-hours (energy). */
  @SerializedName("kWh")
  KWH("kWh"),

  /** Var-hours (reactive energy). */
  @SerializedName("varh")
  VARH("varh"),

  /** kilovar-hours (reactive energy). */
  @SerializedName("kvarh")
  KVARH("kvarh"),

  /** Watts (power). */
  @SerializedName("W")
  W("W"),

  /** kilowatts (power). */
  @SerializedName("kW")
  KW("kW"),

  /** VoltAmpere (apparent power). */
  @SerializedName("VA")
  VA("VA"),

  /** kiloVolt Ampere (apparent power). */
  @SerializedName("kVA")
  KVA("kVA"),

  /** Vars (reactive power). */
  @SerializedName("var")
  VAR("var"),

  /** kilovars (reactive power). */
  @SerializedName("kvar")
  KVAR("kvar"),

  /** Amperes (current). */
  @SerializedName("A")
  A("A"),

  /** Voltage (r.m.s. AC). */
  @SerializedName("V")
  V("V"),

  /** Degrees (temperature). */
  @SerializedName("Celsius")
  CELSIUS("Celsius"),

  /** Degrees (temperature). */
  @SerializedName("Fahrenheit")
  FAHRENHEIT("Fahrenheit"),

  /** Degrees Kelvin (temperature). */
  @SerializedName("K")
  K("K"),

  /** Percentage. */
  @SerializedName("Percent")
  PERCENT("Percent");

  private final String value;

  UnitOfMeasure(String value) {
    this.value = value;
  }

  public static UnitOfMeasure fromString(String value) {
    for (UnitOfMeasure unit : UnitOfMeasure.values()) {
      if (unit.getValue().equals(value)) {
        return unit;
      }
    }
    throw new IllegalArgumentException("Unknown UnitOfMeasure: " + value);
  }
}
