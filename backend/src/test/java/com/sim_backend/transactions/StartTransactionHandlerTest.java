package com.sim_backend.transactions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StartTransactionHandlerTest {
  @Mock private SimulatorStateMachine stateMachine;
  @Mock private OCPPWebSocketClient client;

  private StartTransactionHandler handler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    handler = new StartTransactionHandler(stateMachine, client);
  }

  @Test
  void preAuthorizetest() {
    when(stateMachine.getCurrentState()).thenReturn(SimulatorState.Available);

    AuthorizeResponse authorizeResponse =
        new AuthorizeResponse(new AuthorizeResponse.IdTagInfo(AuthorizationStatus.ACCEPTED));
    StartTransactionResponse startTransactionResponse = new StartTransactionResponse(1, "Accepted");

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

    handler.preAuthorize(1, "Accepted", 0, "2025-1-19T00:00:00Z");
    verify(client).pushMessage(any(Authorize.class));
    verify(client).pushMessage(any(StartTransaction.class));
    verify(stateMachine).transition(SimulatorState.Preparing);
    verify(stateMachine).transition(SimulatorState.Charging);
  }
}
