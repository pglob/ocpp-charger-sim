package com.sim_backend.transactions;

import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.simulator.Simulator;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.messages.Authorize;
import com.sim_backend.websockets.messages.AuthorizeResponse;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

/**
 * Manages transaction operations for the Simulator, including the initiation of start and stop
 * charging processes. This handler coordinates the authorization process before starting or
 * stopping a transaction
 */
@Getter
public class TransactionHandler {
  /** Handler for initiating a StartTransaction operation */
  private StartTransactionHandler startHandler;

  /** Handler for initiating a StopTransaction operation */
  private StopTransactionHandler stopHandler;

  /** The state machine managing the current state of the Simulator */
  private SimulatorStateMachine stateMachine;

  /** The WebSocket client used for communication with the central system */
  private OCPPWebSocketClient client;

  /** The ElectricalTransition instance tracking charging parameters */
  private ElectricalTransition elec;

  /** The simulator's current idTa */
  private String idTag;

  /** The current transaction id */
  private AtomicInteger transactionId;

  /** Flag to indicate if a start transaction is in progress */
  private final AtomicBoolean startInProgress = new AtomicBoolean(false);

  /** Flag to indicate if a stop transaction is in progress */
  private final AtomicBoolean stopInProgress = new AtomicBoolean(false);

  /**
   * Creates a new TransactionHandler for the given Simulator instance
   *
   * @param sim the Simulator this TransactionHandler belongs to
   */
  public TransactionHandler(Simulator sim) {
    stateMachine = sim.getStateMachine();
    client = sim.getWsClient();
    elec = sim.getElec();
    startHandler = new StartTransactionHandler(stateMachine, client);
    stopHandler = new StopTransactionHandler(stateMachine, client);
    idTag = null;
    transactionId = new AtomicInteger(-1);
  }

  /**
   * Performs pre-authorization before starting or stopping a transaction
   *
   * <p>If the authorization is accepted, this method initiates either the start or stop transaction
   * process based on the current state of the simulator. If the authorization fails, it resets the
   * transaction flags and transitions the state machine back to Available
   *
   * @param connectorId the identifier of the connector to be used
   * @param idTag the user identification tag
   */
  public void preAuthorize(int connectorId, String idTag) {
    Authorize authorizeMessage = new Authorize(idTag);
    client.pushMessage(authorizeMessage);

    client.onReceiveMessage(
        AuthorizeResponse.class,
        message -> {
          if (!(message.getMessage() instanceof AuthorizeResponse response)) {
            throw new ClassCastException("Message is not an AuthorizeResponse");
          }

          if (response.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED) {
            System.out.println("Authorization Accepted...");
            System.out.println("Proceeding with Transaction...");
            if (stateMachine.getCurrentState() == SimulatorState.Preparing) {
              startHandler.initiateStartTransaction(
                  connectorId, idTag, transactionId, elec, startInProgress);
              this.idTag = idTag;
            } else if (stateMachine.getCurrentState() == SimulatorState.Charging) {
              stopHandler.initiateStopTransaction(transactionId.get(), idTag, elec, stopInProgress);
            } else {
              System.err.println(
                  "Invalid State Detected... Current State: " + stateMachine.getCurrentState());
            }
          } else {
            System.err.println(
                "Authorize Denied... Status: " + response.getIdTagInfo().getStatus());
            startInProgress.set(false);
            stopInProgress.set(false);
            stateMachine.transition(SimulatorState.Available);
          }
          client.clearOnReceiveMessage(AuthorizeResponse.class);
        });
  }

  /**
   * Starts a charging session
   *
   * <p>If the simulator is in the Available state and no start transaction is already in progress,
   * the state is first transitioned to Preparing, and then the pre-authorization process is
   * initiated. If the simulator is not in the Available state or a start is already in progress,
   * this method does nothing
   *
   * @param connectorId the identifier of the connector to be used
   * @param idTag the user identification tag
   */
  public void StartCharging(int connectorId, String idTag) {
    if (stateMachine.getCurrentState() != SimulatorState.Available || startInProgress.get()) {
      return;
    }

    // Attempt to set the startInProgress flag.
    if (!startInProgress.compareAndSet(false, true)) {
      return;
    }

    stateMachine.transition(SimulatorState.Preparing);
    preAuthorize(connectorId, idTag);
  }

  /**
   * Stops a charging session
   *
   * <p>If the simulator is in the Charging state and no stop transaction is already in progress,
   * this method will either directly stop the charging process if the provided idTag matches the
   * current authorized idTag, or it will initiate pre-authorization if the idTags differ.
   *
   * @param idTag the user identification tag
   */
  public void StopCharging(String idTag) {
    if (stateMachine.getCurrentState() != SimulatorState.Charging || stopInProgress.get()) {
      return;
    }

    // Attempt to set the stopInProgress flag
    if (!stopInProgress.compareAndSet(false, true)) {
      return;
    }

    if (idTag == null || this.idTag.equals(idTag)) {
      stopHandler.initiateStopTransaction(transactionId.get(), idTag, elec, stopInProgress);
    } else {
      preAuthorize(-1, idTag);
    }

    transactionId.set(-1);
  }
}
