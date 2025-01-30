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

public class TransactionHandlerTest {
  @Mock private SimulatorStateMachine stateMachine;
  @Mock private OCPPWebSocketClient client;
  @Mock private OCPPTime ocppTime;
  @Mock private MessageScheduler scheduler;
  @Mock private StartTransactionHandler startHandler;
  @Mock private StopTransactionHandler stopHandler;
  private TransactionHandler transactionHandler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(client.getScheduler()).thenReturn(scheduler);
    when(scheduler.getTime()).thenReturn(ocppTime);
    when(ocppTime.getSynchronizedTime()).thenReturn(ZonedDateTime.parse("2025-01-19T00:00:00Z"));
    transactionHandler = new TransactionHandler(stateMachine, client);
  }

  @Test
  void PreAuthStartChargingtest() {
    when(transactionHandler.getStartHandler().getStateMachine().getCurrentState())
        .thenReturn(SimulatorState.Available)
        .thenReturn(SimulatorState.Preparing)
        .thenReturn(SimulatorState.Charging);

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
        .when(transactionHandler.getStartHandler().getClient())
        .onReceiveMessage(eq(AuthorizeResponse.class), any());

    StartTransactionResponse startTransactionResponse = new StartTransactionResponse(1, "Accepted");

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(startTransactionResponse);
              listener.onMessageReceived(message);
              return null;
            })
        .when(transactionHandler.getStartHandler().getClient())
        .onReceiveMessage(eq(StartTransactionResponse.class), any());

    transactionHandler.PreAuthlStartCharging(1, "Accepted");

    verify(transactionHandler.getStartHandler().getStateMachine())
        .transition(SimulatorState.Preparing);
    verify(transactionHandler.getStartHandler().getClient()).pushMessage(any(Authorize.class));

    verify(transactionHandler.getStartHandler().getStateMachine())
        .transition(SimulatorState.Charging);
    verify(transactionHandler.getStartHandler().getClient())
        .pushMessage(any(StartTransaction.class));
  }

  @Test
  void PostAuthStartChargingtest() {
    when(transactionHandler.getStartHandler().getStateMachine().getCurrentState())
        .thenReturn(SimulatorState.Preparing)
        .thenReturn(SimulatorState.Charging);

    StartTransactionResponse startTransactionResponse = new StartTransactionResponse(1, "Accepted");

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(startTransactionResponse);
              listener.onMessageReceived(message);
              return null;
            })
        .when(transactionHandler.getStartHandler().getClient())
        .onReceiveMessage(eq(StartTransactionResponse.class), any());

    transactionHandler.PostAuthStartCharging(1, "Accepted");

    verify(transactionHandler.getStartHandler().getStateMachine())
        .transition(SimulatorState.Charging);
    verify(transactionHandler.getStartHandler().getClient())
        .pushMessage(any(StartTransaction.class));
  }

  @Test
  void StopChargingSameIdtest() {
    when(transactionHandler.getStopHandler().getStateMachine().getCurrentState())
        .thenReturn(SimulatorState.Charging);

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
        .when(transactionHandler.getStopHandler().getClient())
        .onReceiveMessage(eq(AuthorizeResponse.class), any());

    StopTransactionResponse stopTransactionResponse = new StopTransactionResponse("Accepted");

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(stopTransactionResponse);
              listener.onMessageReceived(message);
              return null;
            })
        .when(transactionHandler.getStopHandler().getClient())
        .onReceiveMessage(eq(StopTransactionResponse.class), any());

    transactionHandler.StopCharging(1, "idTag1", "idTag1");

    verify(transactionHandler.getStopHandler().getClient()).pushMessage(any(StopTransaction.class));
    verify(transactionHandler.getStopHandler().getStateMachine())
        .transition(SimulatorState.Available);
  }

  @Test
  void StopChargingDiffIdtest() {
    when(transactionHandler.getStopHandler().getStateMachine().getCurrentState())
        .thenReturn(SimulatorState.Charging);

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
        .when(transactionHandler.getStopHandler().getClient())
        .onReceiveMessage(eq(AuthorizeResponse.class), any());

    StopTransactionResponse stopTransactionResponse = new StopTransactionResponse("Accepted");

    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              OnOCPPMessage message = mock(OnOCPPMessage.class);
              when(message.getMessage()).thenReturn(stopTransactionResponse);
              listener.onMessageReceived(message);
              return null;
            })
        .when(transactionHandler.getStopHandler().getClient())
        .onReceiveMessage(eq(StopTransactionResponse.class), any());

    transactionHandler.StopCharging(1, "idTag1", "idTag2");

    verify(transactionHandler.getStopHandler().getClient()).pushMessage(any(StopTransaction.class));
    verify(transactionHandler.getStopHandler().getStateMachine())
        .transition(SimulatorState.Available);
  }
}
