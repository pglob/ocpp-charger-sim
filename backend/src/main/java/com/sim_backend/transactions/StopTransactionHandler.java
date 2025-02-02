package com.sim_backend.transactions;

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

  /** Initiate Stop Transaction Handling StopTransaction Request, Response and simulator status */
  public void initiateStopTransaction(int transactionId, String idTag) {

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
        message -> {
          if (!(message.getMessage() instanceof StopTransactionResponse response)) {
            throw new ClassCastException("Message is not a StopTransactionResponse");
          }
          if (response.getIdTaginfo().getStatus() == AuthorizationStatus.ACCEPTED) {
            System.out.println("Stop Transaction Completed...");
            stateMachine.transition(SimulatorState.Available);
          } else {
            System.err.println("Transaction Failed to Stop...");
            stateMachine.transition(SimulatorState.Charging);
          }
        });

    client.clearOnReceiveMessage(StopTransactionResponse.class);
  }
}
