package com.sim_backend.websockets.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing the possible statuses for ClearChargingProfileResponse.
 */
@Getter
@AllArgsConstructor
public enum ClearProfileStatus {
    /**
     * Indicates that the request to clear the charging profile was accepted.
     */
    ACCEPTED("Accepted"),

    /**
     * Indicates that the specified charging profile is unknown.
     */
    UNKNOWN("Unknown");

    private final String value;

    /**
     * Converts a string value to its corresponding enum constant.
     *
     * @param value the string representation of the status.
     * @return the corresponding ClearProfileStatus enum constant.
     * @throws IllegalArgumentException if the value does not match any constant.
     */
    public static ClearProfileStatus fromValue(String value) {
        for (ClearProfileStatus status : ClearProfileStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}