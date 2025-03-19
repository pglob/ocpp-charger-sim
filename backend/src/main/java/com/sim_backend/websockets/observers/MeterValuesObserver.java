package com.sim_backend.websockets.observers;

import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.state.StateObserver;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.MeterValuesSampledData;
import com.sim_backend.websockets.enums.ReadingContext;
import com.sim_backend.websockets.enums.UnitOfMeasure;
import com.sim_backend.websockets.messages.MeterValues;
import com.sim_backend.websockets.messages.MeterValues.MeterValue;
import com.sim_backend.websockets.messages.MeterValues.SampledValue;
import com.sim_backend.websockets.types.RepeatingTimedTask;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MeterValuesObserver implements StateObserver {
  private OCPPWebSocketClient wsClient;
  private TransactionHandler tHandler;
  private ConfigurationRegistry config;
  private ElectricalTransition elec;
  private RepeatingTimedTask meterTask = null;

  /**
   * Instantiate the MeterValuesObserver class variables. This is done seperately from the
   * constructor to allow for circular depencies.
   */
  public void instantiate(
      OCPPWebSocketClient wsClient,
      ChargerStateMachine stateMachine,
      TransactionHandler tHandler,
      ElectricalTransition elec,
      ConfigurationRegistry config) {
    this.wsClient = wsClient;
    stateMachine.addObserver(this);
    this.tHandler = tHandler;
    this.elec = elec;
    this.config = config;
  }

  /**
   * Send a Meter Values message.
   *
   * @param context The reason for sending a Meter Values message.
   */
  public void sendMeterValues(ReadingContext context) {
    MeterValue meterValue = createMeterValue(context);

    int connectorId = 1; // We only support 1 connector
    Integer transactionId = tHandler.getTransactionId().get();
    if (transactionId == -1) transactionId = null;

    MeterValues message =
        new MeterValues(connectorId, transactionId, Collections.singletonList(meterValue));

    wsClient.pushMessage(message);
  }

  /**
   * Create a MeterValue object with current readings.
   *
   * @param context The reason for taking a reading.
   */
  public MeterValue createMeterValue(ReadingContext context) {
    MeterValuesSampledData dataType = config.getMeterValuesSampledData();

    String value;
    MeterValuesSampledData measurand = dataType;
    if (dataType == MeterValuesSampledData.ENERGY_ACTIVE_IMPORT_REGISTER) measurand = null;
    UnitOfMeasure unit = null;

    switch (dataType) {
      case MeterValuesSampledData.CURRENT_OFFERED:
        value = String.valueOf(elec.getCurrentOffered());
        unit = UnitOfMeasure.A;
        break;

      case MeterValuesSampledData.CURRENT_IMPORT:
        value = String.valueOf(elec.getCurrentImport());
        unit = UnitOfMeasure.A;
        break;

      case MeterValuesSampledData.ENERGY_ACTIVE_IMPORT_REGISTER:
        value = String.valueOf(elec.getEnergyActiveImportRegister());
        unit = UnitOfMeasure.KWH;
        break;

      case MeterValuesSampledData.ENERGY_ACTIVE_IMPORT_INTERVAL:
        value =
            String.valueOf(
                elec.getEnergyActiveImportInterval(config.getMeterValueSampleInterval()));
        unit = UnitOfMeasure.KWH;
        break;

      case MeterValuesSampledData.POWER_ACTIVE_IMPORT:
        value = String.valueOf(elec.getPowerActiveImport());
        unit = UnitOfMeasure.KW;
        break;

      case MeterValuesSampledData.POWER_OFFERED:
        value = String.valueOf(elec.getPowerOffered());
        unit = UnitOfMeasure.KW;
        break;

      default:
        throw new UnsupportedOperationException(
            dataType.toString() + " not yet configured in MeterValueObserver.");
    }

    ZonedDateTime timestamp = wsClient.getScheduler().getTime().getSynchronizedTime();

    SampledValue sampledValue = new SampledValue(value, context, measurand, unit);
    return new MeterValue(timestamp, Collections.singletonList(sampledValue));
  }

  /**
   * When transitioning into a charging state, start sending periodic meter values messages. Stop
   * sending the messages when the simulator stops charging.
   *
   * @param newState the new ChargerState after a transition.
   */
  @Override
  public void onStateChanged(ChargerState newState) {
    MessageScheduler scheduler = wsClient.getScheduler();
    int interval = config.getMeterValueSampleInterval();

    if (newState == ChargerState.Charging) {
      meterTask =
          scheduler.periodicFunctionJob(
              interval,
              interval,
              TimeUnit.SECONDS,
              () -> sendMeterValues(ReadingContext.SAMPLE_PERIODIC));
    } else if (meterTask != null) {
      scheduler.killJob(meterTask);
      meterTask = null;
    }
  }
}
