package com.sim_backend.websockets.observers;

import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.state.IllegalStateException;
import com.sim_backend.state.StateObserver;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.BootNotification;
import com.sim_backend.websockets.messages.BootNotificationResponse;
import com.sim_backend.websockets.messages.MessageValidator;
import java.util.concurrent.TimeUnit;

/**
 * Observer that handles BootNotification requests and responses as part of the OCPP protocol
 * communication with the Central System.
 */
public class BootNotificationObserver implements OnOCPPMessageListener, StateObserver {
  private OCPPWebSocketClient webSocketClient;
  private ChargerStateMachine stateMachine;

  public BootNotificationObserver(
      OCPPWebSocketClient webSocketClient, ChargerStateMachine stateMachine) {
    this.webSocketClient = webSocketClient;
    this.stateMachine = stateMachine;

    // Self register observer
    stateMachine.addObserver(this);
    webSocketClient.onReceiveMessage(BootNotificationResponse.class, this);
  }

  /**
   * Handles sending a BootNotification request to the Central System if the charger is in the
   * correct state.
   */
  public void handleBootNotificationRequest() {

    // Ensure current state is booting
    if (stateMachine.getCurrentState() == ChargerState.BootingUp) {
      BootNotification bootNotification = new BootNotification();

      // Ensure no constraint violations
      if (!MessageValidator.isValid(bootNotification)) {
        throw new IllegalArgumentException(MessageValidator.log_message(bootNotification));
      } else {
        webSocketClient.pushMessage(new BootNotification());
      }
    } else
      throw new IllegalStateException(
          "Invalid machine state to send a boot notification: " + stateMachine.getCurrentState());
  }

  /**
   * Processes incoming BootNotificationResponse messages and handles the response based on the
   * registration status provided by the Central System.
   *
   * @param message the received OCPP message, expected to be a BootNotificationResponse.
   * @throws ClassCastException if the message is not a BootNotificationResponse.
   */
  @Override
  public void onMessageReceived(OnOCPPMessage message) {

    if (!(message.getMessage() instanceof BootNotificationResponse response))
      throw new ClassCastException("Message is not a BootNotificationResponse");

    if (!MessageValidator.isValid(response)) {
      throw new IllegalArgumentException(MessageValidator.log_message(response));
    }

    MessageScheduler scheduler = message.getClient().getScheduler();
    long interval = response.getInterval();

    switch (response.getStatus()) {
      case ACCEPTED:
        // Registration successful, set heartbeat from interval, update state
        // and synchronize time to match the Central System.
        scheduler.setHeartbeatInterval(interval, TimeUnit.SECONDS);
        stateMachine.transition(ChargerState.Available);
        scheduler.synchronizeTime(response.getCurrentTime());
        break;

      case PENDING, REJECTED:
        // Central system is pending or rejected the request, set minimum wait time before next
        // BootNotification request
        if (interval < 0) {
          throw new IllegalArgumentException("Invalid heartbeat interval: " + interval);
        }

        if (interval == 0) {
          // Use default heartbeat interval if none given from central system.
          interval = MessageScheduler.getHEARTBEAT_INTERVAL();
        }

        scheduler.registerJob(interval, TimeUnit.SECONDS, new BootNotification());
        break;

      default:
        System.err.println("Unknown status received in BootNotificationResponse.");
        break;
    }
  }

  @Override
  public void onStateChanged(ChargerState newState) {
    if (newState == ChargerState.BootingUp) {
      handleBootNotificationRequest();
    }
  }
}
