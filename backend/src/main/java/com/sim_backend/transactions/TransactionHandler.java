package com.sim_backend.transactions;

import com.sim_backend.charger.Charger;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.enums.Reason;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.Authorize;
import com.sim_backend.websockets.messages.AuthorizeResponse;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

/**
 * Manages transaction operations for the Charger, including the initiation of start and stop
 * charging processes. This handler coordinates the authorization process before starting or
 * stopping a transaction
 */
@Getter
public class TransactionHandler {
  /** Handler for initiating a StartTransaction operation */
  private StartTransactionHandler startHandler;

  /** Handler for initiating a StopTransaction operation */
  private StopTransactionHandler stopHandler;

  /** The state machine managing the current state of the charger */
  private ChargerStateMachine stateMachine;

  /** The WebSocket client used for communication with the central system */
  private OCPPWebSocketClient client;

  /** The ElectricalTransition instance tracking charging parameters */
  private ElectricalTransition elec;

  /** The charger's current idTag */
  private String idTag;

  /** The current transaction id */
  private AtomicInteger transactionId;

  /** Flag to indicate if a start transaction is in progress */
  private final AtomicBoolean startInProgress = new AtomicBoolean(false);

  /** Flag to indicate if a stop transaction is in progress */
  private final AtomicBoolean stopInProgress = new AtomicBoolean(false);

  /**
   * Creates a new TransactionHandler for the given Charger instance
   *
   * @param charger the Charger this TransactionHandler belongs to
   */
  public TransactionHandler(Charger charger) {
    stateMachine = charger.getStateMachine();
    client = charger.getWsClient();
    elec = charger.getElec();
    startHandler = new StartTransactionHandler(stateMachine, client);
    stopHandler = new StopTransactionHandler(stateMachine, client);
    idTag = null;
    transactionId = new AtomicInteger(-1);
  }

  /**
   * Performs pre-authorization before starting or stopping a transaction
   *
   * <p>If the authorization is accepted, this method initiates either the start or stop transaction
   * process based on the current state of the charger. If the authorization fails, it resets the
   * transaction flags and transitions the state machine back to Available
   *
   * @param connectorId the identifier of the connector to be used
   * @param idTag the user identification tag
   * @param reason the reason for stopping the charging session. Leave null if not applicable
   */
  public void preAuthorize(int connectorId, String idTag, Reason reason) {
    Authorize authorizeMessage = new Authorize(idTag);
    client.pushMessage(authorizeMessage);

    final OnOCPPMessageListener listener =
        new OnOCPPMessageListener() {
          @Override
          public void onMessageReceived(OnOCPPMessage message) {
            client.deleteOnReceiveMessage(AuthorizeResponse.class, this);
            if (!(message.getMessage() instanceof AuthorizeResponse response)) {
              throw new ClassCastException("Message is not an AuthorizeResponse");
            }
            if (response.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED) {
              System.out.println("Authorization Accepted...");
              System.out.println("Proceeding with Transaction...");
              if (stateMachine.getCurrentState() == ChargerState.Preparing) {
                startHandler.initiateStartTransaction(
                    connectorId, idTag, transactionId, elec, startInProgress);
                TransactionHandler.this.idTag = idTag;
              } else if (stateMachine.getCurrentState() == ChargerState.Charging) {
                stopHandler.initiateStopTransaction(
                    idTag, reason, transactionId, elec, stopInProgress);
              } else {
                System.err.println(
                    "Invalid State Detected... Current State: " + stateMachine.getCurrentState());
                startInProgress.set(false);
                stopInProgress.set(false);
              }
            } else {
              System.err.println(
                  "Authorize Denied... Status: " + response.getIdTagInfo().getStatus());
              startInProgress.set(false);
              stopInProgress.set(false);
              // If charging, do not stop charging on an authorization failure
              stateMachine.checkAndTransition(ChargerState.Preparing, ChargerState.Available);
            }
          }

          @Override
          public void onTimeout() {
            client.deleteOnReceiveMessage(AuthorizeResponse.class, this);
            System.err.println("Authorization timeout. Resetting transaction state.");
            startInProgress.set(false);
            stopInProgress.set(false);
            stateMachine.checkAndTransition(ChargerState.Preparing, ChargerState.Available);
          }
        };

    client.onReceiveMessage(AuthorizeResponse.class, listener);
  }

  /**
   * Starts a charging session
   *
   * <p>If the charger is in the Available state and no start transaction is already in progress,
   * the state is first transitioned to Preparing, and then the pre-authorization process is
   * initiated. If the charger is not in the Available state or a start is already in progress, this
   * method does nothing
   *
   * @param connectorId the identifier of the connector to be used
   * @param idTag the user identification tag
   */
  public void startCharging(int connectorId, String idTag) {
    if (stateMachine.getCurrentState() != ChargerState.Available || startInProgress.get()) {
      return;
    }

    // Attempt to set the startInProgress flag.
    if (!startInProgress.compareAndSet(false, true)) {
      return;
    }

    if (stateMachine.checkAndTransition(ChargerState.Available, ChargerState.Preparing)) {
      preAuthorize(connectorId, idTag, null);
    } else {
      startInProgress.set(false);
    }
  }

  /**
   * Stops a charging session
   *
   * <p>If the charger is in the Charging state and no stop transaction is already in progress, this
   * method will either directly stop the charging process if the provided idTag matches the current
   * authorized idTag, or it will initiate pre-authorization if the idTags differ.
   *
   * @param idTag the user identification tag
   * @param reason the reason for stopping the charging session (can be null if Local)
   */
  public void stopCharging(String idTag, Reason reason) {
    if (stateMachine.getCurrentState() != ChargerState.Charging || stopInProgress.get()) {
      return;
    }

    // Attempt to set the stopInProgress flag
    if (!stopInProgress.compareAndSet(false, true)) {
      return;
    }

    if (idTag == null || this.idTag.equals(idTag)) {
      stopHandler.initiateStopTransaction(idTag, reason, transactionId, elec, stopInProgress);
    } else {
      preAuthorize(-1, idTag, reason);
    }
  }

  /**
   * Stops a charging session without an idTag (due to error or reboot)
   *
   * <p>If the charger is in the Charging state and no stop transaction is already in progress, this
   * method will directly stop the charging process without authorization.
   *
   * @param reason the reason for stopping the charging session
   */
  public void forceStopCharging(Reason reason) {
    if ((stateMachine.getCurrentState() != ChargerState.Charging
                && stateMachine.getCurrentState() != ChargerState.Faulted
            || transactionId.get() == -1)
        || stopInProgress.get()) {
      return;
    }

    // Attempt to set the stopInProgress flag
    if (!stopInProgress.compareAndSet(false, true)) {
      return;
    }

    stopHandler.initiateStopTransaction(null, reason, transactionId, elec, stopInProgress);
  }
}
