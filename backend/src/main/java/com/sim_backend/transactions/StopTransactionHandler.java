package com.sim_backend.transactions;

import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ReadingContext;
import com.sim_backend.websockets.enums.Reason;
import com.sim_backend.websockets.messages.StopTransaction;
import com.sim_backend.websockets.observers.MeterValuesObserver;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

/*
 * This class handles charger status followed by a StopTransaction message.
 *
 */
@Getter
public class StopTransactionHandler {
  private ChargerStateMachine stateMachine;
  private OCPPWebSocketClient client;
  private MeterValuesObserver meter;

  // Constructor
  public StopTransactionHandler(
      ChargerStateMachine stateMachine, OCPPWebSocketClient client, MeterValuesObserver meter) {
    this.stateMachine = stateMachine;
    this.client = client;
    this.meter = meter;
  }

  /**
   * Initiate StopTransaction, handles StopTransaction requests, responses and charger status.
   *
   * @param idTag id of user.
   * @param reason the reason for stopping the transaction.
   * @param transactionId transactionId from StartTransaction.
   * @param elec ElectricalTransition for retrieving meter values.
   * @param stopInProgress AtomicBoolean to prevent initiateStopTransaction from running more than
   *     once.
   */
  public void initiateStopTransaction(
      String idTag,
      Reason reason,
      AtomicInteger transactionId,
      ElectricalTransition elec,
      AtomicBoolean stopInProgress) {

    // Convert from KWh to Wh
    int meterStop = (int) (elec.getEnergyActiveImportRegister() * 1000.0f);

    OCPPTime ocppTime = client.getScheduler().getTime();
    ZonedDateTime zonetime = ocppTime.getSynchronizedTime();
    String timestamp = zonetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"));

    StopTransaction stopTransactionMessage;

    if (!stateMachine.checkAndTransition(ChargerState.Charging, ChargerState.Available)) {
      return;
    }

    if (reason != null && idTag == null) {
      stopTransactionMessage =
          new StopTransaction(transactionId.get(), meterStop, timestamp, reason);
    } else if (reason == null && idTag != null) {
      stopTransactionMessage =
          new StopTransaction(idTag, transactionId.get(), meterStop, timestamp);
    } else {
      stopTransactionMessage =
          new StopTransaction(idTag, transactionId.get(), meterStop, timestamp, reason);
    }
    client.pushMessage(stopTransactionMessage);
    meter.sendMeterValues(ReadingContext.TRANSACTION_END);

    // No listener is used here since a Central System cannot prevent a transaction from stopping
    System.out.println("Stop Transaction Completed...");
    transactionId.set(-1);
    stopInProgress.set(false);
  }
}
