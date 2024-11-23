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
        SimulatorState.PowerOff,
        testStateMachine.getCurrentState(),
        "StateMachine Should Initalize to PowerOff");
  }

  @Test
  void testTransition() {
    assertEquals(
        SimulatorState.PowerOff,
        testStateMachine.getCurrentState(),
        "StateMachine Should Initalize to PowerOff");
    testStateMachine.transition(SimulatorState.BootingUp);
    assertEquals(
        SimulatorState.BootingUp,
        testStateMachine.getCurrentState(),
        "Current State is Not BootingUp : Poweroff to BootingUp Failed");
    testStateMachine.transition(SimulatorState.Available);
    assertEquals(
        SimulatorState.Available,
        testStateMachine.getCurrentState(),
        "Current State is Not Available : BootingUp to Available Failed");
    testStateMachine.transition(SimulatorState.PowerOff);
    assertEquals(
        SimulatorState.PowerOff,
        testStateMachine.getCurrentState(),
        "Current State is Not PowerOff : Available to PowerOff Failed");
  }
}
