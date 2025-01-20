package com.sim_backend.websockets.enums;

/**
 * Represents the status of the Charge Point.
 */
public enum ChargePointStatus {
    Available, // When a Connector becomes available for a new user.
    Preparing, // When a Connector is preparing for a new user.
    Charging, // When the contactor of a Connector closes, allowing the vehicle to charge.
    SuspendedEVSE, // When the EV is connected but not offering energy.
    SuspendedEV, // When the EV is connected and offering energy but not taking any.
    Finishing, // When a Transaction has stopped but not yet available for a new user.
    Reserved, // When a Connector becomes reserved.
    Unavailable, // When a Connector becomes unavailable.
    Faulted; // When a Charge Point or connector has reported an error.
}