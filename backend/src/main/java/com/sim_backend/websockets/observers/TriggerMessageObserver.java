package com.sim_backend.websockets.observers;

import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.enums.MessageTrigger;
import com.sim_backend.websockets.enums.TriggerMessageStatus;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.BootNotification;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.StatusNotification;
import com.sim_backend.websockets.messages.TriggerMessage;
import com.sim_backend.websockets.messages.TriggerMessageResponse;
import java.time.ZonedDateTime;

public class TriggerMessageObserver implements OnOCPPMessageListener {

  private final OCPPWebSocketClient webSocketClient;
  private final ChargerStateMachine stateMachine;

  public TriggerMessageObserver(
      OCPPWebSocketClient webSocketClient, ChargerStateMachine stateMachine) {
    this.webSocketClient = webSocketClient;
    this.stateMachine = stateMachine;
    webSocketClient.onReceiveMessage(TriggerMessage.class, this);
  }

  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    if (!(message.getMessage() instanceof TriggerMessage triggerMessage)) {
      throw new ClassCastException("Received message is not a TriggerMessage");
    }

    TriggerMessageStatus responseStatus = TriggerMessageStatus.Accepted;
    Runnable triggeredAction = null;
    MessageTrigger requested = triggerMessage.getRequestedMessage();

    ChargerState currentState = stateMachine.getCurrentState();
    if (currentState == ChargerState.PoweredOff || currentState == ChargerState.BootingUp) {
      responseStatus = TriggerMessageStatus.Rejected;
    }
    Integer connectorId = triggerMessage.getConnectorId();
    if (responseStatus == TriggerMessageStatus.Accepted) {
      if (connectorId != null && connectorId != 0 && connectorId != 1) {
        responseStatus = TriggerMessageStatus.Rejected;
      }
    }
    if (responseStatus == TriggerMessageStatus.Accepted) {
      switch (requested) {
        case BootNotification:
          if (currentState != ChargerState.BootingUp) {
            responseStatus = TriggerMessageStatus.Rejected;
          } else {
            triggeredAction =
                () -> {
                  new BootNotificationObserver(webSocketClient, stateMachine)
                      .handleBootNotificationRequest();
                };
          }
          break;

        case DiagnosticsStatusNotification:
          responseStatus = TriggerMessageStatus.NotImplemented;
          break;

        case FirmwareStatusNotification:
          responseStatus = TriggerMessageStatus.NotImplemented;
          break;

        case Heartbeat:
          triggeredAction = () -> webSocketClient.pushMessage(new Heartbeat());
          break;

        case MeterValues:
          // TODO implement MeterValues trigger action here
          responseStatus = TriggerMessageStatus.NotImplemented;
          break;

        case StatusNotification:
          triggeredAction =
              () -> {
                StatusNotification statusNotification =
                    createStatusNotification(connectorId == null ? 0 : connectorId);
                webSocketClient.pushMessage(statusNotification);
              };
          break;

        default:
          responseStatus = TriggerMessageStatus.NotImplemented;
          break;
      }
    }
    TriggerMessageResponse response = new TriggerMessageResponse(triggerMessage, responseStatus);
    message.getClient().pushMessage(response);

    if (triggeredAction != null) {
      triggeredAction.run();
    }
  }

  private StatusNotification createStatusNotification(Integer connectorId) {
    if (connectorId == null) {
      connectorId = 0;
    }
    ChargePointErrorCode errorCode = ChargePointErrorCode.NoError;
    String info = "";
    ChargePointStatus status = mapStateToChargePointStatus(stateMachine.getCurrentState());
    ZonedDateTime timestamp = webSocketClient.getScheduler().getTime().getSynchronizedTime();
    String vendorId = "";
    String vendorErrorCode = "";
    return new StatusNotification(
        connectorId, errorCode, info, status, timestamp, vendorId, vendorErrorCode);
  }

  private ChargePointStatus mapStateToChargePointStatus(ChargerState state) {
    switch (state) {
      case Available:
        return ChargePointStatus.Available;
      case BootingUp:
        return ChargePointStatus.Preparing;
      case Preparing:
        return ChargePointStatus.Preparing;
      case Charging:
        return ChargePointStatus.Charging;
      case PoweredOff:
        return ChargePointStatus.Unavailable;
      default:
        return ChargePointStatus.Available;
    }
  }
}
