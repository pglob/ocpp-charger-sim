package com.sim_backend.simulator;

import com.sim_backend.websockets.exceptions.OCPPMessageFailure;

/**
 * Represents the main loop of the Simulator. This loop is responsible for scheduling and processing
 * messages for the WebSocket client associated with the Simulator. It runs in its own thread and
 * can be stopped by setting a flag or interrupting the thread
 */
public class SimulatorLoop implements Runnable {

  /** A flag indicating when the loop should stop */
  private volatile boolean stopRequested = false;

  /** The Simulator instance associated with this loop */
  private final Simulator sim;

  /**
   * Constructs a new SimulatorLoop for the given Simulator
   *
   * @param sim the Simulator instance whose WebSocket client will be used for processing messages
   */
  public SimulatorLoop(Simulator sim) {
    this.sim = sim;
  }

  /** Requests that the loop stops processing */
  public void requestStop() {
    stopRequested = true;
  }

  /**
   * Processes one iteration of the Simulator loop
   *
   * @return {@code true} if the processing should continue, or {@code false} if the loop should
   *     exit
   */
  public boolean process() {
    try {
      sim.getWsClient().getScheduler().tick();
      sim.getWsClient().popAllMessages();
    } catch (OCPPMessageFailure e) {
      // TODO: Add error handling for OCPP message failures
    } catch (InterruptedException e) {
      // Break out of the loop when interrupted.
      return false;
    }
    return true;
  }

  /** Runs the Simulator loop */
  @Override
  public void run() {
    while (!stopRequested && !Thread.currentThread().isInterrupted()) {
      if (!process()) break;
      try {
        // A small delay to prevent the loop from running too fast,
        // allowing the WebSocket client to process messages
        Thread.sleep(100);
      } catch (InterruptedException e) {
        break;
      }
    }
    process();
  }
}
