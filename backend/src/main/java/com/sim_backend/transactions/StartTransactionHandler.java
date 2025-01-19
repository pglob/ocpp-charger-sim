package com.sim_backend.transactions;

import com.sim_backend.state.IllegalStateException;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.*;
import com.sim_backend.websockets.messages.*;
import lombok.Getter;

/*
 * This class handles sending an authorize request, followed by a StartTransaction message.
 *
 */
@Getter
public class StartTransactionHandler {
  private SimulatorStateMachine stateMachine;
  private OCPPWebSocketClient client;

  // Constructor
  public StartTransactionHandler(SimulatorStateMachine stateMachine, OCPPWebSocketClient client) {
    this.stateMachine = stateMachine;
    this.client = client;
  }

  /*
   *  Start Transaction
   */
  public void preAuthorize(int connectorId, String idTag, int meterStart, String timestamp) {
    // transition to Preparing, check if StateMachine is Available
    if (stateMachine.getCurrentState() != SimulatorState.Available) {
      throw new IllegalStateException(
          "Cannot start with the current state. current state: " + stateMachine.getCurrentState());
    }

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
            System.out.println("Proceeding Transaction...");
            stateMachine.transition(SimulatorState.Preparing);
            initiateStartTransaction(
                connectorId, response.getIdTagInfo().getStatus(), meterStart, timestamp);
          } else {
            System.err.println(
                "Authorize Denied... Status : " + response.getIdTagInfo().getStatus());
            stateMachine.transition(SimulatorState.Available);
          }
        });
  }

  /*
   * Initiate Start Transaction after authorization is completed.
   * Handling StartTransaction Request, Response and simulator status
   */
  private void initiateStartTransaction(
      int connectorId, AuthorizationStatus idTag, int meterStart, String timestamp) {

    /*
     * TODO : Swap meterStart value, Time info
     */
    int tempMeterStart = 0;
    String tempTimestamp = "2025-01-19T00:00:00Z";

    StartTransaction startTransactionMessage =
        /*
         * TODO : Change temp arguments to meterStart, timestamp
         */
        new StartTransaction(connectorId, idTag, tempMeterStart, tempTimestamp);
    client.pushMessage(startTransactionMessage);

    client.onReceiveMessage(
        StartTransactionResponse.class,
        message -> {
          if (!(message.getMessage() instanceof StartTransactionResponse response)) {
            throw new ClassCastException("Message is not an StartTransactionResponse");
          }
          if (response.getIdTaginfo().getStatus() == AuthorizationStatus.ACCEPTED) {
            System.out.println(
                "Transaction Completed... Transaction Id : " + response.getTransactionId());
            stateMachine.transition(SimulatorState.Charging);
          } else {
            System.err.println("Transaction Failed...");
            stateMachine.transition(SimulatorState.Available);
          }
        });
  }
}
