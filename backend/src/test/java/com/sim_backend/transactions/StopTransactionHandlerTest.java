package com.sim_backend.transactions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ReadingContext;
import com.sim_backend.websockets.enums.Reason;
import com.sim_backend.websockets.messages.StopTransaction;
import com.sim_backend.websockets.observers.MeterValuesObserver;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StopTransactionHandlerTest {
  @Mock private ChargerStateMachine stateMachine;
  @Mock private ElectricalTransition elec;
  @Mock private MeterValuesObserver meter;
  @Mock private OCPPWebSocketClient client;
  @Mock private OCPPTime ocppTime;
  @Mock private MessageScheduler scheduler;

  private StopTransactionHandler handler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(elec.getEnergyActiveImportRegister()).thenReturn(5.0f);
    when(client.getScheduler()).thenReturn(scheduler);
    when(scheduler.getTime()).thenReturn(ocppTime);
    when(ocppTime.getSynchronizedTime()).thenReturn(ZonedDateTime.parse("2025-01-19T00:00:00Z"));
    when(client.pushMessage(any(StopTransaction.class))).thenReturn(true);
    handler = new StopTransactionHandler(stateMachine, client, meter);
  }

  /**
   * Helper method to verify the StopTransaction message and post-invocation state.
   *
   * @param expectedTxnId expected transaction id in the StopTransaction message.
   * @param expectedIdTag expected idTag (null if not provided).
   * @param expectedReason expected reason (null if not provided).
   * @param transactionId the AtomicInteger used for transactionId (should be reset to -1).
   * @param stopInProgress the AtomicBoolean used to indicate progress (should be reset to false).
   */
  private void verifyStopTransactionMessage(
      int expectedTxnId,
      String expectedIdTag,
      Reason expectedReason,
      AtomicInteger transactionId,
      AtomicBoolean stopInProgress) {

    ArgumentCaptor<StopTransaction> captor = ArgumentCaptor.forClass(StopTransaction.class);
    verify(client).pushMessage(captor.capture());
    StopTransaction message = captor.getValue();

    verify(meter, times(1)).sendMeterValues(ReadingContext.TRANSACTION_END);

    assertEquals(expectedTxnId, message.getTransactionId());
    assertEquals(5000, message.getMeterStop());
    String expectedTimestamp =
        ZonedDateTime.parse("2025-01-19T00:00:00Z")
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"));
    assertEquals(expectedTimestamp, message.getTimestamp());
    if (expectedIdTag == null) {
      assertNull(message.getIdTag());
    } else {
      assertEquals(expectedIdTag, message.getIdTag());
    }
    assertEquals(expectedReason, message.getReason());

    // Verify that the transaction id and stopInProgress flag were reset
    assertEquals(-1, transactionId.get());
    assertFalse(stopInProgress.get());
  }

  @Test
  void initiateStopTransaction_ReasonNotNull_IdTagNull_Test() {
    // Test when a reason is provided and idTag is null
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    when(stateMachine.checkAndTransition(ChargerState.Charging, ChargerState.Available))
        .thenReturn(true);
    AtomicInteger transactionId = new AtomicInteger(1);
    AtomicBoolean stopInProgress = new AtomicBoolean(true);
    Reason reason = Reason.LOCAL;

    handler.initiateStopTransaction(null, reason, transactionId, elec, stopInProgress);

    // Verify that checkAndTransition was called
    verify(stateMachine).checkAndTransition(ChargerState.Charging, ChargerState.Available);
    // Verify the StopTransaction message and flag resets
    verifyStopTransactionMessage(1, null, reason, transactionId, stopInProgress);
  }

  @Test
  void initiateStopTransaction_ReasonNull_IdTagNotNull_Test() {
    // Test when reason is null and an idTag is provided.
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    when(stateMachine.checkAndTransition(ChargerState.Charging, ChargerState.Available))
        .thenReturn(true);
    AtomicInteger transactionId = new AtomicInteger(2);
    AtomicBoolean stopInProgress = new AtomicBoolean(true);
    String idTag = "user123";

    handler.initiateStopTransaction(idTag, null, transactionId, elec, stopInProgress);

    // Verify that checkAndTransition was called
    verify(stateMachine).checkAndTransition(ChargerState.Charging, ChargerState.Available);
    // Verify the StopTransaction message and flag resets
    verifyStopTransactionMessage(2, idTag, null, transactionId, stopInProgress);
  }

  @Test
  void initiateStopTransaction_ReasonNotNull_IdTagNotNull_Test() {
    // Test when both reason and idTag are provided
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Charging);
    when(stateMachine.checkAndTransition(ChargerState.Charging, ChargerState.Available))
        .thenReturn(true);
    AtomicInteger transactionId = new AtomicInteger(3);
    AtomicBoolean stopInProgress = new AtomicBoolean(true);
    String idTag = "user456";
    Reason reason = Reason.LOCAL;

    handler.initiateStopTransaction(idTag, reason, transactionId, elec, stopInProgress);

    // Verify that checkAndTransition was called
    verify(stateMachine).checkAndTransition(ChargerState.Charging, ChargerState.Available);
    // Verify the StopTransaction message and flag resets
    verifyStopTransactionMessage(3, idTag, reason, transactionId, stopInProgress);
  }

  @Test
  void initiateStopTransaction_FaultedState_Test() {
    // Test when the current state is Faulted, checkAndTransition returns false so processing is
    // aborted
    when(stateMachine.getCurrentState()).thenReturn(ChargerState.Faulted);
    when(stateMachine.checkAndTransition(ChargerState.Charging, ChargerState.Available))
        .thenReturn(false);
    AtomicInteger transactionId = new AtomicInteger(4);
    AtomicBoolean stopInProgress = new AtomicBoolean(true);
    String idTag = "user789";

    handler.initiateStopTransaction(idTag, null, transactionId, elec, stopInProgress);

    // Verify that checkAndTransition was called
    verify(stateMachine).checkAndTransition(ChargerState.Charging, ChargerState.Available);
    // Verify that pushMessage was never called
    verify(client, never()).pushMessage(any(StopTransaction.class));
    // Since processing was aborted, transactionId and stopInProgress should not be changed
    assertEquals(4, transactionId.get());
    assertTrue(stopInProgress.get());
  }
}
