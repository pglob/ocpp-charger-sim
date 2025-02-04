package com.sim_backend.simulator;

import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.observers.BootNotificationObserver;
import java.net.URI;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;

/**
 * Represents a Simulator that mimics the behavior of an EV charger. The Simulator contains a state
 * machine, electrical transition, WebSocket client, and transaction handler
 */
@Getter
public class Simulator {

  /** The WebSocket client used to communicate with the central syste. */
  private OCPPWebSocketClient wsClient;

  /** The state machine representing the current state of the simulator */
  private SimulatorStateMachine stateMachine;

  /** The configuration registry containing simulator settings */
  private final ConfigurationRegistry config;

  /** The electrical transition for tracking charging parameters */
  private ElectricalTransition elec;

  /** The thread running the simulator loop */
  private Thread simulatorThread;

  /** The handler for transaction-related operations */
  private TransactionHandler transactionHandler;

  /** The loop that runs the simulator processe. */
  private SimulatorLoop simulatorLoop;

  /** A lock to ensure that only one Boot() or Reboot() operation can run at a time */
  private final ReentrantLock bootRebootLock = new ReentrantLock();

  /** Constructs a new Simulator instance */
  public Simulator() {
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
   * Boots the simulator. This method initializes the simulator's components, including the state
   * machine, electrical transition, WebSocket client, and transaction handler. It also starts the
   * simulator loop in a separate thread
   */
  public void Boot() {
    // If another Boot/Reboot is in progress, do nothing
    if (!bootRebootLock.tryLock()) {
      return;
    }
    try {
      // Create the Simulator's components
      stateMachine = new SimulatorStateMachine();
      elec = new ElectricalTransition(stateMachine);
      wsClient = new OCPPWebSocketClient(URI.create(config.getCentralSystemUrl()));
      transactionHandler = new TransactionHandler(this);

      // Create Observers
      BootNotificationObserver bootObserver = new BootNotificationObserver(wsClient, stateMachine);

      // Transition the state machine to the BootingUp state
      stateMachine.transition(SimulatorState.BootingUp);

      // Start the simulator loop in its own thread
      simulatorLoop = new SimulatorLoop(this);
      simulatorThread = new Thread(simulatorLoop);
      simulatorThread.start();
    } finally {
      bootRebootLock.unlock();
    }
  }

  /**
   * Reboots the simulator. This method stops any in-progress charging session, shuts down the
   * simulator loop, resets the internal components, and then calls {@link #Boot()} to restart the
   * simulator
   */
  public void Reboot() {
    // If another Boot/Reboot is in progress, do nothing
    if (!bootRebootLock.tryLock()) {
      return;
    }
    try {
      // Stop any current transaction/charging session
      transactionHandler.StopCharging(null);
      stateMachine.transition(SimulatorState.PoweredOff);

      // Signal the simulator loop to stop and interrupt its thread
      if (simulatorThread != null) {
        simulatorLoop.requestStop();
        simulatorThread.interrupt();
        try {
          simulatorThread.join();
        } catch (InterruptedException e) {
        }
      }

      // As per RFC 6455, 1001 indicates the endpoint is "going away"
      wsClient.close(1001, "Charger rebooting");
      wsClient = null;
      transactionHandler = null;
      stateMachine = null;

      // Force garbage collection and wait a bit before restarting
      System.gc();
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
      }

      // Restart the simulator by calling Boot(). As ReentrantLock is reentrant,
      // the current thread can acquire the lock again
      Boot();
    } finally {
      bootRebootLock.unlock();
    }
  }
}
