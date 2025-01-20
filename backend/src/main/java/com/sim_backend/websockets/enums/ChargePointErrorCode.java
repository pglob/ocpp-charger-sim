package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the error codes that can be reported by the Charge Point.
 */
@Getter
@AllArgsConstructor
public enum ChargePointErrorCode {
    @SerializedName("ConnectorLockFailure")
    ConnectorLockFailure("ConnectorLockFailure"), // Failure to lock or unlock connector.

    @SerializedName("EVCommunicationError")
    EVCommunicationError("EVCommunicationError"), // Communication failure with the vehicle.

    @SerializedName("GroundFailure")
    GroundFailure("GroundFailure"), // Ground fault circuit interrupter has been activated.

    @SerializedName("HighTemperature")
    HighTemperature("HighTemperature"), // Temperature inside Charge Point is too high.

    @SerializedName("InternalError")
    InternalError("InternalError"), // Error in internal hard- or software component.

    @SerializedName("LocalListConflict")
    LocalListConflict("LocalListConflict"), // Conflict with LocalAuthorizationList.

    @SerializedName("NoError")
    NoError("NoError"), // No error to report.

    @SerializedName("OtherError")
    OtherError("OtherError"), // Other type of error.

    @SerializedName("OverCurrentFailure")
    OverCurrentFailure("OverCurrentFailure"), // Over current protection device has tripped.

    @SerializedName("OverVoltage")
    OverVoltage("OverVoltage"), // Voltage has risen above an acceptable level.

    @SerializedName("PowerMeterFailure")
    PowerMeterFailure("PowerMeterFailure"), // Failure to read electrical/energy/power meter.

    @SerializedName("PowerSwitchFailure")
    PowerSwitchFailure("PowerSwitchFailure"), // Failure to control power switch.

    @SerializedName("ReaderFailure")
    ReaderFailure("ReaderFailure"), // Failure with idTag reader.

    @SerializedName("ResetFailure")
    ResetFailure("ResetFailure"), // Unable to perform a reset.

    @SerializedName("UnderVoltage")
    UnderVoltage("UnderVoltage"), // Voltage has dropped below an acceptable level.

    @SerializedName("WeakSignal")
    WeakSignal("WeakSignal"); // Wireless communication device reports a weak signal.

    private final String value;

    public static ChargePointErrorCode fromString(String value) {
        for (ChargePointErrorCode errorCode : ChargePointErrorCode.values()) {
            if (errorCode.value.equalsIgnoreCase(value)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unexpected ChargePointErrorCode value: " + value);
    }
}
