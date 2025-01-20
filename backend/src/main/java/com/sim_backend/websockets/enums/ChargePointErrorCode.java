package com.sim_backend.websockets.enums;

/**
 * Represents the error codes that can be reported by the Charge Point.
 */
public enum ChargePointErrorCode {
    ConnectorLockFailure, // Failure to lock or unlock connector.
    EVCommunicationError, // Communication failure with the vehicle.
    GroundFailure, // Ground fault circuit interrupter has been activated.
    HighTemperature, // Temperature inside Charge Point is too high.
    InternalError, // Error in internal hard- or software component.
    LocalListConflict, // Conflict with LocalAuthorizationList.
    NoError, // No error to report.
    OtherError, // Other type of error.
    OverCurrentFailure, // Over current protection device has tripped.
    OverVoltage, // Voltage has risen above an acceptable level.
    PowerMeterFailure, // Failure to read electrical/energy/power meter.
    PowerSwitchFailure, // Failure to control power switch.
    ReaderFailure, // Failure with idTag reader.
    ResetFailure, // Unable to perform a reset.
    UnderVoltage, // Voltage has dropped below an acceptable level.
    WeakSignal // Wireless communication device reports a weak signal.
}