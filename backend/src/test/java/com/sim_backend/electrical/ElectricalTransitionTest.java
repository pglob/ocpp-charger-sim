package com.sim_backend.electrical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sim_backend.charger.Charger;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.OCPPWebSocketClientTest.TestOCPPWebSocketClient;
import com.sim_backend.websockets.observers.StatusNotificationObserver;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ElectricalTransitionTest {

  private TestOCPPWebSocketClient mockClient;
  private MessageScheduler mockScheduler;
  private OCPPTime mockTime;

  @BeforeEach
  public void setUp() {
    mockClient = mock(TestOCPPWebSocketClient.class);
    mockScheduler = mock(MessageScheduler.class);
    mockTime = mock(OCPPTime.class);

    when(mockClient.getScheduler()).thenReturn(mockScheduler);
    when(mockScheduler.getTime()).thenReturn(mockTime);
    when(mockTime.getSynchronizedTime()).thenReturn(ZonedDateTime.now());
  }

  @Test
  public void InitializedTest() {

    ElectricalTransition et = new ElectricalTransition(new ChargerStateMachine(), mockClient);
    TransactionHandler handler = null;
    OCPPWebSocketClient client = null;
    try {
      handler = new TransactionHandler(new Charger(0));
      client = new OCPPWebSocketClient(new URI(""), new StatusNotificationObserver());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    et.setChargingProfileHandler(new ChargingProfileHandler(handler, client));

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

    ElectricalTransition et = new ElectricalTransition(new ChargerStateMachine(), mockClient);
    TransactionHandler handler = null;
    OCPPWebSocketClient client = null;
    try {
      handler = new TransactionHandler(new Charger(0));
      client = new OCPPWebSocketClient(new URI(""), new StatusNotificationObserver());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    et.setChargingProfileHandler(new ChargingProfileHandler(handler, client));

    long beforeCreation = System.currentTimeMillis();
    et.onStateChanged(ChargerState.Charging);
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
    ElectricalTransition et = new ElectricalTransition(new ChargerStateMachine(), mockClient);
    TransactionHandler handler = null;
    OCPPWebSocketClient client = null;
    try {
      handler = new TransactionHandler(new Charger(0));
      client = new OCPPWebSocketClient(new URI(""), new StatusNotificationObserver());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    et.setChargingProfileHandler(new ChargingProfileHandler(handler, client));

    et.onStateChanged(ChargerState.Charging);
    et.onStateChanged(ChargerState.PoweredOff);
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
    ElectricalTransition et = new ElectricalTransition(new ChargerStateMachine(), mockClient);
    TransactionHandler handler = null;
    OCPPWebSocketClient client = null;
    try {
      handler = new TransactionHandler(new Charger(0));
      client = new OCPPWebSocketClient(new URI(""), new StatusNotificationObserver());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    et.setChargingProfileHandler(new ChargingProfileHandler(handler, client));

    // Ensure preconditions.
    assertEquals(et.getEnergyActiveImportInterval(100), 0);
    assertEquals(et.getEnergyActiveImportRegister(), 0);
    // Start charging.
    et.onStateChanged(ChargerState.Charging);
    // Simulate a brief charging period.
    when(mockTime.getSynchronizedTime()).thenReturn(ZonedDateTime.now().plusSeconds(2));
    // Verify the interval energy matches the expected value.
    float intervalEnergy = et.getEnergyActiveImportInterval(100);
    float expectedEnergy = et.getPowerActiveImport() * (2.0f / SECONDS_PER_HOUR);
    float tolerance = 0.001f;
    assertTrue(
        Math.abs(intervalEnergy - expectedEnergy) < tolerance,
        "Interval energy should be approximately: " + expectedEnergy);
    // End charging; this should accumulate the energy consumption into the lifetime energy
    // register.
    et.onStateChanged(ChargerState.PoweredOff);
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
    ElectricalTransition et = new ElectricalTransition(new ChargerStateMachine(), mockClient);
    TransactionHandler handler = null;
    OCPPWebSocketClient client = null;
    try {
      handler = new TransactionHandler(new Charger(0));
      client = new OCPPWebSocketClient(new URI(""), new StatusNotificationObserver());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    et.setChargingProfileHandler(new ChargingProfileHandler(handler, client));

    et.onStateChanged(ChargerState.Charging);
    when(mockTime.getSynchronizedTime())
        .thenReturn(ZonedDateTime.now().plusSeconds(1)); // Simulate 1 second of charging.
    et.onStateChanged(ChargerState.PoweredOff);
    float energyAfterSession1 = et.getEnergyActiveImportRegister();
    et.onStateChanged(ChargerState.Charging);
    when(mockTime.getSynchronizedTime())
        .thenReturn(ZonedDateTime.now().plusSeconds(2)); // Simulate 1 more second of charging.
    et.onStateChanged(ChargerState.PoweredOff);
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
    ElectricalTransition et = new ElectricalTransition(new ChargerStateMachine(), mockClient);
    TransactionHandler handler = null;
    OCPPWebSocketClient client = null;
    try {
      handler = new TransactionHandler(new Charger(0));
      client = new OCPPWebSocketClient(new URI(""), new StatusNotificationObserver());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    et.setChargingProfileHandler(new ChargingProfileHandler(handler, client));

    ElectricalTransition finalEt = et;
    assertThrows(IllegalArgumentException.class, () -> finalEt.getEnergyActiveImportInterval(-1));
    ElectricalTransition finalEt1 = et;
    assertThrows(
        IllegalArgumentException.class, () -> finalEt1.getEnergyActiveImportInterval(-100));
  }

  /** Test that a zero interval returns zero energy consumption. */
  @Test
  public void testZeroInterval() {
    ElectricalTransition et = new ElectricalTransition(new ChargerStateMachine(), mockClient);
    TransactionHandler handler = null;
    OCPPWebSocketClient client = null;
    try {
      handler = new TransactionHandler(new Charger(0));
      client = new OCPPWebSocketClient(new URI(""), new StatusNotificationObserver());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    et.setChargingProfileHandler(new ChargingProfileHandler(handler, client));

    et.onStateChanged(ChargerState.Charging);
    float energyInterval = et.getEnergyActiveImportInterval(0);
    assertEquals(0, energyInterval, "Energy consumption for a zero interval should be zero.");
  }
}
