package com.sim_backend.websockets.observers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.messages.StatusNotification;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StatusNotificationObserverTest {

  private OCPPWebSocketClient mockClient;
  private StatusNotificationObserver observer;

  @BeforeEach
  void setUp() {
    mockClient = mock(OCPPWebSocketClient.class);
    observer = new StatusNotificationObserver(mockClient);
  }

  @Test
  void testOffline() {
    when(mockClient.isOnline()).thenReturn(false);
    observer.sendStatusNotification(
        1,
        ChargePointErrorCode.NoError,
        "",
        ChargePointStatus.Available,
        ZonedDateTime.now(),
        "",
        "");

    verify(mockClient, never()).pushMessage(any(StatusNotification.class));
  }

  @Test
  void testOfflineToOnline() {
    when(mockClient.isOnline()).thenReturn(false);

    observer.sendStatusNotification(
        1,
        ChargePointErrorCode.NoError,
        "",
        ChargePointStatus.Charging,
        ZonedDateTime.now(),
        "",
        "");

    verify(mockClient, never()).pushMessage(any(StatusNotification.class));

    when(mockClient.isOnline()).thenReturn(true);
    observer.onClientGoOnline();

    ArgumentCaptor<StatusNotification> captor = ArgumentCaptor.forClass(StatusNotification.class);
    verify(mockClient, times(1)).pushMessage(captor.capture());
    StatusNotification message = captor.getValue();
    System.out.println(message);
  }
}
