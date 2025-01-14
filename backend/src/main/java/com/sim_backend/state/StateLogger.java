package com.sim_backend.state;

import java.util.*;
import lombok.Getter;

/**
 * A logger implementation of the StateObserver interface.
 * Tracks state transitions in the simulator.
 */
@Getter
public class StateLogger implements StateObserver {
    private SimulatorState lastState = null;
    private final List<SimulatorState> history = new ArrayList<>();

    /**
     * Log state transitions when they occur.
     *
     * @param newState the new state after the transition.
     */
    @Override
    public void onStateChanged(SimulatorState newState) {
        this.lastState = newState;
        this.history.add(newState);
        System.out.println("State Changed to : " + newState);
      }
}
