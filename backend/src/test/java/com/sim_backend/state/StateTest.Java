package com.sim_backend.state;

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
  // Initialize test
  void testInit() throws Exception {
    try {
      if (SimulatorState.PowerOff != testStateMachine.getCurrentState()) {
        throw new Exception("Initialize StateMachine Test Failed");
      }
      System.out.println("Initalize StateMachine Test Passed");
    } catch (Exception e) {
      System.out.println("Error : " + e.getMessage());
    }
  }

  @Test
  // Booting test Valid Situation
  void testBooting() throws Exception {
    // Apply This code to see the Booting test Fail
    // testStateMachine.transition(SimulatorState.BootingUp);
    try {
      if (testStateMachine.getCurrentState() != SimulatorState.PowerOff) {
        throw new Exception("Booting Test Failed : Current State Is Not PowerOff");
      }
      if (!testStateMachine.transition(SimulatorState.BootingUp)) {
        throw new Exception("Booting Test Failed");
      }
      if (testStateMachine.getCurrentState() != SimulatorState.BootingUp) {
        throw new Exception("Poweroff to Booting Transition Failed : Transition Malfunction");
      }
      System.out.println("Booting Test Passed");
    } catch (Exception e) {
      System.out.println("Error : " + e.getMessage());
    }
  }

  @Test
  // Available test Valid Situation
  void testAvailable() throws Exception {
    // Delete This Line To See The Booting Test Fail
    testStateMachine.transition(SimulatorState.BootingUp);
    try {
      if (testStateMachine.getCurrentState() != SimulatorState.BootingUp) {
        throw new Exception("Booting Test Failed : Current State Is Not Bootingup");
      }
      if (!testStateMachine.transition(SimulatorState.Available)) {
        throw new Exception("Available Test Failed");
      }
      if (testStateMachine.getCurrentState() != SimulatorState.Available) {
        throw new Exception("Booting to Available Transition Test Failed : Transition Malfunction");
      }
      System.out.println("Available Test Passed");
    } catch (Exception e) {
      System.out.println("Error : " + e.getMessage());
    }
  }

  @Test
  // Poweroff test Valid Situation
  void testPowerOff() throws Exception {
    testStateMachine.transition(SimulatorState.BootingUp);
    // Delete This line To See the PowerOff Test Fail
    testStateMachine.transition(SimulatorState.Available);
    try {
      if (testStateMachine.getCurrentState() != SimulatorState.Available) {
        throw new Exception("PowerOff Test Failed : Current State Is Not Available");
      }
      if (!testStateMachine.transition(SimulatorState.PowerOff)) {
        throw new Exception("PowerOff Test Failed");
      }
      if (testStateMachine.getCurrentState() != SimulatorState.PowerOff) {
        throw new Exception("Poweroff to Booting Transition Test Failed : Transition Malfunction");
      }
      System.out.println("PowerOff Test Passed");
    } catch (Exception e) {
      System.out.println("Error : " + e.getMessage());
    }
  }
}
