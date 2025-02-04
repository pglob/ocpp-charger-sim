package com.sim_backend.transactions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.simulator.Simulator;
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
  @Mock private Simulator simulator;
  @Mock private ElectricalTransition elec;
  @Mock private OCPPWebSocketClient client;
  @Mock private OCPPTime ocppTime;
  @Mock private MessageScheduler scheduler;
  @Mock private StartTransactionHandler startHandler;
  @Mock private StopTransactionHandler stopHandler;
  private TransactionHandler transactionHandler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(simulator.getWsClient()).thenReturn(client);
    when(simulator.getStateMachine()).thenReturn(stateMachine);
    when(simulator.getElec()).thenReturn(elec);
    when(client.getScheduler()).thenReturn(scheduler);
    when(scheduler.getTime()).thenReturn(ocppTime);
    when(ocppTime.getSynchronizedTime()).thenReturn(ZonedDateTime.parse("2025-01-19T00:00:00Z"));
    transactionHandler = new TransactionHandler(simulator);
  }

  @Test
  void PreAuthorizeStarttest() {
    when(transactionHandler.getStartHandler().getStateMachine().getCurrentState())
        .thenReturn(SimulatorState.Preparing);

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

    transactionHandler.preAuthorize(1, "Accepted");

    verify(transactionHandler.getStartHandler().getClient()).pushMessage(any(Authorize.class));
    verify(transactionHandler.getStartHandler().getStateMachine())
        .transition(SimulatorState.Charging);
    verify(transactionHandler.getStartHandler().getClient())
        .pushMessage(any(StartTransaction.class));
  }

  @Test
  void PreAuthorizeStoptest() {
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
        .when(transactionHandler.getStartHandler().getClient())
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

    transactionHandler.preAuthorize(1, "Accepted");

    verify(transactionHandler.getStopHandler().getClient()).pushMessage(any(Authorize.class));
    verify(transactionHandler.getStopHandler().getStateMachine())
        .transition(SimulatorState.Available);
    verify(transactionHandler.getStopHandler().getClient()).pushMessage(any(StopTransaction.class));
  }

  @Test
  void PreAuthorizeDeniedtest() {

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

    transactionHandler.preAuthorize(1, "idTag");

    verify(stateMachine, times(1)).transition(SimulatorState.Available);
  }
}
