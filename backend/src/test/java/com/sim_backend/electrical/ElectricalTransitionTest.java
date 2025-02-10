package com.sim_backend.electrical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    et.onStateChanged(SimulatorState.PoweredOff);
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

  /**
   * This test simulates a charging session by transitioning into Charging, waiting briefly to allow
   * some energy accumulation, and then transitioning out of Charging. It verifies that the
   * calculated energy consumption for the interval is accurate and that lifetime energy is updated.
   */
  @Test
  public void EnergyConsumptionTest() throws InterruptedException {
    final long SECONDS_PER_HOUR = 3600;
    ElectricalTransition et = new ElectricalTransition();

    // Ensure preconditions.
    assertEquals(et.getEnergyActiveImportInterval(100), 0);
    assertEquals(et.getEnergyActiveImportRegister(), 0);

    // Start charging.
    et.onStateChanged(SimulatorState.Charging);

    // Wait for 2 seconds to simulate a brief charging period.
    Thread.sleep(2000);

    // Verify the interval energy matches the expected value.
    float intervalEnergy = et.getEnergyActiveImportInterval(100);
    float expectedEnergy = et.getPowerActiveImport() * (2.0f / SECONDS_PER_HOUR);
    float tolerance = 0.001f;
    assertTrue(
        Math.abs(intervalEnergy - expectedEnergy) < tolerance,
        "Interval energy should be approximately: " + expectedEnergy);

    // End charging; this should accumulate the energy consumption into the lifetime energy
    // register.
    et.onStateChanged(SimulatorState.PoweredOff);
    float lifetimeEnergy = et.getEnergyActiveImportRegister();
    assertTrue(
        Math.abs(intervalEnergy - lifetimeEnergy) < tolerance,
        "Lifetime energy should have accumulated the consumed energy.");

    // After switching to a non-charging state, all values should be reset.
    assert (et.getVoltage() == 0);
    assert (et.getCurrentImport() == 0);
    assert (et.getCurrentOffered() == 0);
    assert (et.getInitialChargeTimestamp() == 0);
  }

  /** Test that multiple charging sessions correctly accumulate lifetime energy. */
  @Test
  public void testMultipleChargingSessions() throws InterruptedException {
    ElectricalTransition et = new ElectricalTransition();

    et.onStateChanged(SimulatorState.Charging);
    Thread.sleep(1000); // Simulate 1 second of charging.
    et.onStateChanged(SimulatorState.PoweredOff);
    float energyAfterSession1 = et.getEnergyActiveImportRegister();

    et.onStateChanged(SimulatorState.Charging);
    Thread.sleep(1000); // Simulate 1 second of charging.
    et.onStateChanged(SimulatorState.PoweredOff);
    float energyAfterSession2 = et.getEnergyActiveImportRegister();

    // Lifetime energy should increase over sessions.
    assertTrue(
        energyAfterSession2 > energyAfterSession1,
        "Lifetime energy should accumulate across sessions.");
  }

  /**
   * Test that providing a negative interval to getEnergyActiveImportInterval throws an exception.
   */
  @Test
  public void testGetEnergyActiveImportIntervalNegativeInterval() throws InterruptedException {
    ElectricalTransition et = new ElectricalTransition();
    assertThrows(IllegalArgumentException.class, () -> et.getEnergyActiveImportInterval(-1));
    assertThrows(IllegalArgumentException.class, () -> et.getEnergyActiveImportInterval(-100));
  }

  /** Test that a zero interval returns zero energy consumption. */
  @Test
  public void testZeroInterval() {
    ElectricalTransition et = new ElectricalTransition();
    et.onStateChanged(SimulatorState.Charging);
    float energyInterval = et.getEnergyActiveImportInterval(0);
    assertEquals(0, energyInterval, "Energy consumption for a zero interval should be zero.");
  }
}
