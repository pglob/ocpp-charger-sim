package com.sim_backend.websockets.observers;

import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.Reason;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.RemoteStopTransaction;
import com.sim_backend.websockets.messages.RemoteStopTransactionResponse;

/** Observer that handles Getting RemoteStopTransaction requests and response. */
public class RemoteStopTransactionObserver implements OnOCPPMessageListener {

  private final OCPPWebSocketClient client;
  private final TransactionHandler transactionHandler;

  public RemoteStopTransactionObserver(
      OCPPWebSocketClient client, TransactionHandler transactionHandler) {
    this.client = client;
    this.transactionHandler = transactionHandler;

    client.onReceiveMessage(RemoteStopTransaction.class, this);
  }

  /**
   * Processes incoming RemoteStopTransaction messages and handles Stop Transaction.
   *
   * @param message the received OCPP message, expected to be a RemoteStopTransaction.
   * @throws ClassCastException if the message is not a RemoteStopTransaction.
   */
  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    if (!(message.getMessage() instanceof RemoteStopTransaction request)) {
      throw new ClassCastException("Message is not an RemoteStopTransaction Request");
    }

    String status = "Rejected";

    if (transactionHandler.getTransactionId().get() == request.getTransactionId()) {
      transactionHandler.stopCharging(null, Reason.REMOTE);
      status = "Accepted";
    }

    RemoteStopTransactionResponse response = new RemoteStopTransactionResponse(request, status);
    client.pushMessage(response);
  }
}
