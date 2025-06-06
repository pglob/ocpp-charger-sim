package com.sim_backend.websockets.observers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.charger.Charger;
import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.electrical.ChargingProfileHandler;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.transactions.StartTransactionHandler;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.RemoteStartStopStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.RemoteStartTransaction;
import com.sim_backend.websockets.messages.RemoteStartTransactionResponse;
import com.sim_backend.websockets.types.ChargingProfile;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RemoteStartTransactionObserverTest {

  @Mock private OCPPWebSocketClient client;
  @Mock private Charger charger;
  @Mock private TransactionHandler transactionHandler;
  @Mock private StartTransactionHandler startTransactionHandler;
  @Mock private ChargerStateMachine stateMachine;
  @Mock private ConfigurationRegistry config;
  @Mock private ElectricalTransition elec;
  @Mock private ChargingProfileHandler profileHandler;

  private RemoteStartTransactionObserver observer;
  AtomicBoolean progress;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    progress = new AtomicBoolean(false);
    when(transactionHandler.getStartInProgress()).thenReturn(progress);
    when(transactionHandler.getStartHandler()).thenReturn(startTransactionHandler);
    when(transactionHandler.getElec()).thenReturn(elec);
    when(transactionHandler.getElec().getChargingProfileHandler()).thenReturn(profileHandler);
    when(charger.getConfig()).thenReturn(config);
    when(charger.getStateMachine()).thenReturn(stateMachine);
    when(charger.getTransactionHandler()).thenReturn(transactionHandler);
    observer =
        new RemoteStartTransactionObserver(
            client,
            charger.getConfig(),
            charger.getTransactionHandler(),
            charger.getStateMachine());
  }

  @Test
  void testRemoteStartTransactionWithAuthorization() {
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    when(config.isAuthorizeRemoteTxRequests()).thenReturn(true);
    RemoteStartTransaction request =
        new RemoteStartTransaction(
            "testIdTag", 1, new ChargingProfile(0, null, 0, null, null, null, null, null, null));
    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(request);

    observer.onMessageReceived(message);

    verify(transactionHandler, times(1)).startCharging(eq(1), eq("testIdTag"));

    ArgumentCaptor<RemoteStartTransactionResponse> responseCaptor =
        ArgumentCaptor.forClass(RemoteStartTransactionResponse.class);
    verify(client, times(1)).pushMessage(responseCaptor.capture());

    RemoteStartTransactionResponse response = responseCaptor.getValue();
    assert response.getStatus() == RemoteStartStopStatus.ACCEPTED;
  }

  @Test
  void testRemoteStartTransactionWithoutAuthorization() {
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    when(config.isAuthorizeRemoteTxRequests()).thenReturn(false);
    RemoteStartTransaction request =
        new RemoteStartTransaction(
            "testIdTag", 1, new ChargingProfile(0, null, 0, null, null, null, null, null, null));
    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(request);

    observer.onMessageReceived(message);

    ArgumentCaptor<RemoteStartTransactionResponse> responseCaptor =
        ArgumentCaptor.forClass(RemoteStartTransactionResponse.class);
    verify(client, times(1)).pushMessage(responseCaptor.capture());

    RemoteStartTransactionResponse response = responseCaptor.getValue();
    assert response.getStatus() == RemoteStartStopStatus.ACCEPTED;
  }
}
