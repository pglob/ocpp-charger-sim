package com.sim_backend.websockets.observers;

import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.RemoteStartTransaction;
import com.sim_backend.websockets.messages.RemoteStartTransactionResponse;

/** Observer that handles Getting RemoteStartTransaction requests and response. */
public class RemoteStartTransactionObserver implements OnOCPPMessageListener {

  private OCPPWebSocketClient client;
  private ConfigurationRegistry configurationRegistry;
  private TransactionHandler transactionHandler;
  private ChargerStateMachine stateMachine;

  public RemoteStartTransactionObserver(
      OCPPWebSocketClient client,
      ConfigurationRegistry configurationRegistry,
      TransactionHandler transactionHandler,
      ChargerStateMachine stateMachine) {
    this.client = client;
    this.configurationRegistry = configurationRegistry;
    this.transactionHandler = transactionHandler;
    this.stateMachine = stateMachine;

    client.onReceiveMessage(RemoteStartTransaction.class, this);
  }

  /**
   * Processes incoming RemoteStartTransaction messages and handles Start Transaction.
   *
   * @param message the received OCPP message, expected to be a RemoteStartTransaction.
   * @throws ClassCastException if the message is not a RemoteStartTransaction.
   */
  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    if (!(message.getMessage() instanceof RemoteStartTransaction request)) {
      throw new ClassCastException("Message is not an RemoteStartTransaction Request");
    }

    if (stateMachine.getCurrentState() != ChargerState.Available) {
      System.out.println(
          "Invalid State Detected... Current State: " + stateMachine.getCurrentState());
    } else {

      if (configurationRegistry.isAuthorizeRemoteTxRequests()) {

        System.out.println(
            "RemoteStartTransaction Authorization Required, Sending Authorization Request...");
        transactionHandler.startCharging(request.getConnectorId(), request.getIdTag());
      } else {
        System.out.println("RemoteStartTransaction Request Received... Starting Transaction...");
        stateMachine.checkAndTransition(ChargerState.Available, ChargerState.Preparing);
        stateMachine.checkAndTransition(ChargerState.Preparing, ChargerState.Charging);
      }
    }

    // Send response
    RemoteStartTransactionResponse response =
        new RemoteStartTransactionResponse(request, "Accepted");
    client.pushMessage(response);
  }
}
