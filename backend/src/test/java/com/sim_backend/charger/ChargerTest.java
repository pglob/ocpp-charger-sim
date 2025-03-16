package com.sim_backend.charger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.Reason;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
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

  /**
   * This test verifies that the WebSocket client is created with the proper URI. The expected URI
   * is constructed from the ConfigurationRegistry values.
   */
  @Test
  void testWsClientUri() throws Exception {
    // Prepare a container to capture the the URI
    List<Object> wsClientConstructorArgs = new ArrayList<>();

    wsClientConstruction =
        Mockito.mockConstruction(
            OCPPWebSocketClient.class,
            (mock, context) -> {
              wsClientConstructorArgs.add(context.arguments().get(0));
              doNothing().when(mock).close(anyInt(), anyString());
            });
    chargerLoopConstruction =
        Mockito.mockConstruction(
            ChargerLoop.class,
            (mock, context) -> {
              doNothing().when(mock).requestStop();
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

    Charger charger = new Charger(0);
    charger.boot();

    // Verify that the WebSocket client was constructed
    assertNotNull(charger.getWsClient(), "WebSocket client should be initialized");
    assertEquals(1, wsClientConstructorArgs.size(), "One wsClient should have been constructed");

    Object arg = wsClientConstructorArgs.get(0);
    assertTrue(arg instanceof URI, "The first constructor argument should be a URI");
    URI wsUri = (URI) arg;
    String expectedUri = "ws://host.docker.internal:9000/test";
    assertEquals(
        expectedUri, wsUri.toString(), "WebSocket client URI should match the expected value");

    charger.getChargerLoop().requestStop();
    charger.getChargerThread().interrupt();
    charger.getChargerThread().join();
  }

  /** This test verifies that calling boot() initializes the Charger and its components */
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
    Charger charger = new Charger(0);
    charger.boot();

    // Verify that the components were created
    assertNotNull(charger.getStateMachine(), "State machine should be initialized");
    assertNotNull(charger.getElec(), "Electrical transition should be initialized");
    assertNotNull(charger.getWsClient(), "WebSocket client should be initialized");
    assertNotNull(charger.getTransactionHandler(), "Transaction handler should be initialized");

    // Check that the configuration is set and default availability is true
    assertNotNull(charger.getConfig(), "Configuration should be initialized");
    assertTrue(charger.isAvailable(), "Charger should be available by default");

    // Verify that the state machine is transitioned to BootingUp state
    assertEquals(
        ChargerState.BootingUp,
        charger.getStateMachine().getCurrentState(),
        "State machine should be in BootingUp state after boot");

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
   * This test confirms that calling reboot() shuts down the current components and then
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
    Charger charger = new Charger(0);
    charger.boot();

    // Save references to the current components
    OCPPWebSocketClient oldWsClient = charger.getWsClient();
    ChargerStateMachine oldStateMachine = charger.getStateMachine();
    TransactionHandler oldTHandler = charger.getTransactionHandler();
    Thread oldThread = charger.getChargerThread();

    // Reboot
    charger.reboot();
    Thread.sleep(2100); // Allow time for the reboot to complete

    // Verify that the old wsClient was closed with code 1001 and proper message
    verify(oldWsClient, atLeastOnce()).close(1001, "Charger rebooting");

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

  @Test
  void testIsRebootInProgress() throws Exception {
    Charger charger = new Charger(0);
    // Ensure the charger is not in reboot mode
    assertFalse(charger.isRebootInProgress(), "Reboot should not be in progress initially");

    // Obtain the bootRebootLock and lock it
    Field lockField = Charger.class.getDeclaredField("bootRebootLock");
    lockField.setAccessible(true);
    ReentrantLock lock = (ReentrantLock) lockField.get(charger);
    lock.lock();
    try {
      assertTrue(charger.isRebootInProgress(), "Reboot should be in progress when lock is held");
    } finally {
      lock.unlock();
    }
  }

  @Test
  void testFault_TransitionToFaulted() throws Exception {
    // Test fault() when current state is not Faulted
    Charger charger = new Charger(0);
    charger.boot();

    ChargerStateMachine mockStateMachine = mock(ChargerStateMachine.class);
    TransactionHandler mockTransactionHandler = mock(TransactionHandler.class);
    setField(charger, "stateMachine", mockStateMachine);
    setField(charger, "transactionHandler", mockTransactionHandler);

    when(mockStateMachine.getCurrentState()).thenReturn(ChargerState.Available);

    // Call fault() with an error code
    charger.fault(ChargePointErrorCode.OtherError);
    // Verify forceStopCharging is called with Reason.OTHER
    verify(mockTransactionHandler).forceStopCharging(Reason.OTHER);
    // Verify that transition to Faulted is executed
    verify(mockStateMachine).transition(ChargerState.Faulted);
  }

  @Test
  void testFault_AlreadyFaulted() throws Exception {
    // Test fault() when current state is already Faulted
    Charger charger = new Charger(0);
    charger.boot();

    ChargerStateMachine mockStateMachine = mock(ChargerStateMachine.class);
    TransactionHandler mockTransactionHandler = mock(TransactionHandler.class);
    setField(charger, "stateMachine", mockStateMachine);
    setField(charger, "transactionHandler", mockTransactionHandler);

    when(mockStateMachine.getCurrentState()).thenReturn(ChargerState.Faulted);

    charger.fault(ChargePointErrorCode.OtherError);
    verify(mockTransactionHandler).forceStopCharging(Reason.OTHER);
    // Verify no transition is performed
    verify(mockStateMachine, never()).transition(ChargerState.Faulted);
  }

  @Test
  void testClearFault() throws Exception {
    // Test that clearFault() transitions the charger state to Available
    Charger charger = new Charger(0);
    charger.boot();

    ChargerStateMachine mockStateMachine = mock(ChargerStateMachine.class);
    setField(charger, "stateMachine", mockStateMachine);

    charger.clearFault();
    verify(mockStateMachine).checkAndTransition(ChargerState.Faulted, ChargerState.Available);
  }

  /**
   * Helper method to set a private field via reflection.
   *
   * @param target the object containing the field.
   * @param fieldName the name of the field.
   * @param value the value to set.
   * @throws Exception if reflection fails.
   */
  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
