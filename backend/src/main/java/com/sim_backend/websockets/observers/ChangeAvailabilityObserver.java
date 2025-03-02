package com.sim_backend.websockets.observers;

import com.sim_backend.state.ChargerAvailabilityState;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AvailabilityStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.ChangeAvailability;
import com.sim_backend.websockets.messages.ChangeAvailabilityResponse;

public class ChangeAvailabilityObserver implements OnOCPPMessageListener {
  private final OCPPWebSocketClient client;
  private final ChargerAvailabilityState state;

  public ChangeAvailabilityObserver(OCPPWebSocketClient client, ChargerAvailabilityState state) {
    this.client = client;
    this.state = state;
    client.onReceiveMessage(ChangeAvailability.class, this);
  }

  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    ChangeAvailability availability = (ChangeAvailability) message.getMessage();

    state.changeAvailability(availability.getConnectorID(), availability.getType());

    ChangeAvailabilityResponse response =
        new ChangeAvailabilityResponse(availability, AvailabilityStatus.ACCEPTED);
    client.pushMessage(response);
  }
}
