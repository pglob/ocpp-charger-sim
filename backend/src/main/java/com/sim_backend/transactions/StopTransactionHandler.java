package com.sim_backend.transactions;

import com.sim_backend.state.IllegalStateException;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.*;
import com.sim_backend.websockets.messages.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

  /**
   * Authorization Process before initiating Stop Transaction
   *
   * @param idTag ID of an user
   * @throws IllegalStateException If stateMachine status is not a Charging state throw error
   */
  public void preAuthorize(String idTag) {
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
          stateMachine.transition(SimulatorState.Preparing);
          if (response.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED) {
            System.out.println("Authorization Accepted...");
            System.out.println("Proceeding Transaction...");
          } else {
            System.err.println(
                "Authorize Denied... Status : " + response.getIdTagInfo().getStatus());
            stateMachine.transition(SimulatorState.Charging);
          }
        });
  }

  /**
   * Initiate Stop Transaction Handling StopTransaction Request, Response and simulator status If
   * authorization is accepted, change a stateMachine status from Charging to Available Stays
   * Charging otherwise
   */
  public void initiateStopTransaction(int transactionId) {

    /*
     * TODO : Swap meterStop
     */
    int meterStop = 10;

    OCPPTime ocppTime = client.getScheduler().getTime();
    ZonedDateTime zonetime = ocppTime.getSynchronizedTime();
    String timestamp = zonetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"));

    StopTransaction stopTransactionMessage =
        new StopTransaction(transactionId, meterStop, timestamp);
    client.pushMessage(stopTransactionMessage);

    client.onReceiveMessage(
        StopTransactionResponse.class,
        message2 -> {
          if (!(message2.getMessage() instanceof StopTransactionResponse response2)) {
            throw new ClassCastException("Message is not a StopTransactionResponse");
          }
          if (response2.getIdTaginfo().getStatus() == AuthorizationStatus.ACCEPTED) {
            System.out.println("Transaction Completed...");
            stateMachine.transition(SimulatorState.Available);
          } else {
            System.err.println("Transaction Failed...");
            stateMachine.transition(SimulatorState.Charging);
          }
        });

    client.clearOnReceiveMessage(StopTransactionResponse.class);
  }

  public void StopCharging(String preAuthidTag, String postAuthidTag, int transactionId) {
    if (preAuthidTag != postAuthidTag) {
      preAuthorize(postAuthidTag);
      if (stateMachine.getCurrentState() != SimulatorState.Charging) {
        throw new IllegalStateException(
            "Cannot stop with the current state. current state: " + stateMachine.getCurrentState());
      }
      initiateStopTransaction(transactionId);
    } else {
      initiateStopTransaction(transactionId);
    }
  }
}
