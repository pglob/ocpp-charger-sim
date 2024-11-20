package com.sim_backend.state;

import java.util.*;

public class SimulatorStateMachine {
    private SimulatorState currentState;
    private Map<SimulatorState, Set<SimulatorState>> validTransition = new HashMap<>();
    private List<StateIndicator> indicators = new ArrayList<>();

    // Start from Poweroff
    public SimulatorStateMachine() {
        currentState = SimulatorState.PowerOff;
        initValidation();
        notifyIndicator();
    }

    // define which state can be used for a certain state
    public void initValidation() {
        validTransition = 
        Map.of(SimulatorState.PowerOff, Set.of(SimulatorState.BootingUp),
        SimulatorState.BootingUp, Set.of(SimulatorState.Available),
        SimulatorState.Available, Set.of(SimulatorState.PowerOff));
    }

    // State Transition
    public boolean transition(SimulatorState userInput){
        try {
            if(validTransition.getOrDefault(currentState, null).contains(userInput)){
                currentState = userInput;
                notifyIndicator();
                return true;
            }
            else{
                throw new IllegalStateException("Invalid state transition from" + userInput + "to" + currentState);
        }
    } catch (IllegalStateException e) {
        System.out.println(e.getMessage());
        return false;
    }
}

    // Popup Message State Change
    private void notifyIndicator() {
        for(StateIndicator indicator : indicators){
            indicator.onStateChanged(currentState);
        }
    }

    // Getter
    public SimulatorState getCurrentState() {
        return currentState;
    }
}