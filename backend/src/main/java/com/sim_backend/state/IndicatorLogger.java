package com.sim_backend.state;

import java.util.*;
import lombok.Getter;

@Getter
public class IndicatorLogger implements StateIndicator {
  private SimulatorState lastState;
  private List<SimulatorState> history = new ArrayList<>();

  @Override
  public void onStateChanged(SimulatorState newState) {
    this.lastState = newState;
    this.history.add(newState);
    System.out.println("State Changed to : " + newState);
  }
}
