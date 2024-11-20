package com.sim_backend.state;

// Powered_off -> Start() function -> Bootup
// Bootup -> Complete() function -> Available
// Available -> Poweroff() function -> Powered_off
// Available -> useCharger() function -> in_use
// Cehckavailable -> check if it's available
public enum SimulatorState {
    PowerOff,
    BootingUp,
    Available,
}