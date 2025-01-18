package com.sim_backend.transactions;

import com.sim_backend.state.IllegalStateException;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.*;
import com.sim_backend.websockets.messages.*;
import java.time.Instant;
import lombok.Getter;

/*
 * This class is handeling start transaction, send authorize request
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
  public void startTransaction(int connectorId, String idTag, int meterStart, String timestamp) {
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
          System.out.println("even here??");
          if (!(message.getMessage() instanceof AuthorizeResponse response)) {
            throw new ClassCastException("Message is not an AuthorizeResponse");
          }
          if (response.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED) {
            System.out.println("Authorization Accepted... Proceeding Start Transaction...");
            stateMachine.transition(SimulatorState.Preparing);
            initiateTransaction(connectorId, idTag);
          } else {
            System.err.println(
                "Authorize Denied... Status : " + response.getIdTagInfo().getStatus());
            stateMachine.transition(SimulatorState.Available);
          }
        });
  }

  /*
   * Initiate Transaction after authorization is completed.
   * Handeling StartTransaction Request, Response and simulator status
   */
  private void initiateTransaction(int connectorId, String idTag) {

    stateMachine.transition(SimulatorState.Charging);
    int meterStart = 0;
    String timestamp = Instant.now().toString();

    StartTransaction startTransactionMessage =
        new StartTransaction(connectorId, idTag, meterStart, timestamp);
    client.pushMessage(startTransactionMessage);

    client.onReceiveMessage(
        StartTransactionResponse.class,
        message -> {
          if (!(message.getMessage() instanceof StartTransactionResponse response)) {
            throw new ClassCastException("Message is not an StartTransactionResponse");
          }
          if (response.getIdTaginfo().getStatus() == "Accepted") {
            System.out.println(
                "Transaction Started... Transaction Id : " + response.getTransactionId());
            stateMachine.transition(SimulatorState.Charging);
          } else {
            System.err.println("Transaction Failed...");
            stateMachine.transition(SimulatorState.Available);
          }
        });
  }
}
