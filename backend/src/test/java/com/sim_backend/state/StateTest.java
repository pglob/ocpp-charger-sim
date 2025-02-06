package com.sim_backend.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StateTest {

  private ChargerStateMachine testStateMachine;

  @BeforeEach
  void setUp() {
    testStateMachine = new ChargerStateMachine();
  }

  @Test
  void testInit() {
    assertEquals(
        ChargerState.PoweredOff,
        testStateMachine.getCurrentState(),
        "StateMachine should initialize to PoweredOff");
  }

  @Test
  void testTransition() {
    assertEquals(
        ChargerState.PoweredOff,
        testStateMachine.getCurrentState(),
        "StateMachine should initialize to PoweredOff");
    testStateMachine.transition(ChargerState.BootingUp);
    assertEquals(
        ChargerState.BootingUp,
        testStateMachine.getCurrentState(),
        "Transition from PoweredOff to BootingUp failed");
    testStateMachine.transition(ChargerState.Available);
    assertEquals(
        ChargerState.Available,
        testStateMachine.getCurrentState(),
        "Transition from BootingUp to Available failed");
    testStateMachine.transition(ChargerState.PoweredOff);
    assertEquals(
        ChargerState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Transition from Available to PoweredOff failed");
  }

  @Test
  void testTransitionToPoweredOff() {
    // Transition from BootingUp to PoweredOff.
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.PoweredOff);
    assertEquals(
        ChargerState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Transition from BootingUp to PoweredOff should be allowed");

    // Transition from Available to PoweredOff.
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    testStateMachine.transition(ChargerState.PoweredOff);
    assertEquals(
        ChargerState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Transition from Available to PoweredOff should be allowed");

    // Transition from Preparing to PoweredOff.
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    testStateMachine.transition(ChargerState.Preparing);
    testStateMachine.transition(ChargerState.PoweredOff);
    assertEquals(
        ChargerState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Transition from Preparing to PoweredOff should be allowed");

    // Transition from Charging to PoweredOff.
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    testStateMachine.transition(ChargerState.Preparing);
    testStateMachine.transition(ChargerState.Charging);
    testStateMachine.transition(ChargerState.PoweredOff);
    assertEquals(
        ChargerState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Transition from Charging to PoweredOff should be allowed");
  }
}
