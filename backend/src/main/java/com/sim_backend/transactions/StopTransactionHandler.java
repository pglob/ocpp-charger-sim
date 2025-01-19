package com.sim_backend.transactions;

import com.sim_backend.state.IllegalStateException;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.*;
import com.sim_backend.websockets.messages.*;
import lombok.Getter;


/*
 * This class handles sending an authorize request, followed by a StopTransaction message.
 *
 */
@Getter
public class StopTransactionHandler {
  private SimulatorStateMachine stateMachine;
  private OCPPWebSocketClient client;

  // Constructor
  public StopTransactionHandler(SimulatorStateMachine stateMachine, OCPPWebSocketClient client) {
    this.stateMachine = stateMachine;
    this.client = client;
  }

  /*
   *  Stop Transaction
   */
  public void preAuthorize(int transactionId, String idTag, int meterStop, String timestamp) {
    // transition to Available, check if StateMachine is Charging
    if (stateMachine.getCurrentState() != SimulatorState.Charging) {
      throw new IllegalStateException(
          "Cannot stop with the current state. current state: " + stateMachine.getCurrentState());
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
            initiateStopTransaction(transactionId, meterStop, timestamp);
          } else {
            System.err.println(
                "Authorize Denied... Status : " + response.getIdTagInfo().getStatus());
            stateMachine.transition(SimulatorState.Charging);
          }
        });
  }

  /*
   * Initiate Stop Transaction after authorization is completed.
   * Handling StartTransaction Request, Response and simulator status
   */
  private void initiateStopTransaction(int transactionId, int meterStart, String timestamp) {

    /*
     * TODO : Swap meterStart value, Time info
     */
    int tempMeterStop = 10;
    String tempTimestamp = "2025-01-19T00:00:00Z";

    StopTransaction stopTransactionMessage =
        /*
         * TODO : Change temp arguments to meterStart, timestamp
         */
        new StopTransaction(transactionId, tempMeterStop, tempTimestamp);
    client.pushMessage(stopTransactionMessage);

    client.onReceiveMessage(
        StopTransactionResponse.class,
        message -> {
          if (!(message.getMessage() instanceof StopTransactionResponse response)) {
            throw new ClassCastException("Message is not an StopTransactionResponse");
          }
          if (response.getIdTaginfo().getStatus() == AuthorizationStatus.ACCEPTED) {
            System.out.println("Transaction Completed...");
            stateMachine.transition(SimulatorState.Available);
          } else {
            System.err.println("Transaction Failed...");
            stateMachine.transition(SimulatorState.Charging);
          }
        });
  }
}
