package com.sim_backend.transactions;

import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.messages.Authorize;
import com.sim_backend.websockets.messages.AuthorizeResponse;
import lombok.Getter;

/** This TransactionHandler manages Start and Stop operation */
@Getter
public class TransactionHandler {
  private StartTransactionHandler startHandler;
  private StopTransactionHandler stopHandler;
  private SimulatorStateMachine stateMachine;
  private OCPPWebSocketClient client;
  private String idTag;
  private int transactionId;

  // Constructor
  public TransactionHandler(SimulatorStateMachine stateMachine, OCPPWebSocketClient client) {
    startHandler = new StartTransactionHandler(stateMachine, client);
    stopHandler = new StopTransactionHandler(stateMachine, client);
    this.stateMachine = stateMachine;
    this.client = client;
    this.idTag = null;
    this.transactionId = -1;
  }

  /**
   * Authorization process before StartTransaction or StopTransaction. When Authorize is accepted
   * handles Start or Stop initiation
   *
   * @param connectorId ID of the connector
   * @param idTag ID of the user
   */
  public void preAuthorize(int connectorId, String idTag) {
    Authorize authorizeMessage = new Authorize(idTag);
    client.pushMessage(authorizeMessage);

    client.onReceiveMessage(
        AuthorizeResponse.class,
        message -> {
          if (!(message.getMessage() instanceof AuthorizeResponse response)) {
            throw new ClassCastException("Message is not an AuthorizeResponse");
          }

          if (response.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED) {
            System.out.println("Authorization Accepted...");
            System.out.println("Proceeding with Transaction...");
            if (stateMachine.getCurrentState() == SimulatorState.Preparing) {
              startHandler.initiateStartTransaction(connectorId, idTag);
              this.transactionId = startHandler.getTransactionId();
              this.idTag = idTag;
              /*
               * TODO : Delete line 59 if it's not required.
               */
              stateMachine.transition(startHandler.getStateMachine().getCurrentState());
            } else if (stateMachine.getCurrentState() == SimulatorState.Charging) {
              stopHandler.initiateStopTransaction(this.transactionId, idTag);
              /*
               * TODO : Delete line 65 if it's not required.
               */
              stateMachine.transition(stopHandler.getStateMachine().getCurrentState());
            } else {
              System.err.println(
                  "Invalid State Detected... Current State : " + stateMachine.getCurrentState());
            }
          } else {
            System.err.println(
                "Authorize Denied... Status : " + response.getIdTagInfo().getStatus());
            stateMachine.transition(SimulatorState.Available);
          }
        });
  }

  /**
   * StartCharging Resume Charging when authorized before Otherwise, Authorize and then start
   * charging
   *
   * @param connectorId Id of connector
   * @param idTag id of user
   */
  public void StartCharging(int connectorId, String idTag) {
    stateMachine.transition(SimulatorState.Preparing);
    startHandler.getStateMachine().transition(SimulatorState.Preparing);
    if (this.idTag != null && this.idTag.equals(idTag)) {
      startHandler.initiateStartTransaction(connectorId, idTag);
      stateMachine.transition(startHandler.getStateMachine().getCurrentState());
    } else if (this.idTag != idTag) {
      preAuthorize(connectorId, idTag);
    }
  }

  /**
   * StopCharging if idTag is authorized before and have the same id then stop Otherwise, Authorize
   * and then stop charging
   *
   * @param idTag id of user
   */
  public void StopCharging(String idTag) {
    if (this.idTag == idTag) {
      stopHandler.initiateStopTransaction(this.transactionId, idTag);
      stateMachine.transition(stopHandler.getStateMachine().getCurrentState());
    } else {
      preAuthorize(0, idTag);
    }
  }
}
