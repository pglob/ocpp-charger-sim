package com.sim_backend.websockets.observers;

import com.sim_backend.state.IllegalStateException;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.state.StateIndicator;
import com.sim_backend.websockets.MessageScheduler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.BootNotification;
import com.sim_backend.websockets.messages.BootNotificationResponse;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;

/**
 * Observer that handles BootNotification requests and responses as part of the OCPP protocol
 * communication with the Central System.
 */
@AllArgsConstructor
public class BootNotificationObserver implements OnOCPPMessageListener, StateIndicator {
  private OCPPWebSocketClient webSocketClient;
  private SimulatorStateMachine currState;

  /**
   * Handles sending a BootNotification request to the Central System if the simulator is in the
   * correct state.
   */
  public void handleBootNotificationRequest() {

    // Ensure current state is booting
    if (currState.getCurrentState() == SimulatorState.BootingUp) {
      webSocketClient.pushMessage(new BootNotification());
    } else
      throw new IllegalStateException(
          "Invalid machine state to send a boot notification: " + currState.getCurrentState());
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
    MessageScheduler scheduler = message.getClient().getScheduler();
    long interval = response.getInterval();

    switch (response.getStatus()) {
      case ACCEPTED:
        // Registration successful, set heartbeat from interval
        scheduler.setHeartbeatInterval(interval, TimeUnit.SECONDS);
        currState.transition(SimulatorState.Available);

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
  public void onStateChanged(SimulatorState newState) {
    if (newState == SimulatorState.BootingUp) {
      handleBootNotificationRequest();
    }
  }
}
