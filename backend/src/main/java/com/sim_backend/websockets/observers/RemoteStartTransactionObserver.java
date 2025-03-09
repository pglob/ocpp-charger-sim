package com.sim_backend.websockets.observers;

import com.sim_backend.charger.Charger;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.RemoteStartTransaction;
import com.sim_backend.websockets.messages.RemoteStartTransactionResponse;

/** Observer that handles Getting RemoteStartTransaction requests and response. */
public class RemoteStartTransactionObserver implements OnOCPPMessageListener {

  private OCPPWebSocketClient client;
  private Charger charger;

  public RemoteStartTransactionObserver(OCPPWebSocketClient client, Charger charger) {
    this.client = client;
    this.charger = charger;

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
    if (charger.getConfig().isAuthorizeRemoteTxRequests()) {
      System.out.println(
          "RemoteStartTransaction Authorization Required, Sending Authorization Request...");
      charger
          .getTransactionHandler()
          .preAuthorize(request.getConnectorId(), request.getIdTag(), null);
      charger.getConfig().setAuthorizeRemoteTxRequests(false);
    } else {
      System.out.println("RemoteStartTransaction Request Received... Starting Transaction...");
      charger.getTransactionHandler().startCharging(request.getConnectorId(), request.getIdTag());
    }

    // Send response
    RemoteStartTransactionResponse response =
        new RemoteStartTransactionResponse(request, "Accepted");
    client.pushMessage(response);
  }
}
