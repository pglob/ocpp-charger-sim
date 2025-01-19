package com.sim_backend.state;

import java.util.*;
import lombok.Getter;

/**
 * State machine for managing a simulated charger. The states here include OCPP protocol defined
 * states, in addition to other states that are relevant for charger operation.
 */
public class SimulatorStateMachine {
  @Getter private SimulatorState currentState;

  private List<StateObserver> observers = new ArrayList<>();
  private Map<SimulatorState, Set<SimulatorState>> validTransitions =
      Map.of(
          SimulatorState.PoweredOff, Set.of(SimulatorState.BootingUp),
          SimulatorState.BootingUp, Set.of(SimulatorState.Available),
          SimulatorState.Available, Set.of(SimulatorState.Preparing, SimulatorState.PoweredOff),
          SimulatorState.Preparing, Set.of(SimulatorState.Charging, SimulatorState.Available),
          SimulatorState.Charging, Set.of(SimulatorState.Preparing, SimulatorState.Available));

  /** Initializes the state machine in the PoweredOff state. */
  public SimulatorStateMachine() {
    currentState = SimulatorState.PoweredOff;
    notifyObservers();
  }

  /**
   * Initializes the state machine in the desired state.
   *
   * @param initialState the state to start in
   */
  public SimulatorStateMachine(SimulatorState initalState) {
    currentState = initalState;
    notifyObservers();
  }

  /**
   * Attempts to transition to a new state.
   *
   * @param newState the target state to transition to.
   * @throws IllegalStateException if the transition is invalid.
   */
  public void transition(SimulatorState newState) {
    if (validTransitions.getOrDefault(currentState, null).contains(newState)) {
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
}
