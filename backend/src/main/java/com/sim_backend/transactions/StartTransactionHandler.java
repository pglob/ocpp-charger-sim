package com.sim_backend.transactions;

import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.enums.ReadingContext;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.StartTransaction;
import com.sim_backend.websockets.messages.StartTransactionResponse;
import com.sim_backend.websockets.observers.MeterValuesObserver;
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
  private ChargerStateMachine stateMachine;
  private OCPPWebSocketClient client;
  private int transactionId;
  private MeterValuesObserver meter;

  // Constructor
  public StartTransactionHandler(
      ChargerStateMachine stateMachine, OCPPWebSocketClient client, MeterValuesObserver meter) {
    this.stateMachine = stateMachine;
    this.client = client;
    this.meter = meter;
    this.transactionId = -1;
  }

  /**
   * Initiate Start Transaction Handling StartTransaction Request, Response and charger status If
   * authorization is accepted, change a stateMachine status to Charging. Switch to Available
   * otherwise.
   *
   * @param connectorId ID of connector
   * @param idTag ID of user
   * @param transactionId AtomicInteger used to pass the transactionId back to the
   *     TransactionHandler
   * @param elec ElectricalTransition for retrieving meter values
   * @param startInProgress AtomicBoolean to prevent initiateStartTransaction from running more than
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
    meter.sendMeterValues(ReadingContext.TRANSACTION_BEGIN);

    final OnOCPPMessageListener listener =
        new OnOCPPMessageListener() {
          @Override
          public void onMessageReceived(OnOCPPMessage message) {
            client.deleteOnReceiveMessage(StartTransactionResponse.class, this);
            if (!(message.getMessage() instanceof StartTransactionResponse response)) {
              throw new ClassCastException("Message is not a StartTransactionResponse");
            }
            if (response.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED) {
              transactionId.set(response.getTransactionId());
              System.out.println(
                  "Start Transaction Completed... Transaction Id : " + response.getTransactionId());
              stateMachine.checkAndTransition(ChargerState.Preparing, ChargerState.Charging);
            } else {
              System.err.println("Transaction Failed to Start...");
              stateMachine.checkAndTransition(ChargerState.Preparing, ChargerState.Available);
            }
            startInProgress.set(false);
          }

          @Override
          public void onTimeout() {
            client.deleteOnReceiveMessage(StartTransactionResponse.class, this);
            System.err.println("Start Transaction timeout. Resetting transaction state.");
            startInProgress.set(false);
            stateMachine.checkAndTransition(ChargerState.Preparing, ChargerState.Available);
          }
        };

    client.onReceiveMessage(StartTransactionResponse.class, listener);
  }
}
