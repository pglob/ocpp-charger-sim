package com.sim_backend.state;

import java.util.*;

public class IndicatorLogger implements StateIndicator {
  private SimulatorState lastState;
  private List<SimulatorState> stateHistory = new ArrayList<>();

  @Override
  public void onStateChanged(SimulatorState newState) {
    this.lastState = newState;
    this.stateHistory.add(newState);
    System.out.println("State Changed to : " + newState);
  }

  public SimulatorState getLastState() {
    return lastState;
  }

  public List<SimulatorState> getHistory() {
    return stateHistory;
  }
}
