package com.sim_backend.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StateTest {

  private SimulatorStateMachine testStateMachine;

  @BeforeEach
  void setUp() {
    testStateMachine = new SimulatorStateMachine();
  }

  @Test
  void testInit() {
    assertEquals(
        SimulatorState.PoweredOff,
        testStateMachine.getCurrentState(),
        "StateMachine should initialize to PoweredOff");
  }

  @Test
  void testTransition() {
    assertEquals(
        SimulatorState.PoweredOff,
        testStateMachine.getCurrentState(),
        "StateMachine should initialize to PoweredOff");
    testStateMachine.transition(SimulatorState.BootingUp);
    assertEquals(
        SimulatorState.BootingUp,
        testStateMachine.getCurrentState(),
        "Transition from PoweredOff to BootingUp failed");
    testStateMachine.transition(SimulatorState.Available);
    assertEquals(
        SimulatorState.Available,
        testStateMachine.getCurrentState(),
        "Transition from BootingUp to Available failed");
    testStateMachine.transition(SimulatorState.PoweredOff);
    assertEquals(
        SimulatorState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Transition from Available to PoweredOff failed");
  }

  @Test
  void testTransitionToPoweredOff() {
    // Transition from BootingUp to PoweredOff.
    testStateMachine.transition(SimulatorState.BootingUp);
    testStateMachine.transition(SimulatorState.PoweredOff);
    assertEquals(
        SimulatorState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Transition from BootingUp to PoweredOff should be allowed");

    // Transition from Available to PoweredOff.
    testStateMachine.transition(SimulatorState.BootingUp);
    testStateMachine.transition(SimulatorState.Available);
    testStateMachine.transition(SimulatorState.PoweredOff);
    assertEquals(
        SimulatorState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Transition from Available to PoweredOff should be allowed");

    // Transition from Preparing to PoweredOff.
    testStateMachine.transition(SimulatorState.BootingUp);
    testStateMachine.transition(SimulatorState.Available);
    testStateMachine.transition(SimulatorState.Preparing);
    testStateMachine.transition(SimulatorState.PoweredOff);
    assertEquals(
        SimulatorState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Transition from Preparing to PoweredOff should be allowed");

    // Transition from Charging to PoweredOff.
    testStateMachine.transition(SimulatorState.BootingUp);
    testStateMachine.transition(SimulatorState.Available);
    testStateMachine.transition(SimulatorState.Preparing);
    testStateMachine.transition(SimulatorState.Charging);
    testStateMachine.transition(SimulatorState.PoweredOff);
    assertEquals(
        SimulatorState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Transition from Charging to PoweredOff should be allowed");
  }
}
