package com.sim_backend.state;

import java.util.*;
import lombok.Getter;

/**
 * State machine for managing a simulated charger. The states here include OCPP protocol defined
 * states, in addition to other states that are relevant for charger operation.
 */
public class ChargerStateMachine {
  @Getter private ChargerState currentState;

  private List<StateObserver> observers = new ArrayList<>();
  private Map<ChargerState, Set<ChargerState>> validTransitions =
      Map.of(
          ChargerState.PoweredOff,
          Set.of(ChargerState.BootingUp),
          ChargerState.BootingUp,
          Set.of(ChargerState.Available, ChargerState.Faulted, ChargerState.Unavailable),
          ChargerState.Available,
          Set.of(ChargerState.Preparing, ChargerState.Faulted, ChargerState.Unavailable),
          ChargerState.Preparing,
          Set.of(ChargerState.Charging, ChargerState.Available, ChargerState.Faulted),
          ChargerState.Charging,
          Set.of(ChargerState.Available, ChargerState.Faulted),
          ChargerState.Faulted,
          Set.of(ChargerState.Available),
          ChargerState.Unavailable,
          Set.of(ChargerState.Available));

  /** Initializes the state machine in the PoweredOff state. */
  public ChargerStateMachine() {
    currentState = ChargerState.PoweredOff;
    notifyObservers();
  }

  /**
   * Initializes the state machine in the desired state.
   *
   * @param initialState the state to start in
   */
  public ChargerStateMachine(ChargerState initalState) {
    currentState = initalState;
    notifyObservers();
  }

  /**
   * Atomically checks that the current state equals the expected state and transitions to the new
   * state if valid
   *
   * @param expectedState the state you expect before transitioning
   * @param newState the desired state to transition to
   * @return true if the transition was successful; false otherwise.
   */
  public synchronized boolean checkAndTransition(
      ChargerState expectedState, ChargerState newState) {
    // Ensure the current state is as expected
    if (!currentState.equals(expectedState)) {
      return false;
    }

    // Check if the transition is valid
    if (newState == ChargerState.PoweredOff
        || (validTransitions
            .getOrDefault(currentState, Collections.emptySet())
            .contains(newState))) {
      currentState = newState;
      notifyObservers();
      return true;
    } else {
      // Transition is not valid from the current state
      return false;
    }
  }

  /**
   * Attempts to transition to a new state.
   *
   * @param newState the target state to transition to.
   * @throws IllegalStateException if the transition is invalid.
   */
  public synchronized void transition(ChargerState newState) {
    // Transitioning to PoweredOff is always allowed
    if (newState == ChargerState.PoweredOff
        || validTransitions.getOrDefault(currentState, null).contains(newState)) {
      currentState = newState;
      notifyObservers();
    } else {
      throw new IllegalStateException(
          "Invalid state transition from" + currentState + "to" + newState);
    }
  }

  /**
   * Adds a state observer.
   *
   * @param observer the state observer to add.
   */
  public void addObserver(StateObserver newObserver) {
    observers.add(newObserver);
  }

  /** Notifies all registered state observers about a state transition. */
  private void notifyObservers() {
    for (StateObserver observer : observers) {
      observer.onStateChanged(currentState);
    }
  }

  public boolean inTransaction() {
    return currentState.equals(ChargerState.Charging)
        || currentState.equals(ChargerState.Preparing);
  }
}
