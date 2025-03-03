package com.sim_backend.websockets.observers;

import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.state.StateObserver;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AvailabilityStatus;
import com.sim_backend.websockets.enums.AvailabilityType;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.ChangeAvailability;
import com.sim_backend.websockets.messages.ChangeAvailabilityResponse;
import com.sim_backend.websockets.messages.StatusNotification;

public class ChangeAvailabilityObserver implements OnOCPPMessageListener, StateObserver {
  private final OCPPWebSocketClient client;
  private final ChargerStateMachine stateMachine;
  private int wantedConnectorId;
  private ChargerState wantedState = null;

  public ChangeAvailabilityObserver(OCPPWebSocketClient client, ChargerStateMachine state) {
    this.client = client;
    this.stateMachine = state;
    client.onReceiveMessage(ChangeAvailability.class, this);
    stateMachine.addObserver(this);
  }

  private void changeAvailability(int connectorId, ChargerState newState) {
    ChargePointStatus chargePointStatus =
        newState == ChargerState.Unavailable
            ? ChargePointStatus.Unavailable
            : ChargePointStatus.Available;

    StatusNotification statusNotification =
        new StatusNotification(
            connectorId,
            ChargePointErrorCode.NoError,
            "Availability changed",
            chargePointStatus,
            null,
            "",
            "");
    client.pushMessage(statusNotification);
    stateMachine.transition(newState);
  }

  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    ChangeAvailability availability = (ChangeAvailability) message.getMessage();
    ChargerState newState =
        availability.getType() == AvailabilityType.INOPERATIVE
            ? ChargerState.Unavailable
            : ChargerState.Available;

    AvailabilityStatus responseStatus = AvailabilityStatus.REJECTED;

    if (stateMachine.getCurrentState() == newState) {
      responseStatus = AvailabilityStatus.ACCEPTED;
    } else if (stateMachine.inTransaction()
        && newState == ChargerState.Unavailable) {
      responseStatus = AvailabilityStatus.SCHEDULED;
      wantedConnectorId = availability.getConnectorID();
      wantedState = newState;
    } else {
      responseStatus = AvailabilityStatus.ACCEPTED;
      changeAvailability(availability.getConnectorID(), newState);
    }

    client.pushMessage(new ChangeAvailabilityResponse(availability, responseStatus));
  }

  @Override
  public void onStateChanged(ChargerState newState) {
    if (newState == ChargerState.Available && wantedState != null) {
      changeAvailability(wantedConnectorId, wantedState);
      wantedState = null;
    }
  }
}
