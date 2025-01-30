package com.sim_backend.transactions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.*;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StopTransactionHandlerTest {
  @Mock private SimulatorStateMachine stateMachine;
  @Mock private OCPPWebSocketClient client;
  @Mock private OCPPTime ocppTime;
  @Mock private MessageScheduler scheduler;

  private StopTransactionHandler handler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(client.getScheduler()).thenReturn(scheduler);
    when(scheduler.getTime()).thenReturn(ocppTime);
    when(ocppTime.getSynchronizedTime()).thenReturn(ZonedDateTime.parse("2025-01-19T00:00:00Z"));
    handler = new StopTransactionHandler(stateMachine, client);
  }

  @Test
  void preAuthorizeAcceptedtest() {
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.Charging);

    AuthorizeResponse authorizeResponse =
        new AuthorizeResponse(new AuthorizeResponse.IdTagInfo(AuthorizationStatus.ACCEPTED));

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(authorizeResponse);
              listener.onMessageReceived(message);
              return null;
            })
        .when(client)
        .onReceiveMessage(eq(AuthorizeResponse.class), any());

    handler.preAuthorize("Accepted");
    verify(client).pushMessage(any(Authorize.class));
  }

  @Test
  void preAuthorizeDeniedtest() {
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.Charging);

    AuthorizeResponse authorizeResponse =
        new AuthorizeResponse(new AuthorizeResponse.IdTagInfo(AuthorizationStatus.BLOCKED));

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(authorizeResponse);
              listener.onMessageReceived(message);
              return null;
            })
        .when(client)
        .onReceiveMessage(eq(AuthorizeResponse.class), any());

    handler.preAuthorize("Blocked");
    verify(client).pushMessage(any(Authorize.class));
  }

  @Test
  void initiateStopTransactiontest() {
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.Charging);
    StopTransactionResponse stopTransactionResponse = new StopTransactionResponse("Accepted");

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(stopTransactionResponse);
              listener.onMessageReceived(message);
              return null;
            })
        .when(client)
        .onReceiveMessage(eq(StopTransactionResponse.class), any());

    handler.initiateStopTransaction(1);

    verify(client).pushMessage(any(StopTransaction.class));
    verify(stateMachine).transition(SimulatorState.Available);
  }

  @Test
  void StopChargingSameIdtest() {
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.Charging);
    StopTransactionResponse stopTransactionResponse = new StopTransactionResponse("Accepted");

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(stopTransactionResponse);
              listener.onMessageReceived(message);
              return null;
            })
        .when(client)
        .onReceiveMessage(eq(StopTransactionResponse.class), any());

    handler.StopCharging("idTag1", "idTag1", 1);

    verify(client).pushMessage(any(StopTransaction.class));
    verify(stateMachine).transition(SimulatorState.Available);
  }

  @Test
  void StopChargingDiffIdtest() {
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.Charging);
    StopTransactionResponse stopTransactionResponse = new StopTransactionResponse("Accepted");

    doAnswer(
            invocation -> {
              stateMachine.transition(SimulatorState.Preparing);
              return null;
            })
        .when(stateMachine)
        .transition(SimulatorState.Preparing);

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(stopTransactionResponse);
              listener.onMessageReceived(message);
              return null;
            })
        .when(client)
        .onReceiveMessage(eq(StopTransactionResponse.class), any());

    handler.StopCharging("idTag1", "idTag2", 1);

    verify(client).pushMessage(any(StopTransaction.class));
    verify(stateMachine).transition(SimulatorState.Available);
  }
}
