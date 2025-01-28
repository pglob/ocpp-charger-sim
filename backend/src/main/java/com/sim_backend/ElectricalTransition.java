package com.sim_backend;

import com.sim_backend.state.IllegalStateException;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ElectricalTransition {

  /** Charger's voltage.*/
  private int voltage;

  /** Charger's maximum current in amps.*/
  private  int currentOffered;

  /** The actual current drawn from the charger by the EV. */
  private int currentImport;

  /** The maximum power the charger is capable of providing in kilowatts (kW). */
  private float powerOffered;

  /**
   * The actual power consumed by the charger in kilowatts (kW). Value is not calculated using load
   * balancing.
   */
  private float powerActiveImport;

  /**
   * A timestamp of when charging first occurs
   */
  private long initialChargeTimestamp;

  /**
   * Amount of seconds per hour. Used for calculations.
   */
  private static final float SECONDS_PER_HOUR = 3600.0f;

  /**
   * Amount of milliseconds per hour. Used for calculations.
   */
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
  public float getEnergyActiveImportRegister(){
    long timeCharging = System.currentTimeMillis() - this.initialChargeTimestamp;
    return this.powerOffered * ((float) timeCharging /MILLISECONDS_PER_HOUR);
  }

  public void ChargingTransition(SimulatorStateMachine stateMachine) {

    if(stateMachine.getCurrentState() != SimulatorState.Charging){
      throw new IllegalStateException("Charging transition triggered during incorrect state: " + stateMachine.getCurrentState());
    }

    this.voltage = 240;
    this.currentOffered = 40;
    this.currentImport = 40;
    this.powerOffered = (float) (currentOffered * voltage) /1000;
    this.powerActiveImport = (float) (currentImport * voltage) /1000;
    initialChargeTimestamp = System.currentTimeMillis();
  }
}
