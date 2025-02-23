package com.sim_backend.electrical;

import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.state.StateObserver;
import com.sim_backend.websockets.enums.ChargingRateUnit;
import com.sim_backend.websockets.messages.MessageValidator;
import com.sim_backend.websockets.messages.SetChargingProfile;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class represents the electrical transition of a charging process, including the charging
 * power, voltage, current, and energy consumed. It tracks the charging state and calculates the
 * power usage and energy consumption in kilowatt-hours (kWh).
 */
@Getter
@NoArgsConstructor
public class ElectricalTransition implements StateObserver {

  /** The charger's voltage in volts. */
  private int voltage = 0;

  /** The voltage the charger is connected to. 240V assumes a split phase residential circuit. */
  private final int nominalVoltage = 240;

  /** The maximum current offered by the charger in amps. */
  private int currentOffered = 0;

  /** The actual current drawn from the charger by the EV. */
  private int currentImport = 0;

  /** The maximum current the charger is rated for. */
  private final int maxCurrent = 40;

  /** A timestamp of when charging first started for the current session. */
  private long initialChargeTimestamp = 0;

  /** Accumulated lifetime energy consumption in kWh across all sessions. */
  private float lifetimeEnergy = 0.0f;

  private SetChargingProfile chargingProfile = null;

  /** Constant representing the number of seconds in an hour. Used for energy calculations. */
  private static final long SECONDS_PER_HOUR = 3600;

  /** Constant representing the number of milliseconds in an hour. Used for energy calculations. */
  private static final long MILLISECONDS_PER_HOUR = 3600000;

  /** Constant representing the number of milliseconds in a second. Used for energy calculations. */
  private static final long MILLISECONDS_PER_SECOND = 1000;

  public ElectricalTransition(ChargerStateMachine stateMachine) {
    stateMachine.addObserver(this);
  }

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
    if (interval < 0) throw new IllegalArgumentException("interval must be nonnegative");

    long timeChargingSeconds =
        (System.currentTimeMillis() - this.initialChargeTimestamp) / MILLISECONDS_PER_SECOND;

    // If the requested interval exceeds the actual charging time, adjust accordingly
    if (timeChargingSeconds < interval) {
      interval = (int) timeChargingSeconds;
    }
    return getPowerActiveImport() * ((float) interval / SECONDS_PER_HOUR);
  }

  /**
   * Returns the lifetime energy used by the charger in kilowatt-hours (kWh). This includes energy
   * consumed in previous charging sessions as well as the current session if it is still active.
   *
   * @return the lifetime energy consumed in kilowatt-hours (kWh).
   */
  public float getEnergyActiveImportRegister() {
    float currentSessionEnergy = 0.0f;
    if (initialChargeTimestamp > 0) {
      long timeCharging = System.currentTimeMillis() - this.initialChargeTimestamp;
      currentSessionEnergy =
          getPowerActiveImport() * ((float) timeCharging / MILLISECONDS_PER_HOUR);
    }
    return lifetimeEnergy + currentSessionEnergy;
  }

  /**
   * Updates the electrical values based on the given charger state. When transitioning from
   * Charging to a non-Charging state, the energy consumed in the current session is accumulated
   * into lifetimeEnergy.
   *
   * @param chargerState is the current charger state
   */
  @Override
  public void onStateChanged(ChargerState chargerState) {
    // If we're leaving the Charging state, accumulate the current session energy.
    if (chargerState != ChargerState.Charging) {
      if (initialChargeTimestamp != 0) { // Only if there was an active charging session.
        long timeCharging = System.currentTimeMillis() - this.initialChargeTimestamp;
        float sessionEnergy =
            getPowerActiveImport() * ((float) timeCharging / MILLISECONDS_PER_HOUR);
        lifetimeEnergy += sessionEnergy;
      }
      // Reset the current session values.
      this.voltage = 0;
      this.currentOffered = 0;
      this.currentImport = 0;
      this.initialChargeTimestamp = 0;
    }
    // State is Charging, proceed to set electrical values for a new session.
    else {
      this.voltage = this.nominalVoltage;
      this.currentOffered = this.maxCurrent;
      this.currentImport = this.currentOffered;
      // Set a new start time for this charging session.
      this.initialChargeTimestamp = System.currentTimeMillis();
    }
  }

  public void setChargingProfile(SetChargingProfile chargingProfile) {
    if (!MessageValidator.isValid(chargingProfile)) {
      throw new IllegalArgumentException(MessageValidator.log_message(chargingProfile));
    }

    // Ensure charger is active
    if(this.voltage == 0){
      throw new IllegalArgumentException("ChargingProfile received but charger is not active");
    }

    double minChargingRate = chargingProfile.getCsChargingProfiles().getChargingSchedule().getMinChargingRate();

    // Convert given watts value to amps
    if(chargingProfile.getCsChargingProfiles().getChargingSchedule().getChargingRateUnit() == ChargingRateUnit.WATTS){
      minChargingRate = minChargingRate/this.voltage;
    }

    // Ensure min charge rate does not exceed charger's max
    if(minChargingRate > this.currentOffered){
      throw new IllegalArgumentException("ChargingProfile minimum charging rate exceeds charger's maximum");
    }

    this.chargingProfile = chargingProfile;

  }
}
