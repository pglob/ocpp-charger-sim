package com.sim_backend.state;

public interface StateIndicator {
  void onStateChanged(SimulatorState newState);
}
