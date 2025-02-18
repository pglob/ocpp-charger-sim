package com.sim_backend.charger;

import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;

/**
 * Represents the main loop of the Charger. This loop is responsible for scheduling and processing
 * messages for the WebSocket client associated with the Charger. It runs in its own thread and can
 * be stopped by setting a flag or interrupting the thread
 */
public class ChargerLoop implements Runnable {

  /** A flag indicating when the loop should stop */
  private volatile boolean stopRequested = false;

  /** The Charger instance associated with this loop */
  private final Charger charger;

  /**
   * Constructs a new ChargerLoop for the given Charger
   *
   * @param charger the Charger instance whose WebSocket client will be used for processing messages
   */
  public ChargerLoop(Charger charger) {
    this.charger = charger;
  }

  /** Requests that the loop stops processing */
  public void requestStop() {
    stopRequested = true;
  }

  /**
   * Processes one iteration of the Charger loop
   *
   * @return {@code true} if the processing should continue, or {@code false} if the loop should
   *     exit
   */
  public boolean process() {
    try {
      OCPPWebSocketClient wsClient = charger.getWsClient();
      wsClient.getScheduler().tick();
      wsClient.getQueue().checkTimeouts(wsClient);
      wsClient.popAllMessages();
    } catch (OCPPMessageFailure e) {
      // TODO: Add error handling for OCPP message failures
    } catch (InterruptedException e) {
      // Break out of the loop when interrupted.
      return false;
    }
    return true;
  }

  /** Runs the Charger loop */
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
