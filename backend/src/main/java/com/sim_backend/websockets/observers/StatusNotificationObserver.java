package com.sim_backend.websockets.observers;

import com.sim_backend.state.ChargerState;
import com.sim_backend.state.StateObserver;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.messages.StatusNotification;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;

/** Observer that handles StatusNotification requests when charger state changes. */
@AllArgsConstructor
public class StatusNotificationObserver implements StateObserver {

  private final OCPPWebSocketClient client;
  private List<StatusNotification> statusNotificationQueue = new ArrayList<>();

  /**
   * This sends a StatusNotification when State changes. If the client is offline it queues, when
   * client is back online the last StatusNotification message to be sent.
   *
   * @param newState the new ChargerState after a transition.
   */
  @Override
  public void onStateChanged(ChargerState newState) {
    ChargePointStatus status = ChargerStateToStatus(newState);

    if (status == null) {
      return;
    }
    ZonedDateTime timestamp = client.getScheduler().getTime().getSynchronizedTime();
    sendStatusNotification(1, ChargePointErrorCode.NoError, "", status, timestamp, "", "");
  }

  /** Manual StatusNotification Request function */
  public void sendStatusNotification(
      int connectorId,
      ChargePointErrorCode errorCode,
      String info,
      ChargePointStatus status,
      ZonedDateTime timestamp,
      String vendorId,
      String vendorErrorCode) {
    StatusNotification notification =
        new StatusNotification(
            connectorId, errorCode, info, status, timestamp, vendorId, vendorErrorCode);

    if (client.isOnline()) {
      client.pushMessage(notification);
    } else {
      System.out.println("Client is Offine... Failed to Send StatusNotification");
      statusNotificationQueue.add(notification);
    }
  }

  /**
   * ChargerState to ChargePointStatus.
   *
   * @param state ChargerState.
   * @return ChargePointStatus.
   */
  private ChargePointStatus ChargerStateToStatus(ChargerState state) {
    return switch (state) {
      case ChargerState.Available -> ChargePointStatus.Available;
      case ChargerState.Preparing -> ChargePointStatus.Preparing;
      case ChargerState.Charging -> ChargePointStatus.Charging;
      default -> null;
    };
  }

  /** Called in client goOline() function. */
  public void onClientGoOnline() {
    if (!statusNotificationQueue.isEmpty()) {
      System.out.println("Client is back online. Send latest StatusNotification.");
      client.pushMessage(statusNotificationQueue.get(statusNotificationQueue.size() - 1));
      statusNotificationQueue.clear();
    }
  }
}
