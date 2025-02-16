package com.sim_backend.websockets.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the different units for charging rate.
 */
@Getter
@RequiredArgsConstructor
public enum ChargingRateUnit {

    /**
     * Unit for amperes (A).
     */
    AMPS("A"),

    /**
     * Unit for watts (W).
     */
    WATTS("W");

    // String value associated with each enum constant
    private final String value;

    /**
     * Converts a string value to the corresponding enum constant.
     *
     * @param value the string value to convert
     * @return the corresponding ChargingRateUnit enum constant
     * @throws IllegalArgumentException if the string does not match any valid enum constant
     */
    public static ChargingRateUnit fromString(String value) {
        for (ChargingRateUnit unit : ChargingRateUnit.values()) {
            if (unit.value.equalsIgnoreCase(value)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + value);
    }
}