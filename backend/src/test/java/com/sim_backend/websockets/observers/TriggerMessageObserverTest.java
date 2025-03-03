package com.sim_backend.websockets.observers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.MessageTrigger;
import com.sim_backend.websockets.enums.TriggerMessageStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.StatusNotification;
import com.sim_backend.websockets.messages.TriggerMessage;
import com.sim_backend.websockets.messages.TriggerMessageResponse;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TriggerMessageObserverTest {

  @Mock private OCPPWebSocketClient webSocketClient;
  @Mock private ChargerStateMachine stateMachine;
  @Mock private OnOCPPMessage onOCPPMessage;
  private TriggerMessageObserver observer;

  @BeforeEach
  void setUp() {
    observer = new TriggerMessageObserver(webSocketClient, stateMachine);
  }

  private TriggerMessage createTriggerMessage(MessageTrigger trigger, Integer connectorId) {
    return connectorId == null
        ? new TriggerMessage(trigger)
        : new TriggerMessage(trigger, connectorId);
  }

  private TriggerMessageResponse captureTriggerMessageResponse() {
    ArgumentCaptor<TriggerMessageResponse> captor =
        ArgumentCaptor.forClass(TriggerMessageResponse.class);
    verify(webSocketClient, atLeastOnce()).pushMessage(captor.capture());
    return captor.getValue();
  }

  @Test
  void testBootNotification_BootingUp() {
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.BootingUp);

    TriggerMessage triggerMsg = createTriggerMessage(MessageTrigger.BootNotification, null);

    when(onOCPPMessage.getMessage()).thenReturn(triggerMsg);
    when(onOCPPMessage.getClient()).thenReturn(webSocketClient);

    observer.onMessageReceived(onOCPPMessage);

    TriggerMessageResponse response = captureTriggerMessageResponse();
    assert response.getStatus() == TriggerMessageStatus.Accepted;

    verify(webSocketClient, atLeastOnce())
        .pushMessage(
            argThat(msg -> msg instanceof com.sim_backend.websockets.messages.BootNotification));
  }

  @Test
  void testDiagnosticsStatusNotification() {
    TriggerMessage triggerMsg =
        createTriggerMessage(MessageTrigger.DiagnosticsStatusNotification, null);
    when(onOCPPMessage.getMessage()).thenReturn(triggerMsg);
    when(onOCPPMessage.getClient()).thenReturn(webSocketClient);

    observer.onMessageReceived(onOCPPMessage);

    TriggerMessageResponse response = captureTriggerMessageResponse();
    assert response.getStatus() == TriggerMessageStatus.NotImplemented;

    verify(webSocketClient, times(1)).pushMessage(any(TriggerMessageResponse.class));
    verify(webSocketClient, never())
        .pushMessage(argThat(msg -> !(msg instanceof TriggerMessageResponse)));
  }

  @Test
  void testFirmwareStatusNotification() {
    TriggerMessage triggerMsg =
        createTriggerMessage(MessageTrigger.FirmwareStatusNotification, null);
    when(onOCPPMessage.getMessage()).thenReturn(triggerMsg);
    when(onOCPPMessage.getClient()).thenReturn(webSocketClient);

    observer.onMessageReceived(onOCPPMessage);

    TriggerMessageResponse response = captureTriggerMessageResponse();
    assert response.getStatus() == TriggerMessageStatus.NotImplemented;
  }

  @Test
  void testHeartbeat() {
    TriggerMessage triggerMsg = createTriggerMessage(MessageTrigger.Heartbeat, null);
    when(onOCPPMessage.getMessage()).thenReturn(triggerMsg);
    when(onOCPPMessage.getClient()).thenReturn(webSocketClient);

    observer.onMessageReceived(onOCPPMessage);

    TriggerMessageResponse response = captureTriggerMessageResponse();
    assert response.getStatus() == TriggerMessageStatus.Accepted;

    verify(webSocketClient).pushMessage(argThat(msg -> msg instanceof Heartbeat));
  }

  @Test
  void testMeterValues() {
    TriggerMessage triggerMsg = createTriggerMessage(MessageTrigger.MeterValues, null);
    when(onOCPPMessage.getMessage()).thenReturn(triggerMsg);
    when(onOCPPMessage.getClient()).thenReturn(webSocketClient);

    observer.onMessageReceived(onOCPPMessage);

    TriggerMessageResponse response = captureTriggerMessageResponse();
    assert response.getStatus() == TriggerMessageStatus.NotImplemented;
    verify(webSocketClient, times(1)).pushMessage(any(TriggerMessageResponse.class));
  }

  @Test
  void testStatusNotification() {
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    MessageScheduler mockScheduler = mock(MessageScheduler.class);
    OCPPTime mockTime = mock(OCPPTime.class);
    when(webSocketClient.getScheduler()).thenReturn(mockScheduler);
    when(mockScheduler.getTime()).thenReturn(mockTime);
    when(mockTime.getSynchronizedTime()).thenReturn(ZonedDateTime.now());
    TriggerMessage triggerMsg = createTriggerMessage(MessageTrigger.StatusNotification, 1);

    when(onOCPPMessage.getMessage()).thenReturn(triggerMsg);
    when(onOCPPMessage.getClient()).thenReturn(webSocketClient);

    observer.onMessageReceived(onOCPPMessage);
    TriggerMessageResponse response = captureTriggerMessageResponse();
    assert response.getStatus() == TriggerMessageStatus.Accepted;
    verify(webSocketClient).pushMessage(argThat(msg -> msg instanceof StatusNotification));
  }
}
