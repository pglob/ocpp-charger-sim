package com.sim_backend.state;

import java.util.*;

public class SimulatorStateMachine {
  private SimulatorState currentState;
  private Map<SimulatorState, Set<SimulatorState>> validTransitions = new HashMap<>();
  private List<StateIndicator> indicators = new ArrayList<>();

  // Start from Poweroff
  public SimulatorStateMachine() {
    currentState = SimulatorState.PowerOff;
    initValidation();
    notifyIndicator();
  }

  // If there is an indicator begin with
  public SimulatorStateMachine(StateIndicator initalIndicator) {
    addIndicator(initalIndicator);
    currentState = SimulatorState.PowerOff;
    initValidation();
    notifyIndicator();
  }

  // define which state can be used for a certain state
  public void initValidation() {
    validTransitions =
        Map.of(
            SimulatorState.PowerOff,
            Set.of(SimulatorState.BootingUp),
            SimulatorState.BootingUp,
            Set.of(SimulatorState.Available),
            SimulatorState.Available,
            Set.of(SimulatorState.PowerOff));
  }

  // State Transition
  public void transition(SimulatorState userInput) {
    if (validTransitions.getOrDefault(currentState, null).contains(userInput)) {
      currentState = userInput;
      notifyIndicator();
    } else {
      throw new IllegalStateException(
          "Invalid state transition from" + currentState + "to" + userInput);
    }
  }

  public void addIndicator(StateIndicator indicator) {
    indicators.add(indicator);
  }

  // Popup Message State Change
  private void notifyIndicator() {
    for (StateIndicator indicator : indicators) {
      indicator.onStateChanged(currentState);
    }
  }

  // Getter
  public SimulatorState getCurrentState() {
    return currentState;
  }
}
