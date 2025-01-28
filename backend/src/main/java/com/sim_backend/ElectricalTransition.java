package com.sim_backend;

import com.sim_backend.state.SimulatorState;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class represents the electrical transition of a charging process, including the charging
 * power, voltage, current, and energy consumed. It tracks the charging state and calculates the
 * power usage and energy consumption in kilowatt-hours (kWh).
 */
@Getter
@NoArgsConstructor
public class ElectricalTransition {

  /** The charger's voltage in volts. Defaults to 240 volts */
  private int voltage;

  /** The maximum current offered by the charger in amps. Defaults to 40 amps */
  private int currentOffered;

  /** The actual current drawn from the charger by the EV. Defaults to 40 amps */
  private int currentImport;

  /** A timestamp of when charging first started. */
  private long initialChargeTimestamp;

  /** Constant representing the number of seconds in an hour. Used for energy calculations. */
  private static final long SECONDS_PER_HOUR = 3600;

  /** Constant representing the number of milliseconds in an hour. Used for energy calculations. */
  private static final long MILLISECONDS_PER_HOUR = 3600000;

  /** Constant representing the number of milliseconds in a second. Used for energy calculations. */
  private static final long MILLISECONDS_PER_SECOND = 1000;

  /**
   * Calculates and returns the maximum power the charger is capable of providing in kilowatts (kW).
   *
   * @return the maximum power offered in kilowatts (kW).
   */
  public float getPowerOffered() {
    return (float) (currentOffered * voltage) / 1000;
  }

  /**
   * Calculates and returns the actual power consumed by the charger in kilowatts (kW).
   *
   * @return the actual power consumed in kilowatts (kW).
   */
  public float getPowerActiveImport() {
    return (float) (currentImport * voltage) / 1000;
  }

  /**
   * Retrieves the amount of energy consumed since the specified interval in kilowatt-hours (kWh).
   *
   * @param interval the interval in seconds for which the energy usage is being queried
   * @return the energy consumed since the given interval in kilowatt-hours (kWh).
   */
  public float getEnergyActiveImportInterval(int interval) {
    long timeChargingSeconds =
        (System.currentTimeMillis() - this.initialChargeTimestamp) / MILLISECONDS_PER_SECOND;

    // If the requested interval exceeds the actual charging time, adjust accordingly
    if (timeChargingSeconds < interval) {
      interval = (int) timeChargingSeconds;
    }
    return getPowerActiveImport() * ((float) interval / SECONDS_PER_HOUR);
  }

  /**
   * Returns the lifetime energy used by the charger in kilowatt-hours (kWh).
   *
   * @return the lifetime energy consumed in kilowatt-hours (kWh).
   */
  public float getEnergyActiveImportRegister() {
    long timeCharging = System.currentTimeMillis() - this.initialChargeTimestamp;
    return getPowerActiveImport() * ((float) timeCharging / MILLISECONDS_PER_HOUR);
  }

  /**
   * Updates the electrical values based on the given simulator state. If the state is not Charging,
   * all electrical values are reset to zero. Otherwise, when in the Charging state, the voltage is
   * set to 240V, and the current offered and imported are both set to 40A. The initial charge
   * timestamp is also updated to the current system time.
   *
   * @param state the current state of the charger
   */
  public void transition(SimulatorState state) {

    // Whenever we aren't charging, zero all the electrical values.
    if (state != SimulatorState.Charging) {
      this.voltage = 0;
      this.currentOffered = 0;
      this.currentImport = 0;
      this.initialChargeTimestamp = 0;
    }

    // State is charging, proceed to set electrical values
    else {
      this.voltage = 240;
      this.currentOffered = 40;
      this.currentImport = 40;
      this.initialChargeTimestamp = System.currentTimeMillis();
    }
  }
}
