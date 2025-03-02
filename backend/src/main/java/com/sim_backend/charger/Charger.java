package com.sim_backend.charger;

import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.Reason;
import com.sim_backend.websockets.observers.*;
import java.net.URI;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;

/**
 * Represents a simulated EV charger. The Charger contains a state machine, electrical transition,
 * WebSocket client, and transaction handler
 */
@Getter
public class Charger {

  /** The WebSocket client used to communicate with the central syste. */
  private OCPPWebSocketClient wsClient;

  /** The state machine representing the current state of the charger */
  private ChargerStateMachine stateMachine;

  /** The configuration registry containing charger settings */
  private final ConfigurationRegistry config;

  /** The electrical transition for tracking charging parameters */
  private ElectricalTransition elec;

  /** The thread running the charger loop */
  private Thread chargerThread;

  /** The handler for transaction-related operations */
  private TransactionHandler transactionHandler;

  /** The loop that runs the charger processes */
  private ChargerLoop chargerLoop;

  /** A lock to ensure that only one Boot() or Reboot() operation can run at a time */
  private final ReentrantLock bootRebootLock = new ReentrantLock();

  private StatusNotificationObserver statusNotificationObserver;

  /** Constructs a new Charger instance */
  public Charger() {
    // TODO: Get central system URI from frontend or command line
    this.config = new ConfigurationRegistry("temptag", "ws://host.docker.internal:9000");
  }

  /**
   * Checks if a reboot is currently in progress
   *
   * @return {@code true} if a Boot or Reboot operation is currently running, {@code false}
   *     otherwise
   */
  public boolean isRebootInProgress() {
    return bootRebootLock.isLocked();
  }

  /**
   * Boots the charger. This method initializes the charger's components, including the state
   * machine, electrical transition, WebSocket client, and transaction handler. It also starts the
   * charger loop in a separate thread
   */
  public void boot() {
    // If another Boot/Reboot is in progress, do nothing
    if (!bootRebootLock.tryLock()) {
      return;
    }
    try {
      statusNotificationObserver = new StatusNotificationObserver();
      // Create the Charger's components
      stateMachine = new ChargerStateMachine();
      elec = new ElectricalTransition(stateMachine);
      wsClient =
          new OCPPWebSocketClient(
              URI.create(config.getCentralSystemUrl()), statusNotificationObserver);
      transactionHandler = new TransactionHandler(this);

      // Create Observers
      BootNotificationObserver bootObserver = new BootNotificationObserver(wsClient, stateMachine);
      ChangeConfigurationObserver changeConfigurationObserver =
          new ChangeConfigurationObserver(wsClient, config);
      GetConfigurationObserver getConfigurationObserver =
          new GetConfigurationObserver(wsClient, config);
      ChangeAvailabilityObserver changeAvailabilityObserver =
          new ChangeAvailabilityObserver(wsClient);
      statusNotificationObserver.setClient(wsClient);

      // Add Observers
      stateMachine.addObserver(statusNotificationObserver);

      // Transition the state machine to the BootingUp state
      stateMachine.transition(ChargerState.BootingUp);

      // Start the charger loop in its own thread
      chargerLoop = new ChargerLoop(this);
      chargerThread = new Thread(chargerLoop);
      chargerThread.start();
    } finally {
      bootRebootLock.unlock();
    }
  }

  /**
   * Reboots the charger. This method stops any in-progress charging session, shuts down the charger
   * loop, resets the internal components, and then calls {@link #boot()} to restart the charger
   */
  public void reboot() {
    // If another Boot/Reboot is in progress, do nothing
    if (!bootRebootLock.tryLock()) {
      return;
    }
    try {
      // Stop any current transaction/charging session
      transactionHandler.forceStopCharging(Reason.REBOOT);
      stateMachine.transition(ChargerState.PoweredOff);

      // Signal the charger loop to stop and interrupt its thread
      if (chargerThread != null) {
        chargerLoop.requestStop();
        chargerThread.interrupt();
        try {
          chargerThread.join();
        } catch (InterruptedException e) {
        }
      }

      // As per RFC 6455, 1001 indicates the endpoint is "going away"
      wsClient.close(1001, "Charger rebooting");
      wsClient = null;
      transactionHandler = null;
      stateMachine = null;

      // Wait a bit before restarting to simulate power cycling
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
      }

      // Restart the charger by calling Boot(). As ReentrantLock is reentrant,
      // the current thread can acquire the lock again
      boot();
    } finally {
      bootRebootLock.unlock();
    }
  }

  /**
   * Puts the charger in a faulted state. A POST to /api/{chargerId}/charger/clear-fault, a call to
   * ClearFault() or a Reboot is required to return to normal operation.
   *
   * @param error the error code that caused a fault
   */
  public void fault(ChargePointErrorCode error) {
    transactionHandler.forceStopCharging(Reason.OTHER);
    if (stateMachine.getCurrentState() != ChargerState.Faulted) {
      stateMachine.transition(ChargerState.Faulted);
    }
  }

  /** Returns the charger from a Faulted state to an Available state. */
  public boolean clearFault() {
    return stateMachine.checkAndTransition(ChargerState.Faulted, ChargerState.Available);
  }
}
