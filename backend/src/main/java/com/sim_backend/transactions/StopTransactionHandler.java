package com.sim_backend.transactions;

import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.messages.StopTransaction;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;

/*
 * This class handles charger status followed by a StopTransaction message.
 *
 */
@Getter
public class StopTransactionHandler {
  private ChargerStateMachine stateMachine;
  private OCPPWebSocketClient client;

  // Constructor
  public StopTransactionHandler(ChargerStateMachine stateMachine, OCPPWebSocketClient client) {
    this.stateMachine = stateMachine;
    this.client = client;
  }

  /**
   * Initiate StopTransaction, handles StopTransaction requests, responses and charger status.
   *
   * @param transactionId transactionId from StartTransaction
   * @param idTag id of user
   * @param elec ElectricalTransition for retrieving meter values
   * @param stopInProgress AtomicBoolean to prevent initiateStopTransaction from running more than
   *     once
   */
  public void initiateStopTransaction(
      int transactionId, String idTag, ElectricalTransition elec, AtomicBoolean stopInProgress) {

    // Convert from KWh to Wh
    int meterStop = (int) (elec.getEnergyActiveImportRegister() * 1000.0f);

    OCPPTime ocppTime = client.getScheduler().getTime();
    ZonedDateTime zonetime = ocppTime.getSynchronizedTime();
    String timestamp = zonetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"));

    StopTransaction stopTransactionMessage =
        new StopTransaction(idTag, transactionId, meterStop, timestamp);
    client.pushMessage(stopTransactionMessage);

    // No listener is used here since a Central System cannot prevent a transaction from stopping
    System.out.println("Stop Transaction Completed...");
    stateMachine.transition(ChargerState.Available);
  }
}
