package com.sim_backend.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

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
        "StateMachine Should Initalize to PoweredOff");
  }

  @Test
  void testTransition() {
    assertEquals(
        SimulatorState.PoweredOff,
        testStateMachine.getCurrentState(),
        "StateMachine Should Initalize to PoweredOff");
    testStateMachine.transition(SimulatorState.BootingUp);
    assertEquals(
        SimulatorState.BootingUp,
        testStateMachine.getCurrentState(),
        "Current State is Not BootingUp : PoweredOff to BootingUp Failed");
    testStateMachine.transition(SimulatorState.Available);
    assertEquals(
        SimulatorState.Available,
        testStateMachine.getCurrentState(),
        "Current State is Not Available : BootingUp to Available Failed");
    testStateMachine.transition(SimulatorState.PoweredOff);
    assertEquals(
        SimulatorState.PoweredOff,
        testStateMachine.getCurrentState(),
        "Current State is Not PoweredOff : Available to PoweredOff Failed");
  }
}
