package com.sim_backend.websockets.observers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.MeterValuesSampledData;
import com.sim_backend.websockets.enums.ReadingContext;
import com.sim_backend.websockets.enums.UnitOfMeasure;
import com.sim_backend.websockets.messages.MeterValues;
import com.sim_backend.websockets.messages.MeterValues.MeterValue;
import com.sim_backend.websockets.messages.MeterValues.SampledValue;
import com.sim_backend.websockets.types.RepeatingTimedTask;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Unit tests for {@link MeterValuesObserver}. */
public class MeterValuesObserverTest {

  private MeterValuesObserver observer;
  private OCPPWebSocketClient wsClient;
  private TransactionHandler transactionHandler;
  private ConfigurationRegistry config;
  private ElectricalTransition elec;
  private ChargerStateMachine stateMachine;
  private MessageScheduler scheduler;

  @BeforeEach
  public void setUp() {
    observer = new MeterValuesObserver();

    wsClient = mock(OCPPWebSocketClient.class, RETURNS_DEEP_STUBS);
    transactionHandler = mock(TransactionHandler.class);
    config = mock(ConfigurationRegistry.class);
    elec = mock(ElectricalTransition.class);
    stateMachine = mock(ChargerStateMachine.class);
    scheduler = mock(MessageScheduler.class);

    // Create a mock for the OCPPTime instance
    OCPPTime ocppTime = mock(OCPPTime.class);
    ZonedDateTime fixedTime = ZonedDateTime.now();
    when(ocppTime.getSynchronizedTime()).thenReturn(fixedTime);
    when(scheduler.getTime()).thenReturn(ocppTime);

    // Set wsClient to return our scheduler
    when(wsClient.getScheduler()).thenReturn(scheduler);

    // Instantiate the observer
    observer.instantiate(wsClient, stateMachine, transactionHandler, elec, config);
  }

  @Test
  public void testInstantiateRegistersObserver() {
    // Verify that instantiate calls addObserver on the state machine
    verify(stateMachine).addObserver(observer);
  }

  @Test
  public void testCreateMeterValue_CurrentOffered() {
    // Sse CURRENT_OFFERED as the sampled data type
    when(config.getMeterValuesSampledData()).thenReturn(MeterValuesSampledData.CURRENT_OFFERED);
    when(elec.getCurrentOffered()).thenReturn(15);

    MeterValue meterValue = observer.createMeterValue(ReadingContext.SAMPLE_PERIODIC);

    // Verify that the returned meter value is not null and contains expected values
    assertNotNull(meterValue);
    ZonedDateTime expectedTime = scheduler.getTime().getSynchronizedTime();
    assertEquals(expectedTime, meterValue.getTimestamp());
    assertEquals(1, meterValue.getSampledValue().size());

    SampledValue sampledValue = meterValue.getSampledValue().get(0);
    assertEquals(String.valueOf(15), sampledValue.getValue());
    assertEquals(ReadingContext.SAMPLE_PERIODIC, sampledValue.getContext());
    assertEquals(MeterValuesSampledData.CURRENT_OFFERED, sampledValue.getMeasurand());
    assertEquals(UnitOfMeasure.A, sampledValue.getUnit());
  }

  @Test
  public void testSendMeterValues_WithValidTransactionId() {
    // Sse CURRENT_IMPORT as the sampled data type
    when(config.getMeterValuesSampledData()).thenReturn(MeterValuesSampledData.CURRENT_IMPORT);
    when(elec.getCurrentImport()).thenReturn(20);
    // Return a valid transaction id (other than -1)
    when(transactionHandler.getTransactionId()).thenReturn(new AtomicInteger(123));

    observer.sendMeterValues(ReadingContext.SAMPLE_PERIODIC);

    // Capture and verify the MeterValues message sent to the WebSocket client
    ArgumentCaptor<MeterValues> captor = ArgumentCaptor.forClass(MeterValues.class);
    verify(wsClient).pushMessage(captor.capture());
    MeterValues message = captor.getValue();

    assertEquals(1, message.getConnectorId());
    assertEquals(123, message.getTransactionId());
    assertEquals(1, message.getMeterValue().size());

    MeterValue meterValue = message.getMeterValue().get(0);
    SampledValue sampledValue = meterValue.getSampledValue().get(0);
    assertEquals(String.valueOf(20), sampledValue.getValue());
    assertEquals(MeterValuesSampledData.CURRENT_IMPORT, sampledValue.getMeasurand());
    // For CURRENT_IMPORT, the unit is set to A
    assertEquals(UnitOfMeasure.A, sampledValue.getUnit());
  }

  @Test
  public void testSendMeterValues_WithInvalidTransactionId() {
    // Use POWER_ACTIVE_IMPORT as the sampled data type
    when(config.getMeterValuesSampledData()).thenReturn(MeterValuesSampledData.POWER_ACTIVE_IMPORT);
    when(elec.getPowerActiveImport()).thenReturn(5.0f);
    // Return -1 so that it becomes null
    when(transactionHandler.getTransactionId()).thenReturn(new AtomicInteger(-1));

    observer.sendMeterValues(ReadingContext.SAMPLE_PERIODIC);

    // Capture and verify the MeterValues message
    ArgumentCaptor<MeterValues> captor = ArgumentCaptor.forClass(MeterValues.class);
    verify(wsClient).pushMessage(captor.capture());
    MeterValues message = captor.getValue();

    assertEquals(1, message.getConnectorId());
    assertNull(message.getTransactionId());

    SampledValue sampledValue = message.getMeterValue().get(0).getSampledValue().get(0);
    assertEquals(String.valueOf(5.0f), sampledValue.getValue());
    assertEquals(MeterValuesSampledData.POWER_ACTIVE_IMPORT, sampledValue.getMeasurand());
    assertEquals(UnitOfMeasure.KW, sampledValue.getUnit());
  }

  @Test
  public void testOnStateChanged_ChargingSchedulesTask() {
    // Setup a sample interval
    when(config.getMeterValueSampleInterval()).thenReturn(10);
    // Create a dummy repeating task to simulate scheduling
    RepeatingTimedTask dummyTask = mock(RepeatingTimedTask.class);
    when(scheduler.periodicFunctionJob(eq(30L), eq(10L), eq(TimeUnit.SECONDS), any()))
        .thenReturn(dummyTask);

    observer.onStateChanged(ChargerState.Charging);

    // Verify that a periodic job was scheduled
    verify(scheduler).periodicFunctionJob(eq(30L), eq(10L), eq(TimeUnit.SECONDS), any());
  }

  @Test
  public void testOnStateChanged_NonChargingKillsTask() {
    when(config.getMeterValueSampleInterval()).thenReturn(10);
    // Create a dummy repeating task
    RepeatingTimedTask dummyTask = mock(RepeatingTimedTask.class);
    when(scheduler.periodicFunctionJob(eq(30L), eq(10L), eq(TimeUnit.SECONDS), any()))
        .thenReturn(dummyTask);

    // Transition to Charging to schedule the task.
    observer.onStateChanged(ChargerState.Charging);
    // Then, transition to a non-Charging state to cancel the task
    observer.onStateChanged(ChargerState.Available);

    verify(scheduler).killJob(dummyTask);
  }
}
