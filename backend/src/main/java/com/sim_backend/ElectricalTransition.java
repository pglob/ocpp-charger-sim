package com.sim_backend;

import com.sim_backend.state.IllegalStateException;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import lombok.NoArgsConstructor;

/**
 * This class represents the electrical transition of a charging process, including the charging
 * power, voltage, current, and energy consumed. It tracks the charging state and calculates the
 * power usage and energy consumption in kilowatt-hours (kWh).
 */
@NoArgsConstructor
public class ElectricalTransition {

  /** The charger's voltage in volts. */
  private int voltage;

  /** The maximum current offered by the charger in amps. */
  private int currentOffered;

  /** The actual current drawn from the charger by the EV. */
  private int currentImport;

  /** The maximum power the charger is capable of providing in kilowatts (kW). */
  private float powerOffered;

  /**
   * The actual power consumed by the charger in kilowatts (kW). Value is not calculated using load
   * balancing.
   */
  private float powerActiveImport;

  /** A timestamp of when charging first started. */
  private long initialChargeTimestamp;

  /** Constant representing the number of seconds in an hour. Used for energy calculations. */
  private static final float SECONDS_PER_HOUR = 3600.0f;

  /** Constant representing the number of milliseconds in an hour. Used for energy calculations. */
  private static final long MILLISECONDS_PER_HOUR = 3600000;

  /**
   * Retrieves the amount of energy consumed since the specified interval in kilowatt-hours (kWh).
   *
   * @param interval the interval in seconds for which the energy usage is being queried
   * @return the energy consumed since the given interval in kilowatt-hours (kWh).
   */
  public float getEnergyActiveImportInterval(int interval) {
    return this.powerOffered * ((float) interval / SECONDS_PER_HOUR);
  }

  /**
   * Returns the lifetime energy used by the charger in kilowatt-hours (kWh).
   *
   * @return the lifetime energy consumed in kilowatt-hours (kWh).
   */
  public float getEnergyActiveImportRegister() {
    long timeCharging = System.currentTimeMillis() - this.initialChargeTimestamp;
    return this.powerOffered * ((float) timeCharging / MILLISECONDS_PER_HOUR);
  }

  /**
   * Initiates the charging transition, setting the initial charging parameters such as voltage,
   * current, and power. The function also checks if the state machine is in the correct state
   * (Charging) before applying the transition.
   *
   * @param stateMachine The state machine controlling the charging process.
   * @throws IllegalStateException If the current state is not 'Charging', an exception is thrown.
   */
  public void ChargingTransition(SimulatorStateMachine stateMachine) {

    if (stateMachine.getCurrentState() != SimulatorState.Charging) {
      throw new IllegalStateException(
          "Charging transition triggered during incorrect state: "
              + stateMachine.getCurrentState());
    }

    // Set the charger's voltage and current parameters for the transition.
    this.voltage = 240;
    this.currentOffered = 40;
    this.currentImport = 40;

    // Calculate the power offered and the active power import in kilowatts.
    this.powerOffered = (float) (currentOffered * voltage) / 1000;
    this.powerActiveImport = (float) (currentImport * voltage) / 1000;

    // Record the timestamp when the charging starts.
    initialChargeTimestamp = System.currentTimeMillis();
  }
}
