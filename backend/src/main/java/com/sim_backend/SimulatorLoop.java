package com.sim_backend;

import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;

public class SimulatorLoop {

  /**
   * Run the main simulator loop.
   *
   * @param wsClient the WebSocket client
   */
  static void runSimulatorLoop(OCPPWebSocketClient wsClient) {
    while (true) {
      try {
        wsClient.getScheduler().tick();
        wsClient.popAllMessages();
      } catch (OCPPMessageFailure e) {
        // TODO: Add error handling for OCPP message failures
      } catch (InterruptedException e) {
        break;
      }
      try {
        // A small delay to prevent the loop from running too fast,
        // allowing the WebSocket client to process messages
        Thread.sleep(100);
      } catch (InterruptedException e) {
        break;
      }
    }
  }
}
