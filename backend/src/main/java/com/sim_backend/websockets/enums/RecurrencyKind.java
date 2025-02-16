package com.sim_backend.websockets.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Enum representing the different types of recurrency. */
@Getter
@RequiredArgsConstructor
public enum RecurrencyKind {

  /** Daily recurrency type. */
  DAILY("Daily"),

  /** Weekly recurrency type. */
  WEEKLY("Weekly");

  // String value associated with each enum constant
  private final String value;

  /**
   * Converts a string value to the corresponding enum constant.
   *
   * @param value the string value to convert
   * @return the corresponding RecurrencyKind enum constant
   * @throws IllegalArgumentException if the string does not match any valid enum constant
   */
  public static RecurrencyKind fromString(String value) {
    for (RecurrencyKind recurrency : RecurrencyKind.values()) {
      if (recurrency.value.equalsIgnoreCase(value)) {
        return recurrency;
      }
    }
    throw new IllegalArgumentException("Unexpected value: " + value);
  }
}
