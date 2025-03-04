package com.sim_backend.websockets.observers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sim_backend.charger.Charger;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AvailabilityStatus;
import com.sim_backend.websockets.enums.AvailabilityType;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.ChangeAvailability;
import com.sim_backend.websockets.messages.ChangeAvailabilityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChangeAvailabilityObserverTest {

  @Mock private OCPPWebSocketClient webSocketClient;
  @Mock private Charger charger;
  @Mock private ChargerStateMachine chargerStateMachine;

  private ChangeAvailabilityObserver observer;

  @BeforeEach
  void setUp() {
    when(charger.getStateMachine()).thenReturn(chargerStateMachine);
    observer = new ChangeAvailabilityObserver(webSocketClient, charger);
  }

  @Test
  void testChangeToUnavailable() {
    when(chargerStateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    when(chargerStateMachine.isBooted()).thenReturn(true);
    when(chargerStateMachine.inTransaction()).thenReturn(false);
    doReturn(true)
        .when(chargerStateMachine)
        .checkAndTransition(ChargerState.Available, ChargerState.Unavailable);

    ChangeAvailability request = new ChangeAvailability(0, AvailabilityType.INOPERATIVE);
    observer.onMessageReceived(new OnOCPPMessage(request, webSocketClient));

    verify(chargerStateMachine, times(1))
        .checkAndTransition(ChargerState.Available, ChargerState.Unavailable);
    verify(charger, times(1)).setAvailable(false);
    verify(webSocketClient, times(1))
        .pushMessage(new ChangeAvailabilityResponse(request, AvailabilityStatus.ACCEPTED));
  }

  @Test
  void testChangeToAvailable() {
    when(chargerStateMachine.getCurrentState()).thenReturn(ChargerState.Unavailable);
    when(chargerStateMachine.isBooted()).thenReturn(true);
    when(chargerStateMachine.inTransaction()).thenReturn(false);
    doReturn(true)
        .when(chargerStateMachine)
        .checkAndTransition(ChargerState.Unavailable, ChargerState.Available);

    ChangeAvailability request = new ChangeAvailability(0, AvailabilityType.OPERATIVE);
    observer.onMessageReceived(new OnOCPPMessage(request, webSocketClient));

    verify(chargerStateMachine, times(1))
        .checkAndTransition(ChargerState.Unavailable, ChargerState.Available);
    verify(charger, times(1)).setAvailable(true);
    verify(webSocketClient, times(1))
        .pushMessage(new ChangeAvailabilityResponse(request, AvailabilityStatus.ACCEPTED));
  }

  @Test
  void testChangeSameState() {
    when(chargerStateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    when(chargerStateMachine.isBooted()).thenReturn(true);

    ChangeAvailability request = new ChangeAvailability(0, AvailabilityType.OPERATIVE);
    observer.onMessageReceived(new OnOCPPMessage(request, webSocketClient));

    verify(chargerStateMachine, times(0)).checkAndTransition(any(), any());
    verify(charger, times(0)).setAvailable(true);
    verify(webSocketClient, times(1))
        .pushMessage(new ChangeAvailabilityResponse(request, AvailabilityStatus.ACCEPTED));
  }

  @Test
  void testBadState() {
    assertFalse(observer.changeAvailability(ChargerState.Available));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> observer.changeAvailability(ChargerState.PoweredOff));

    assertEquals("Expected Available or Unavailable, got PoweredOff", exception.getMessage());
  }

  @Test
  void testNotBooted() {
    when(chargerStateMachine.getCurrentState()).thenReturn(ChargerState.PoweredOff);
    when(chargerStateMachine.isBooted()).thenReturn(false);

    ChangeAvailability request = new ChangeAvailability(0, AvailabilityType.OPERATIVE);
    observer.onMessageReceived(new OnOCPPMessage(request, webSocketClient));

    verify(chargerStateMachine, times(0))
        .checkAndTransition(ChargerState.Unavailable, ChargerState.Available);
    verify(charger, times(0)).setAvailable(true);
    verify(webSocketClient, times(1))
        .pushMessage(new ChangeAvailabilityResponse(request, AvailabilityStatus.REJECTED));
  }

  @Test
  void testChangeScheduled() {
    when(chargerStateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    when(chargerStateMachine.isBooted()).thenReturn(true);
    when(chargerStateMachine.inTransaction()).thenReturn(true);
    doReturn(true)
        .when(chargerStateMachine)
        .checkAndTransition(ChargerState.Available, ChargerState.Unavailable);

    ChangeAvailability request = new ChangeAvailability(0, AvailabilityType.INOPERATIVE);
    observer.onMessageReceived(new OnOCPPMessage(request, webSocketClient));

    verify(chargerStateMachine, times(0))
        .checkAndTransition(ChargerState.Available, ChargerState.Unavailable);
    verify(charger, times(0)).setAvailable(false);

    observer.onStateChanged(ChargerState.Available);

    verify(chargerStateMachine, times(1))
        .checkAndTransition(ChargerState.Available, ChargerState.Unavailable);
    verify(charger, times(1)).setAvailable(false);
    verify(webSocketClient, times(1))
        .pushMessage(new ChangeAvailabilityResponse(request, AvailabilityStatus.SCHEDULED));
  }

  @Test
  void testAvailabilityOnReboot() {
    when(chargerStateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    when(chargerStateMachine.isBooted()).thenReturn(true);
    when(chargerStateMachine.inTransaction()).thenReturn(false);
    doReturn(true)
        .when(chargerStateMachine)
        .checkAndTransition(ChargerState.Available, ChargerState.Unavailable);

    ChangeAvailability request = new ChangeAvailability(0, AvailabilityType.INOPERATIVE);
    observer.onMessageReceived(new OnOCPPMessage(request, webSocketClient));

    observer.onStateChanged(ChargerState.BootingUp);
    assertEquals(observer.getWantedState(), ChargerState.Unavailable);

    observer.onStateChanged(ChargerState.Available);
    verify(chargerStateMachine, times(2))
        .checkAndTransition(ChargerState.Available, ChargerState.Unavailable);
    verify(charger, times(2)).setAvailable(false);
  }

  @Test
  void testLatestRequestOverridesPending() {
    when(chargerStateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    when(chargerStateMachine.isBooted()).thenReturn(true);
    when(chargerStateMachine.inTransaction()).thenReturn(true);

    observer.onMessageReceived(
        new OnOCPPMessage(
            new ChangeAvailability(0, AvailabilityType.INOPERATIVE), webSocketClient));
    assertEquals(observer.getWantedState(), ChargerState.Unavailable);
    observer.onMessageReceived(
        new OnOCPPMessage(new ChangeAvailability(1, AvailabilityType.OPERATIVE), webSocketClient));
    assertEquals(observer.getWantedState(), ChargerState.Available);
  }
}
