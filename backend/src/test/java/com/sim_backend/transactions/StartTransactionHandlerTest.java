package com.sim_backend.transactions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.*;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StartTransactionHandlerTest {
  @Mock private SimulatorStateMachine stateMachine;
  @Mock private OCPPWebSocketClient client;
  @Mock private OCPPTime ocppTime;
  @Mock private MessageScheduler scheduler;

  private StartTransactionHandler handler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(client.getScheduler()).thenReturn(scheduler);
    when(scheduler.getTime()).thenReturn(ocppTime);
    when(ocppTime.getSynchronizedTime()).thenReturn(ZonedDateTime.parse("2025-01-19T00:00:00Z"));
    handler = new StartTransactionHandler(stateMachine, client);
  }

  @Test
  void initiateStartTransactiontest() {
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.Preparing);

    StartTransactionResponse startTransactionResponse = new StartTransactionResponse(1, "Accepted");

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

    handler.initiateStartTransaction(1, "Accepted");

    verify(client).pushMessage(any(StartTransaction.class));
    verify(stateMachine).transition(SimulatorState.Charging);
  }
}
