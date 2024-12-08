package com.sim_backend.websockets.events;

import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.messages.AuthorizeResponse;

public class OCPPMessageHandler implements OnOCPPMessageListener {

  private void handleAuthorize(AuthorizeResponse response) {
    AuthorizationStatus status = response.getIdTagInfo().getStatus();

    if (status == AuthorizationStatus.ACCEPTED) {
      System.out.println("Authorize Accepted");
    } else {
      System.out.println("Authorize Denied");
    }
  }

  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    System.out.println("Received OCPP Message: : ");
  }

  //@Override
  public void onAuthorizeReceived(AuthorizeResponse response) {
    System.out.println("Received Authorize Response");
    handleAuthorize(response);
  }
}
