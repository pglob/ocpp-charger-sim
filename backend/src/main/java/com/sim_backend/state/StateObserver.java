package com.sim_backend.state;

/**
 * Interface for observing state transitions. Implementing classes will be notified of state
 * transitions.
 */
public interface StateObserver {

  /**
   * Called when the simulator's state transitions.
   *
   * @param newState the new state after the change.
   */
  void onStateChanged(SimulatorState newState);
}
