package com.sim_backend;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sim_backend.state.SimulatorState;
import org.junit.jupiter.api.Test;

public class ElectricalTransitionTest {

  @Test
  public void InitializedTest() {
    ElectricalTransition et = new ElectricalTransition();

    assert (et.getPowerActiveImport() == 0);
    assert (et.getPowerOffered() == 0);
    assert (et.getCurrentImport() == 0);
    assert (et.getCurrentOffered() == 0);
    assert (et.getVoltage() == 0);
    assert (et.getEnergyActiveImportRegister() == 0);
    assert (et.getEnergyActiveImportInterval(100)
        == 0); // Should be 0 if other electrical values are 0.
    assert (et.getInitialChargeTimestamp() == 0);
  }

  @Test
  public void ChargingStateTest() {

    ElectricalTransition et = new ElectricalTransition();
    long beforeCreation = System.currentTimeMillis();
    et.onStateChanged(SimulatorState.Charging);
    long afterCreation = System.currentTimeMillis();

    assert (et.getVoltage() == 240);
    assert (et.getCurrentOffered() == 40);
    assert (et.getCurrentImport() == 40);

    // Get the timestamp from the object
    long objectTimestamp = et.getInitialChargeTimestamp();

    // Allow a small tolerance for the timestamp (3 seconds)
    long toleranceInMillis = 3000;

    assertTrue(
        objectTimestamp >= beforeCreation - toleranceInMillis
            && objectTimestamp <= afterCreation + toleranceInMillis,
        "Timestamp is not within the acceptable range.");
  }

  @Test
  public void ChargingStateIntoNonCharging() {

    ElectricalTransition et = new ElectricalTransition();
    et.onStateChanged(SimulatorState.Charging);
    et.onStateChanged((SimulatorState.PoweredOff));
    assert (et.getPowerActiveImport() == 0);
    assert (et.getPowerOffered() == 0);
    assert (et.getCurrentImport() == 0);
    assert (et.getCurrentOffered() == 0);
    assert (et.getVoltage() == 0);
    assert (et.getEnergyActiveImportRegister() == 0);
    assert (et.getEnergyActiveImportInterval(100)
        == 0); // Should be 0 if other electrical values are 0.
    assert (et.getInitialChargeTimestamp() == 0);
  }
}
