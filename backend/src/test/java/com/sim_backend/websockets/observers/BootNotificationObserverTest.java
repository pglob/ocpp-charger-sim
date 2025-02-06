package com.sim_backend.websockets.observers;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.state.IllegalStateException;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.RegistrationStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.BootNotification;
import com.sim_backend.websockets.messages.BootNotificationResponse;
import com.sim_backend.websockets.types.OCPPMessage;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BootNotificationObserverTest {

  @Mock private OCPPWebSocketClient webSocketClient;
  @Mock private SimulatorStateMachine stateMachine;

  private BootNotificationObserver observer;

  private static BootNotification isBootNotification() {
    return argThat(argument -> argument instanceof BootNotification);
  }

  @BeforeEach
  void setUp() {
    observer = new BootNotificationObserver(webSocketClient, stateMachine);
  }

  @Test
  void handleBootNotificationRequest_WhenBooting_SendsNotification() {
    // Arrange
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.BootingUp);

    // Act
    observer.handleBootNotificationRequest();

    // Assert
    verify(webSocketClient).pushMessage(isBootNotification());
  }

  @Test
  void handleBootNotificationRequest_WhenNotBooting_ThrowsException() {
    // Arrange
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.Available);

    // Act & Assert
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalStateException.class, () -> observer.handleBootNotificationRequest());
  }

  @Test
  void onMessageReceived_WhenStatusAccepted_SetsHeartbeatAndTransitionsState() {
    // Arrange
    BootNotificationResponse response =
        Mockito.spy(
            new BootNotificationResponse(RegistrationStatus.ACCEPTED, ZonedDateTime.now(), 20));

    MessageScheduler messageScheduler = mock(MessageScheduler.class);
    when(webSocketClient.getScheduler()).thenReturn(messageScheduler);

    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(response);
    when(message.getClient()).thenReturn(webSocketClient);

    // Act
    observer.onMessageReceived(message);

    // Assert
    verify(messageScheduler).setHeartbeatInterval(20L, TimeUnit.SECONDS);
    verify(stateMachine).transition(SimulatorState.Available);
  }

  @Test
  void onMessageReceived_WhenStatusPending_RegistersJob() {
    // Arrange
    BootNotificationResponse response =
        Mockito.spy(
            new BootNotificationResponse(RegistrationStatus.PENDING, ZonedDateTime.now(), 20));

    MessageScheduler messageScheduler = mock(MessageScheduler.class);
    when(webSocketClient.getScheduler()).thenReturn(messageScheduler);

    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(response);
    when(message.getClient()).thenReturn(webSocketClient);

    // Act
    observer.onMessageReceived(message);

    // Assert
    verify(messageScheduler).registerJob(eq(20L), eq(TimeUnit.SECONDS), isBootNotification());
  }

  @Test
  void onMessageReceived_WhenStatusRejected_RegistersJob() {
    // Arrange
    // Arrange
    BootNotificationResponse response =
        Mockito.spy(
            new BootNotificationResponse(RegistrationStatus.REJECTED, ZonedDateTime.now(), 240));

    MessageScheduler messageScheduler = mock(MessageScheduler.class);
    when(webSocketClient.getScheduler()).thenReturn(messageScheduler);

    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(response);
    when(message.getClient()).thenReturn(webSocketClient);

    // Act
    observer.onMessageReceived(message);

    // Assert
    verify(messageScheduler)
        .registerJob(
            eq(MessageScheduler.getHEARTBEAT_INTERVAL()),
            eq(TimeUnit.SECONDS),
            isBootNotification());
  }

  @Test
  void onMessageReceived_WhenInvalidMessageType_ThrowsException() {
    // Arrange
    OCPPMessage invalidMessage = mock(OCPPMessage.class);
    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(invalidMessage);

    // Act & Assert
    org.junit.jupiter.api.Assertions.assertThrows(
        ClassCastException.class, () -> observer.onMessageReceived(message));
  }

  @Test
  void onStateChanged_WhenBootingUp_SendsNotification() {
    // Arrange
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.BootingUp);

    // Act
    observer.onStateChanged(SimulatorState.BootingUp);

    // Assert
    verify(webSocketClient).pushMessage(isBootNotification());
  }

  @Test
  void onStateChanged_WhenNotBootingUp_DoesNotSendNotification() {
    // Act
    observer.onStateChanged(SimulatorState.Available);

    // Assert
    verify(webSocketClient, never()).pushMessage(any());
  }
}
