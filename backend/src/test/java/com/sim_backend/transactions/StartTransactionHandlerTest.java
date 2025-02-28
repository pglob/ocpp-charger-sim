package com.sim_backend.transactions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.*;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StartTransactionHandlerTest {
  @Mock private ChargerStateMachine stateMachine;
  @Mock private ElectricalTransition elec;
  @Mock private OCPPWebSocketClient client;
  @Mock private OCPPTime ocppTime;
  @Mock private MessageScheduler scheduler;

  private StartTransactionHandler handler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(elec.getEnergyActiveImportRegister()).thenReturn(1000.0f);
    when(client.getScheduler()).thenReturn(scheduler);
    when(scheduler.getTime()).thenReturn(ocppTime);
    when(ocppTime.getSynchronizedTime()).thenReturn(ZonedDateTime.parse("2025-01-19T00:00:00Z"));
    handler = new StartTransactionHandler(stateMachine, client);
  }

  @Test
  void initiateStartTransactiontest() {
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Preparing);

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
        .when(client)
        .onReceiveMessage(eq(StartTransactionResponse.class), any());

    handler.initiateStartTransaction(1, "Accepted", new AtomicInteger(), elec, new AtomicBoolean());

    verify(client).pushMessage(any(StartTransaction.class));
    verify(stateMachine).checkAndTransition(ChargerState.Preparing, ChargerState.Charging);
  }

  @Test
  void initiateStartTransactionTimeoutTest() {
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Preparing);
    AtomicInteger transactionId = new AtomicInteger();
    AtomicBoolean startInProgress = new AtomicBoolean(true);

    // Capture the registered listener and simulate a timeout by calling onTimeout()
    doAnswer(
            invocation -> {
              OnOCPPMessageListener listener = invocation.getArgument(1);
              listener.onTimeout();
              return null;
            })
        .when(client)
        .onReceiveMessage(eq(StartTransactionResponse.class), any());

    // Initiate the start transaction which registers the listener and pushes the message
    handler.initiateStartTransaction(1, "Accepted", transactionId, elec, startInProgress);

    // Verify that a StartTransaction message was pushed
    verify(client).pushMessage(any(StartTransaction.class));

    // Verify that onTimeout() resulted in the state machine transitioning to Available
    verify(stateMachine).checkAndTransition(ChargerState.Preparing, ChargerState.Available);

    // Verify that the startInProgress flag is set to false after the timeout
    assertFalse(startInProgress.get(), "startInProgress should be false after a timeout");
  }
}
