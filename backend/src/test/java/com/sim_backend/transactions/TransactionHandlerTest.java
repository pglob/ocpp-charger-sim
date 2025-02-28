package com.sim_backend.transactions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.charger.Charger;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
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
  @Mock private ChargerStateMachine stateMachine;
  @Mock private Charger charger;
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
    when(charger.getWsClient()).thenReturn(client);
    when(charger.getStateMachine()).thenReturn(stateMachine);
    when(charger.getElec()).thenReturn(elec);
    when(client.getScheduler()).thenReturn(scheduler);
    when(scheduler.getTime()).thenReturn(ocppTime);
    when(ocppTime.getSynchronizedTime()).thenReturn(ZonedDateTime.parse("2025-01-19T00:00:00Z"));
    transactionHandler = new TransactionHandler(charger);
  }

  @Test
  void PreAuthorizeStarttest() {
    when(transactionHandler.getStartHandler().getStateMachine().getCurrentState())
        .thenReturn(ChargerState.Preparing);

    AuthorizeResponse authorizeResponse =
        new AuthorizeResponse(
            new Authorize(), new AuthorizeResponse.IdTagInfo(AuthorizationStatus.ACCEPTED));

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

    StartTransactionResponse startTransactionResponse =
        new StartTransactionResponse(new StartTransaction(1, "", 1, ""), 1, "Accepted");

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
        .transition(ChargerState.Charging);
    verify(transactionHandler.getStartHandler().getClient())
        .pushMessage(any(StartTransaction.class));
  }

  @Test
  void PreAuthorizeStoptest() {
    when(transactionHandler.getStopHandler().getStateMachine().getCurrentState())
        .thenReturn(ChargerState.Charging);

    AuthorizeResponse authorizeResponse =
        new AuthorizeResponse(
            new Authorize(), new AuthorizeResponse.IdTagInfo(AuthorizationStatus.ACCEPTED));

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

    StopTransactionResponse stopTransactionResponse =
        new StopTransactionResponse(new StopTransaction("", 1, 1, ""), "Accepted");

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
        .transition(ChargerState.Available);
    verify(transactionHandler.getStopHandler().getClient()).pushMessage(any(StopTransaction.class));
  }

  @Test
  void PreAuthorizeDeniedtest() {

    AuthorizeResponse authorizeResponse =
        new AuthorizeResponse(
            new Authorize(), new AuthorizeResponse.IdTagInfo(AuthorizationStatus.BLOCKED));

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

    verify(stateMachine, times(1)).transition(ChargerState.Available);
  }

  @Test
  void PreAuthorizeTimeoutTest() {
    // Capture the registered listener and simulate a timeout by calling onTimeout()
    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              listener.onTimeout();
              return null;
            })
        .when(client)
        .onReceiveMessage(eq(AuthorizeResponse.class), any());

    // Call preAuthorize() to initiate the process
    transactionHandler.preAuthorize(1, "timeoutId");

    // Verify that an Authorize message was pushed
    verify(client).pushMessage(any(Authorize.class));

    // Verify that the listener's onTimeout() caused the client to delete the listener
    verify(client).deleteOnReceiveMessage(eq(AuthorizeResponse.class), any());

    // Verify that the state machine transitions to Available on timeout
    verify(stateMachine).transition(ChargerState.Available);

    // Verify that both startInProgress and stopInProgress flags are reset to false
    assertFalse(
        transactionHandler.getStartInProgress().get(),
        "startInProgress should be false after a timeout");
    assertFalse(
        transactionHandler.getStopInProgress().get(),
        "stopInProgress should be false after a timeout");
  }
}
