package com.sim_backend.transactions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.sim_backend.charger.Charger;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.enums.Reason;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.*;
import com.sim_backend.websockets.observers.MeterValuesObserver;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TransactionHandlerTest {
  @Mock private ChargerStateMachine stateMachine;
  @Mock private Charger charger;
  @Mock private ElectricalTransition elec;
  @Mock private MeterValuesObserver meter;
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
    when(charger.getMeterValueObserver()).thenReturn(meter);
    when(client.getScheduler()).thenReturn(scheduler);
    when(scheduler.getTime()).thenReturn(ocppTime);
    when(ocppTime.getSynchronizedTime()).thenReturn(ZonedDateTime.parse("2025-01-19T00:00:00Z"));
    transactionHandler = new TransactionHandler(charger);
    assertNotNull(transactionHandler.getTransactionId());
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

    transactionHandler.preAuthorize(1, "Accepted", null);

    verify(transactionHandler.getStartHandler().getClient()).pushMessage(any(Authorize.class));
    verify(transactionHandler.getStartHandler().getStateMachine())
        .checkAndTransition(ChargerState.Preparing, ChargerState.Charging);
    verify(transactionHandler.getStartHandler().getClient())
        .pushMessage(any(StartTransaction.class));
  }

  @Test
  void PreAuthorizeStoptest() {
    when(transactionHandler.getStopHandler().getStateMachine().getCurrentState())
        .thenReturn(ChargerState.Charging);
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    when(stateMachine.checkAndTransition(ChargerState.Charging, ChargerState.Available))
        .thenReturn(true);

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
        .when(client)
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

    transactionHandler.preAuthorize(1, "Accepted", null);

    verify(transactionHandler.getStopHandler().getClient()).pushMessage(any(Authorize.class));
    verify(transactionHandler.getStopHandler().getStateMachine())
        .checkAndTransition(ChargerState.Charging, ChargerState.Available);
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

    transactionHandler.preAuthorize(1, "idTag", null);

    verify(stateMachine, times(1))
        .checkAndTransition(ChargerState.Preparing, ChargerState.Available);
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
    transactionHandler.preAuthorize(1, "timeoutId", null);

    // Verify that an Authorize message was pushed
    verify(client).pushMessage(any(Authorize.class));

    // Verify that the listener's onTimeout() caused the client to delete the listener
    verify(client).deleteOnReceiveMessage(eq(AuthorizeResponse.class), any());

    // Verify that the state machine transitions to Available on timeout
    verify(stateMachine).checkAndTransition(ChargerState.Preparing, ChargerState.Available);

    // Verify that both startInProgress and stopInProgress flags are reset to false
    assertFalse(
        transactionHandler.getStartInProgress().get(),
        "startInProgress should be false after a timeout");
    assertFalse(
        transactionHandler.getStopInProgress().get(),
        "stopInProgress should be false after a timeout");
  }

  @Test
  void startChargingInvalidStateTest() {
    // Test when the current state is not Available, startCharging does nothing
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    transactionHandler.startCharging(1, "testId");

    // Verify no authorize message was pushed
    verify(client, never()).pushMessage(any(Authorize.class));
  }

  @Test
  void startChargingAlreadyInProgressTest() {
    // Test when startCharging is already in progress, startCharging does nothing
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    transactionHandler.getStartInProgress().set(true);
    transactionHandler.startCharging(1, "testId");

    // Verify no authorize message was pushed
    verify(client, never()).pushMessage(any(Authorize.class));

    transactionHandler.getStartInProgress().set(false);
  }

  @Test
  void startChargingSuccessTest() {
    // Test when the charger is Available, startCharging transitions to Preparing and triggers
    // pre-authorization
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    when(stateMachine.checkAndTransition(ChargerState.Available, ChargerState.Preparing))
        .thenReturn(true);

    TransactionHandler spyHandler = spy(transactionHandler);
    doNothing().when(spyHandler).preAuthorize(anyInt(), anyString(), any());
    spyHandler.startCharging(1, "testId");

    // Verify that the state machine transitioned to Preparing
    verify(stateMachine).checkAndTransition(ChargerState.Available, ChargerState.Preparing);

    // Verify that preAuthorize() was called with the correct parameters
    verify(spyHandler).preAuthorize(eq(1), eq("testId"), isNull());
  }

  @Test
  void stopChargingInvalidStateTest() {
    // Test when the current state is not Charging, stopCharging does nothing
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    transactionHandler.stopCharging("testId", Reason.LOCAL);

    // Verify no authorize message was pushed
    verify(client, never()).pushMessage(any(Authorize.class));
  }

  @Test
  void stopChargingAlreadyInProgressTest() {
    // Test when stopCharging is already in progress, stopCharging does nothing
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    transactionHandler.getStopInProgress().set(true);
    transactionHandler.stopCharging("testId", Reason.LOCAL);

    // Verify no authorize message was pushed
    verify(client, never()).pushMessage(any(Authorize.class));

    transactionHandler.getStopInProgress().set(false);
  }

  @Test
  void stopChargingDirectStopTest() throws Exception {
    // Test when the provided idTag matches the current authorized idTag, stopCharging triggers a
    // direct stop
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    transactionHandler.getStopInProgress().set(false);

    Field idTagField = TransactionHandler.class.getDeclaredField("idTag");
    idTagField.setAccessible(true);
    idTagField.set(transactionHandler, "testId");

    // Spy on the stopHandler to verify that its initiateStopTransaction() is called
    Object originalStopHandler = transactionHandler.getStopHandler();
    StopTransactionHandler spiedStopHandler = spy((StopTransactionHandler) originalStopHandler);
    Field stopHandlerField = TransactionHandler.class.getDeclaredField("stopHandler");
    stopHandlerField.setAccessible(true);
    stopHandlerField.set(transactionHandler, spiedStopHandler);

    transactionHandler.stopCharging("testId", Reason.LOCAL);
    // Verify that initiateStopTransaction() was called on the stop handler with the expected
    // parameters
    verify(spiedStopHandler)
        .initiateStopTransaction(
            eq("testId"), eq(Reason.LOCAL), any(AtomicInteger.class), eq(elec), any());
  }

  @Test
  void stopChargingPreAuthorizeTest() {
    // Test when the provided idTag does not match the current authorized idTag, stopCharging
    // triggers pre-authorization
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    Field idTagField;
    try {
      idTagField = TransactionHandler.class.getDeclaredField("idTag");
      idTagField.setAccessible(true);
      idTagField.set(transactionHandler, "authorizedId");
    } catch (Exception e) {
      e.printStackTrace();
    }
    TransactionHandler spyHandler = spy(transactionHandler);

    doNothing().when(spyHandler).preAuthorize(anyInt(), anyString(), any());
    spyHandler.stopCharging("differentId", Reason.LOCAL);

    // Verify that preAuthorize() was called with connectorId = -1 and the correct idTag and
    // reason
    verify(spyHandler).preAuthorize(eq(-1), eq("differentId"), eq(Reason.LOCAL));
  }

  @Test
  void forceStopChargingSuccessTest() throws Exception {
    // Test when the charger is in Charging state and transactionId is valid, forceStopCharging
    // triggers a stop
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    transactionHandler.getTransactionId().set(100);

    // py on the stopHandler to verify that its initiateStopTransaction() is called
    Object originalStopHandler = transactionHandler.getStopHandler();
    StopTransactionHandler spiedStopHandler = spy((StopTransactionHandler) originalStopHandler);
    Field stopHandlerField = TransactionHandler.class.getDeclaredField("stopHandler");
    stopHandlerField.setAccessible(true);
    stopHandlerField.set(transactionHandler, spiedStopHandler);

    // Ensure stopInProgress is false
    transactionHandler.getStopInProgress().set(false);
    transactionHandler.forceStopCharging(Reason.LOCAL);

    // Verify that initiateStopTransaction() was called on the stop handler with a null idTag
    verify(spiedStopHandler)
        .initiateStopTransaction(
            isNull(), eq(Reason.LOCAL), any(AtomicInteger.class), eq(elec), any());
  }

  @Test
  void forceStopChargingNoActionTest() throws Exception {
    // Test when the state is not Charging or Faulted, forceStopCharging does nothing
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    transactionHandler.getTransactionId().set(100);
    transactionHandler.getStopInProgress().set(false);
    transactionHandler.forceStopCharging(Reason.LOCAL);

    // Spy on the stopHandler to verify that no stop transaction was initiated
    Object originalStopHandler = transactionHandler.getStopHandler();
    StopTransactionHandler spiedStopHandler = spy((StopTransactionHandler) originalStopHandler);
    Field stopHandlerField = TransactionHandler.class.getDeclaredField("stopHandler");
    stopHandlerField.setAccessible(true);
    stopHandlerField.set(transactionHandler, spiedStopHandler);

    // Verify no stop transaction was initiated
    verify(spiedStopHandler, never()).initiateStopTransaction(any(), any(), any(), any(), any());

    // Test when transactionId is -1, forceStopCharging does nothing
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    transactionHandler.getTransactionId().set(-1);
    transactionHandler.forceStopCharging(Reason.LOCAL);

    // Verify no stop transaction was initiated
    verify(spiedStopHandler, never()).initiateStopTransaction(any(), any(), any(), any(), any());

    // Test when stopInProgress is true, forceStopCharging does nothing
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    transactionHandler.getTransactionId().set(100);
    transactionHandler.getStopInProgress().set(true);
    transactionHandler.forceStopCharging(Reason.LOCAL);

    // Verify no stop transaction was initiated
    verify(spiedStopHandler, never()).initiateStopTransaction(any(), any(), any(), any(), any());
  }
}
