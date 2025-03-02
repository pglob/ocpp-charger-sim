package com.sim_backend.websockets.observers;

import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AvailabilityStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.ChangeAvailability;
import com.sim_backend.websockets.messages.ChangeAvailabilityResponse;

public class ChangeAvailabilityObserver implements OnOCPPMessageListener {
  private final OCPPWebSocketClient client;

  public ChangeAvailabilityObserver(OCPPWebSocketClient client) {
    this.client = client;
    client.onReceiveMessage(ChangeAvailability.class, this);
  }

  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    ChangeAvailability availability = (ChangeAvailability) message.getMessage();

    ChangeAvailabilityResponse response =
        new ChangeAvailabilityResponse(availability, AvailabilityStatus.ACCEPTED);
    client.pushMessage(response);
  }
}
