package com.sim_backend.transactions;

import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.*;
import com.sim_backend.websockets.messages.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

/*
 * This class handles sending an authorize request, followed by a StartTransaction message.
 *
 */
@Getter
public class StartTransactionHandler {
  private SimulatorStateMachine stateMachine;
  private OCPPWebSocketClient client;
  private int transactionId;

  // Constructor
  public StartTransactionHandler(SimulatorStateMachine stateMachine, OCPPWebSocketClient client) {
    this.stateMachine = stateMachine;
    this.client = client;
    this.transactionId = -1;
  }

  /**
   * Initiate Start Transaction Handling StartTransaction Request, Response and simulator status If
   * authorization is accepted, change a stateMachine status to Charging Switch to Available
   * otherwise
   *
   * @param connectorId ID of connector
   * @param idTag ID of user
   * @param transactionId AtomicInteger used to pass the transactionId back to the
   *     TransactionHandler
   * @param elec ElectricalTransition for retrieving meter values
   * @param stopInProgress AtomicBoolean to prevent initiateStopTransaction from running more than
   *     once
   */
  public void initiateStartTransaction(
      int connectorId,
      String idTag,
      AtomicInteger transactionId,
      ElectricalTransition elec,
      AtomicBoolean startInProgress) {

    // Convert from KWh to Wh
    int meterStart = (int) (elec.getEnergyActiveImportRegister() * 1000.0f);

    OCPPTime ocppTime = client.getScheduler().getTime();
    ZonedDateTime zonetime = ocppTime.getSynchronizedTime();
    String timestamp = zonetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"));

    StartTransaction startTransactionMessage =
        new StartTransaction(connectorId, idTag, meterStart, timestamp);
    client.pushMessage(startTransactionMessage);

    client.onReceiveMessage(
        StartTransactionResponse.class,
        message -> {
          if (!(message.getMessage() instanceof StartTransactionResponse response)) {
            throw new ClassCastException("Message is not an StartTransactionResponse");
          }
          if (response.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED) {
            transactionId.set(response.getTransactionId());
            System.out.println(
                "Start Transaction Completed... Transaction Id : " + response.getTransactionId());
            stateMachine.transition(SimulatorState.Charging);
          } else {
            System.err.println("Transaction Failed to Start...");
            stateMachine.transition(SimulatorState.Available);
          }
          client.clearOnReceiveMessage(StartTransactionResponse.class);
          startInProgress.set(false);
        });
  }
}
