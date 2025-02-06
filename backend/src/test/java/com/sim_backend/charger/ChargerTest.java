package com.sim_backend.charger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class ChargerTest {

  // Construction mocks for dependencies to avoid network connections
  private MockedConstruction<OCPPWebSocketClient> wsClientConstruction;
  private MockedConstruction<ChargerLoop> chargerLoopConstruction;

  @AfterEach
  void tearDown() {
    if (wsClientConstruction != null) {
      wsClientConstruction.close();
    }
    if (chargerLoopConstruction != null) {
      chargerLoopConstruction.close();
    }
  }

  /** This test verifies that calling Boot() initializes the Charger and its components */
  @Test
  void testBoot() throws Exception {
    // Set up mocks for the components that would create network connections
    wsClientConstruction =
        Mockito.mockConstruction(
            OCPPWebSocketClient.class,
            (mock, context) -> {
              doNothing().when(mock).close(anyInt(), anyString());
            });
    chargerLoopConstruction =
        Mockito.mockConstruction(
            ChargerLoop.class,
            (mock, context) -> {
              doNothing().when(mock).requestStop();
              // Stub run() to simulate a long-running loop
              doAnswer(
                      invocation -> {
                        while (!Thread.currentThread().isInterrupted()) {
                          try {
                            Thread.sleep(100);
                          } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                          }
                        }
                        return null;
                      })
                  .when(mock)
                  .run();
            });

    // Start the charger
    Charger charger = new Charger();
    charger.Boot();

    // Verify that the components were created
    assertNotNull(charger.getStateMachine(), "State machine should be initialized");
    assertNotNull(charger.getElec(), "Electrical transition should be initialized");
    assertNotNull(charger.getWsClient(), "WebSocket client should be initialized");
    assertNotNull(charger.getTransactionHandler(), "Transaction handler should be initialized");

    // Verify that the charger loop was created
    assertNotNull(charger.getChargerLoop(), "Charger loop should be initialized");
    assertEquals(
        1,
        chargerLoopConstruction.constructed().size(),
        "Exactly one charger loop should be constructed");

    // Verify that the charger thread is created and is running
    assertNotNull(charger.getChargerThread(), "Charger thread should be initialized");
    Thread.sleep(50); // Give the thread time to start
    assertTrue(charger.getChargerThread().isAlive(), "Charger thread should be alive");

    charger.getChargerLoop().requestStop();
    charger.getChargerThread().interrupt();
    charger.getChargerThread().join();
  }

  /**
   * This test confirms that calling Reboot() shuts down the current components and then
   * reinitializes the Charger
   */
  @Test
  void testReboot() throws Exception {
    // Set up mocks for the components that would create network connections
    wsClientConstruction =
        Mockito.mockConstruction(
            OCPPWebSocketClient.class,
            (mock, context) -> {
              doNothing().when(mock).close(anyInt(), anyString());
            });
    chargerLoopConstruction =
        Mockito.mockConstruction(
            ChargerLoop.class,
            (mock, context) -> {
              doNothing().when(mock).requestStop();
              // Stub run() to simulate a long-running loop
              doAnswer(
                      invocation -> {
                        while (!Thread.currentThread().isInterrupted()) {
                          try {
                            Thread.sleep(100);
                          } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                          }
                        }
                        return null;
                      })
                  .when(mock)
                  .run();
            });

    // Start the charger
    Charger charger = new Charger();
    charger.Boot();

    // Save references to the current components
    OCPPWebSocketClient oldWsClient = charger.getWsClient();
    ChargerStateMachine oldStateMachine = charger.getStateMachine();
    TransactionHandler oldTHandler = charger.getTransactionHandler();
    Thread oldThread = charger.getChargerThread();

    // Reboot
    charger.Reboot();
    Thread.sleep(2100); // Allow time for the reboot to complete

    // Verify that new component instances were created
    assertNotNull(charger.getWsClient(), "WebSocket client should be reinitialized after reboot");
    assertNotSame(
        oldWsClient,
        charger.getWsClient(),
        "A new WebSocket client instance should be created after reboot");

    assertNotNull(charger.getStateMachine(), "State machine should be reinitialized after reboot");
    assertNotSame(
        oldStateMachine,
        charger.getStateMachine(),
        "A new state machine instance should be created after reboot");

    assertNotNull(
        charger.getTransactionHandler(),
        "Transaction handler should be reinitialized after reboot");
    assertNotSame(
        oldTHandler,
        charger.getTransactionHandler(),
        "A new transaction handler instance should be created after reboot");

    // Verify that a new charger thread is running
    assertNotNull(
        charger.getChargerThread(), "Charger thread should be reinitialized after reboot");
    assertNotSame(
        oldThread,
        charger.getChargerThread(),
        "A new charger thread should be created after reboot");
    Thread.sleep(50); // Give the thread time to start
    assertTrue(
        charger.getChargerThread().isAlive(), "New charger thread should be alive after reboot");

    // Verify that the reboot lock is released
    assertFalse(
        charger.isRebootInProgress(), "Reboot should not be in progress after reboot completes");

    charger.getChargerLoop().requestStop();
    charger.getChargerThread().interrupt();
    charger.getChargerThread().join();
  }
}
