package com.sim_backend.transactions;

import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import lombok.Getter;

/**
 * 
 * This TransactionHandler manages Start and Stop opreation
 * 
 */
@Getter
public class TransactionHandler {
  private StartTransactionHandler startHandler;
  private StopTransactionHandler stopHandler;

  // Constructor
  public TransactionHandler(SimulatorStateMachine stateMachine, OCPPWebSocketClient client) {
    startHandler = new StartTransactionHandler(stateMachine, client);
    stopHandler = new StopTransactionHandler(stateMachine, client);
  }

  // Initial Start Charging for the first time
  public void PreAuthlStartCharging(int connectorId, String idTag) {
    startHandler.preAuthStartCharging(connectorId, idTag);
  }

  /**
   * Resume Charging when authorized before
   * @param connectorId
   * @param configIdTag from Configuration registry data
   */
  public void PostAuthStartCharging(int connectorId, String configIdTag) {
    startHandler.initiateStartTransaction(connectorId, configIdTag);
  }

  // Stop Charging
  /**
   * 
   * @param transactionId
   * @param configIdTag from Configuration Registry data
   * @param idTag idTag of user
   */
  public void StopCharging(int transactionId, String configIdTag, String idTag) {
    stopHandler.StopCharging(configIdTag, idTag, transactionId);
  }
}
