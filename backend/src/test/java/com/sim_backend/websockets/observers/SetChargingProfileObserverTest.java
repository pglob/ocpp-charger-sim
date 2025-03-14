package com.sim_backend.websockets.observers;

import static org.mockito.Mockito.*;

import com.sim_backend.electrical.ChargingProfileHandler;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ChargingProfileStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.SetChargingProfile;
import com.sim_backend.websockets.messages.SetChargingProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SetChargingProfileObserverTest {

  private ChargingProfileHandler mockChargingProfileHandler;
  private ElectricalTransition mockElectricalTransition;
  private OCPPWebSocketClient mockClient;
  private SetChargingProfileObserver observer;

  @BeforeEach
  void setUp() {
    mockChargingProfileHandler = mock(ChargingProfileHandler.class);
    mockElectricalTransition = mock(ElectricalTransition.class);
    mockClient = mock(OCPPWebSocketClient.class);

    when(mockElectricalTransition.getChargingProfileHandler())
        .thenReturn(mockChargingProfileHandler);

    observer =
        new SetChargingProfileObserver(
            mockChargingProfileHandler, mockElectricalTransition, mockClient);
  }

  @Test
  void testOnMessageReceived_SuccessfulProcessing() {
    SetChargingProfile mockRequest = mock(SetChargingProfile.class);
    OnOCPPMessage mockMessage = mock(OnOCPPMessage.class);
    when(mockMessage.getMessage()).thenReturn(mockRequest);

    observer.onMessageReceived(mockMessage);

    verify(mockChargingProfileHandler).addChargingProfile(mockRequest.getCsChargingProfiles());

    ArgumentCaptor<SetChargingProfileResponse> responseCaptor =
        ArgumentCaptor.forClass(SetChargingProfileResponse.class);
    verify(mockClient).pushMessage(responseCaptor.capture());

    SetChargingProfileResponse response = responseCaptor.getValue();
    assert response.getStatus() == ChargingProfileStatus.ACCEPTED;
  }

  @Test
  void testOnMessageReceived_InvalidMessage_ThrowsClassCastException() {
    OnOCPPMessage mockMessage = mock(OnOCPPMessage.class);

    try {
      observer.onMessageReceived(mockMessage);
    } catch (ClassCastException e) {
      assert e.getMessage().equals("Message is not a SetChargingProfile");
    }
  }

  @Test
  void testOnMessageReceived_NullChargingProfileHandler_ThrowsNullPointerException() {
    when(mockElectricalTransition.getChargingProfileHandler()).thenReturn(null);
    SetChargingProfile mockRequest = mock(SetChargingProfile.class);
    OnOCPPMessage mockMessage = mock(OnOCPPMessage.class);
    when(mockMessage.getMessage()).thenReturn(mockRequest);

    try {
      observer.onMessageReceived(mockMessage);
    } catch (NullPointerException e) {
      assert e.getMessage().equals("ChargingProfileHandler is null");
    }
  }
}
