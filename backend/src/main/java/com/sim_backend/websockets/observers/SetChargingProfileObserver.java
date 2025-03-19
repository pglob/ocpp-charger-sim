package com.sim_backend.websockets.observers;

import com.sim_backend.electrical.ChargingProfileHandler;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ChargingProfileStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.SetChargingProfile;
import com.sim_backend.websockets.messages.SetChargingProfileResponse;

/** Observer class that listens for SetChargingProfile messages and processes them accordingly. */
public class SetChargingProfileObserver implements OnOCPPMessageListener {

  private final ElectricalTransition electricalTransition;
  private final OCPPWebSocketClient client;

  public SetChargingProfileObserver(
      ChargingProfileHandler chargingProfileHandler,
      ElectricalTransition electricalTransition,
      OCPPWebSocketClient client) {
    this.electricalTransition = electricalTransition;
    electricalTransition.setChargingProfileHandler(chargingProfileHandler);
    this.client = client;
    this.client.onReceiveMessage(SetChargingProfile.class, this);
  }

  /**
   * Handles incoming OCPP messages and processes SetChargingProfile requests.
   *
   * @param message The received OCPP message.
   * @throws ClassCastException if the message is not an instance of SetChargingProfile.
   * @throws NullPointerException if the ChargingProfileHandler is not set.
   */
  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    if (!(message.getMessage() instanceof SetChargingProfile request))
      throw new ClassCastException("Message is not a SetChargingProfile");

    if (electricalTransition.getChargingProfileHandler() == null)
      throw new NullPointerException("ChargingProfileHandler is null");

    if (!electricalTransition
        .getChargingProfileHandler()
        .addChargingProfile(request.getCsChargingProfiles())) {
      client.pushMessage(new SetChargingProfileResponse(request, ChargingProfileStatus.REJECTED));
    }

    client.pushMessage(new SetChargingProfileResponse(request, ChargingProfileStatus.ACCEPTED));
  }
}
