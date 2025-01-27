package com.sim_backend;

import lombok.Getter;

@Getter
public class ElectricalTransition {

  /** Charger's voltage. Default value of a level 2 charger, 240V. */
  private int voltage = 240;

  /** Charger's maximum current in amps. Defaults to 40A. */
  private final int currentOffered = 40;

  /** The actual current drawn from the charger by the EV. */
  private final int currentImport = 40;

  /** The maximum power the charger is capable of providing in kilowatts (kW).*/
  private float powerOffered;

  /** The actual power consumed by the charger in kilowatts (kW). Value is not calculated using load balancing. */
  private float powerActiveImport;

  /** The lifetime energy used by the charger in kilowatt-hours (kWh). */
  private int lifetimeEnergyUse = 0;

  public ElectricalTransition() {
    this.powerOffered = (float) (currentOffered * voltage) / 1000;
    this.powerActiveImport = (float) (currentImport * voltage) / 1000;
  }

    /**
     * Retrieves the amount of energy consumed since the specified interval in kilowatt-hours (kWh).
     *
     * @param interval the interval in seconds for which the energy usage is being queried
     * @return the energy consumed since the given interval in kilowatt-hours (kWh).
     */
    public float getEnergyActiveImportInterval(int interval) {
        return this.powerOffered * (interval/3600);
    }

    public int ChargingTransition() {
    return 0;
  }
}
