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
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BootNotificationObserverTest {

  @Mock private OCPPWebSocketClient webSocketClient;

  private BootNotificationObserver observer;

  private static BootNotification isBootNotification() {
    return argThat(argument -> argument instanceof BootNotification);
  }

  @BeforeEach
  void setUp() {
    observer = new BootNotificationObserver();
  }

  @Test
  void handleBootNotificationRequest_WhenBooting_SendsNotification() {
    // Arrange
    SimulatorStateMachine stateMachine = mock(SimulatorStateMachine.class);
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.BootingUp);

    // Act
    observer.handleBootNotificationRequest(webSocketClient, stateMachine);

    // Assert
    verify(webSocketClient).pushMessage(isBootNotification());
  }

  @Test
  void handleBootNotificationRequest_WhenNotBooting_ThrowsException() {
    // Arrange
    SimulatorStateMachine stateMachine = mock(SimulatorStateMachine.class);
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.Available);

    // Act & Assert
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalStateException.class,
        () -> observer.handleBootNotificationRequest(webSocketClient, stateMachine));
  }

  @Test
  void onMessageReceived_WhenStatusAccepted_SetsHeartbeat() {
    // Arrange
    BootNotificationResponse response = mock(BootNotificationResponse.class);
    when(response.getStatus()).thenReturn(RegistrationStatus.ACCEPTED);
    when(response.getInterval()).thenReturn(30);

    OCPPWebSocketClient client = mock(OCPPWebSocketClient.class);
    MessageScheduler messageScheduler = mock(MessageScheduler.class);
    when(client.getScheduler()).thenReturn(messageScheduler);

    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(response);
    when(message.getClient()).thenReturn(client);

    // Act
    observer.onMessageReceived(message);

    // Assert
    verify(messageScheduler).setHeartbeatInterval(30L, TimeUnit.SECONDS);
  }

  @Test
  void onMessageReceived_WhenStatusPending_RegistersJob() {
    // Arrange
    BootNotificationResponse response = mock(BootNotificationResponse.class);
    when(response.getStatus()).thenReturn(RegistrationStatus.PENDING);
    when(response.getInterval()).thenReturn(15);

    OCPPWebSocketClient client = mock(OCPPWebSocketClient.class);
    MessageScheduler messageScheduler = mock(MessageScheduler.class);
    when(client.getScheduler()).thenReturn(messageScheduler);

    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(response);
    when(message.getClient()).thenReturn(client);

    // Act
    observer.onMessageReceived(message);

    // Assert
    verify(messageScheduler).registerJob(eq(15L), eq(TimeUnit.SECONDS), isBootNotification());
  }

  @Test
  void onMessageReceived_WhenStatusRejected_RegistersJob() {
    // Arrange
    BootNotificationResponse response = mock(BootNotificationResponse.class);
    when(response.getStatus()).thenReturn(RegistrationStatus.REJECTED);
    when(response.getInterval()).thenReturn(0);

    OCPPWebSocketClient client = mock(OCPPWebSocketClient.class);
    MessageScheduler messageScheduler = mock(MessageScheduler.class);
    when(client.getScheduler()).thenReturn(messageScheduler);

    OnOCPPMessage message = mock(OnOCPPMessage.class);
    when(message.getMessage()).thenReturn(response);
    when(message.getClient()).thenReturn(client);

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
}
