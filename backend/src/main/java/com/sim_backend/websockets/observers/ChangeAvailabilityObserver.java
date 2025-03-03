package com.sim_backend.websockets.observers;

import com.sim_backend.charger.Charger;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.state.StateObserver;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AvailabilityStatus;
import com.sim_backend.websockets.enums.AvailabilityType;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.ChangeAvailability;
import com.sim_backend.websockets.messages.ChangeAvailabilityResponse;

/** Observer for handling incoming ChangeAvailability Messages */
public class ChangeAvailabilityObserver implements OnOCPPMessageListener, StateObserver {
  /** Our OCPPWebSocketClient */
  private final OCPPWebSocketClient client;

  /** Our charger's state machine */
  private final ChargerStateMachine stateMachine;

  /** Our charger */
  private final Charger charger;

  /** The last wanted state */
  private ChargerState wantedState = null;

  public ChangeAvailabilityObserver(OCPPWebSocketClient client, Charger charger) {
    this.client = client;
    this.stateMachine = charger.getStateMachine();
    this.charger = charger;
    client.onReceiveMessage(ChangeAvailability.class, this);
    stateMachine.addObserver(this);
  }

  /**
   * Change our availability.
   *
   * @param newState The state to change to.
   * @return True if successful.
   */
  private boolean changeAvailability(ChargerState newState) {
    ChargerState expectedState =
        newState == ChargerState.Unavailable ? ChargerState.Available : ChargerState.Unavailable;

    charger.setAvailable(newState == ChargerState.Available);
    return stateMachine.checkAndTransition(expectedState, newState);
  }

  /**
   * Handle when we receive a ChangeAvailability message.
   *
   * @param message The OCPP message event.
   */
  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    ChangeAvailability availability = (ChangeAvailability) message.getMessage();
    ChargerState newState =
        availability.getType() == AvailabilityType.INOPERATIVE
            ? ChargerState.Unavailable
            : ChargerState.Available;

    AvailabilityStatus responseStatus = AvailabilityStatus.REJECTED;
    ChargerState currentState = stateMachine.getCurrentState();

    if (stateMachine.isBooted()) {
      if (currentState == newState) {
        responseStatus = AvailabilityStatus.ACCEPTED;
      } else if (stateMachine.inTransaction()) {
        responseStatus = AvailabilityStatus.SCHEDULED;
        wantedState = newState;
      } else if (changeAvailability(newState)) {
        responseStatus = AvailabilityStatus.ACCEPTED;
      }
    }

    client.pushMessage(new ChangeAvailabilityResponse(availability, responseStatus));
  }

  /**
   * Called when the statemachine changes.
   *
   * @param newState the new state after the change.
   */
  @Override
  public void onStateChanged(ChargerState newState) {
    if (newState == ChargerState.BootingUp && !charger.isAvailable()) {
      wantedState = ChargerState.Unavailable;
    } else if (newState == ChargerState.Available && wantedState == ChargerState.Unavailable) {
      changeAvailability(wantedState);
      wantedState = null;
    }
  }
}
