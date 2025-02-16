package com.sim_backend.transactions;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StopTransactionHandlerTest {
  @Mock private ChargerStateMachine stateMachine;
  @Mock private ElectricalTransition elec;
  @Mock private OCPPWebSocketClient client;
  @Mock private OCPPTime ocppTime;
  @Mock private MessageScheduler scheduler;

  private StopTransactionHandler handler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(elec.getEnergyActiveImportRegister()).thenReturn(0.0f);
    when(client.getScheduler()).thenReturn(scheduler);
    when(scheduler.getTime()).thenReturn(ocppTime);
    when(ocppTime.getSynchronizedTime()).thenReturn(ZonedDateTime.parse("2025-01-19T00:00:00Z"));
    handler = new StopTransactionHandler(stateMachine, client);
  }

  @Test
  void initiateStopTransactiontest() {
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
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

    handler.initiateStopTransaction(1, "idTag", elec, new AtomicBoolean());

    verify(client).pushMessage(any(StopTransaction.class));
    verify(stateMachine).transition(ChargerState.Available);
  }
}
