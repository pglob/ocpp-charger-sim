package com.sim_backend.simulator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class SimulatorTest {

  // Construction mocks for dependencies to avoid network connections
  private MockedConstruction<OCPPWebSocketClient> wsClientConstruction;
  private MockedConstruction<SimulatorLoop> simLoopConstruction;

  @AfterEach
  void tearDown() {
    if (wsClientConstruction != null) {
      wsClientConstruction.close();
    }
    if (simLoopConstruction != null) {
      simLoopConstruction.close();
    }
  }

  /** This test verifies that calling Boot() initializes the Simulator and its components */
  @Test
  void testBoot() throws Exception {
    // Set up mocks for the components that would create network connections
    wsClientConstruction =
        Mockito.mockConstruction(
            OCPPWebSocketClient.class,
            (mock, context) -> {
              doNothing().when(mock).close(anyInt(), anyString());
            });
    simLoopConstruction =
        Mockito.mockConstruction(
            SimulatorLoop.class,
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

    // Start the simulator
    Simulator sim = new Simulator();
    sim.Boot();

    // Verify that the components were created
    assertNotNull(sim.getStateMachine(), "State machine should be initialized");
    assertNotNull(sim.getElec(), "Electrical transition should be initialized");
    assertNotNull(sim.getWsClient(), "WebSocket client should be initialized");
    assertNotNull(sim.getTransactionHandler(), "Transaction handler should be initialized");

    // Verify that the SimulatorLoop was created
    assertNotNull(sim.getSimulatorLoop(), "Simulator loop should be initialized");
    assertEquals(
        1,
        simLoopConstruction.constructed().size(),
        "Exactly one SimulatorLoop should be constructed");

    // Verify that the simulator thread is created and is running
    assertNotNull(sim.getSimulatorThread(), "Simulator thread should be initialized");
    Thread.sleep(50); // Give the thread time to start
    assertTrue(sim.getSimulatorThread().isAlive(), "Simulator thread should be alive");

    sim.getSimulatorLoop().requestStop();
    sim.getSimulatorThread().interrupt();
    sim.getSimulatorThread().join();
  }

  /**
   * This test confirms that calling Reboot() shuts down the current components and then
   * reinitializes the Simulator
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
    simLoopConstruction =
        Mockito.mockConstruction(
            SimulatorLoop.class,
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

    // Start the simulator
    Simulator sim = new Simulator();
    sim.Boot();

    // Save references to the current components
    OCPPWebSocketClient oldWsClient = sim.getWsClient();
    SimulatorStateMachine oldStateMachine = sim.getStateMachine();
    TransactionHandler oldTHandler = sim.getTransactionHandler();
    Thread oldThread = sim.getSimulatorThread();

    // Reboot
    sim.Reboot();
    Thread.sleep(2100); // Allow time for the reboot to complete

    // Verify that new component instances were created
    assertNotNull(sim.getWsClient(), "WebSocket client should be reinitialized after reboot");
    assertNotSame(
        oldWsClient,
        sim.getWsClient(),
        "A new WebSocket client instance should be created after reboot");

    assertNotNull(sim.getStateMachine(), "State machine should be reinitialized after reboot");
    assertNotSame(
        oldStateMachine,
        sim.getStateMachine(),
        "A new state machine instance should be created after reboot");

    assertNotNull(
        sim.getTransactionHandler(), "Transaction handler should be reinitialized after reboot");
    assertNotSame(
        oldTHandler,
        sim.getTransactionHandler(),
        "A new transaction handler instance should be created after reboot");

    // Verify that a new simulator thread is running
    assertNotNull(
        sim.getSimulatorThread(), "Simulator thread should be reinitialized after reboot");
    assertNotSame(
        oldThread,
        sim.getSimulatorThread(),
        "A new simulator thread should be created after reboot");
    Thread.sleep(50); // Give the thread time to start
    assertTrue(
        sim.getSimulatorThread().isAlive(), "New simulator thread should be alive after reboot");

    // Verify that the reboot lock is released
    assertFalse(
        sim.isRebootInProgress(), "Reboot should not be in progress after reboot completes");

    sim.getSimulatorLoop().requestStop();
    sim.getSimulatorThread().interrupt();
    sim.getSimulatorThread().join();
  }
}
