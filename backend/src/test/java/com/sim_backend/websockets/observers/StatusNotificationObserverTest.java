package com.sim_backend.websockets.observers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.state.ChargerState;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.StatusNotification;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class StatusNotificationObserverTest {

  @Mock private OCPPWebSocketClient client;
  @Mock private OCPPTime ocppTime;
  @Mock private MessageScheduler scheduler;
  private StatusNotificationObserver observer;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    MessageScheduler scheduler = mock(MessageScheduler.class);
    OCPPTime ocppTime = mock(OCPPTime.class);
    when(client.getScheduler()).thenReturn(scheduler);
    when(scheduler.getTime()).thenReturn(ocppTime);
    when(ocppTime.getSynchronizedTime()).thenReturn(ZonedDateTime.parse("2025-02-26T00:00:00Z"));
    observer = new StatusNotificationObserver(client);
  }

  @Test
  void testOffline() {
    when(client.isOnline()).thenReturn(false);
    observer.sendStatusNotification(
        1,
        ChargePointErrorCode.NoError,
        "",
        ChargePointStatus.Available,
        ZonedDateTime.now(),
        "",
        "");

    verify(client, never()).pushMessage(any(StatusNotification.class));
  }

  @Test
  void testOfflineToOnline() {
    when(client.isOnline()).thenReturn(false);

    observer.sendStatusNotification(
        1,
        ChargePointErrorCode.NoError,
        "",
        ChargePointStatus.Charging,
        ZonedDateTime.now(),
        "",
        "");

    verify(client, never()).pushMessage(any(StatusNotification.class));

    when(client.isOnline()).thenReturn(true);
    observer.onClientGoOnline();

    ArgumentCaptor<StatusNotification> captor = ArgumentCaptor.forClass(StatusNotification.class);
    verify(client, times(1)).pushMessage(captor.capture());
  }

  @Test
  void testOnStateChangeOfflineToOnline() {
    when(client.isOnline()).thenReturn(false);

    StatusNotification statusNotification =
        new StatusNotification(
            1,
            ChargePointErrorCode.NoError,
            "",
            ChargePointStatus.Available,
            ZonedDateTime.now(),
            "",
            "");

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(statusNotification);
              listener.onMessageReceived(message);
              return null;
            })
        .when(client)
        .onReceiveMessage(eq(StatusNotification.class), any());

    observer.onStateChanged(ChargerState.Available);

    when(client.isOnline()).thenReturn(true);
    observer.onClientGoOnline();

    ArgumentCaptor<StatusNotification> captor = ArgumentCaptor.forClass(StatusNotification.class);
    verify(client, times(1)).pushMessage(captor.capture());
  }

  @Test
  void testOnStateChangeOnline() {
    when(client.isOnline()).thenReturn(true);

    StatusNotification statusNotification =
        new StatusNotification(
            1,
            ChargePointErrorCode.NoError,
            "",
            ChargePointStatus.Available,
            ZonedDateTime.now(),
            "",
            "");

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(statusNotification);
              listener.onMessageReceived(message);
              return null;
            })
        .when(client)
        .onReceiveMessage(eq(StatusNotification.class), any());

    observer.onStateChanged(ChargerState.Available);

    ArgumentCaptor<StatusNotification> captor = ArgumentCaptor.forClass(StatusNotification.class);
    verify(client, times(1)).pushMessage(captor.capture());

    StatusNotification message = captor.getValue();
    System.out.println(message);
  }
}
