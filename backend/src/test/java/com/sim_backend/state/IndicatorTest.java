package com.sim_backend.state;

import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class IndicatorTest {
  private SimulatorStateMachine testStateMachine;
  private IndicatorLogger testIndicator;

  @BeforeEach
  void setUp() {
    testIndicator = new IndicatorLogger();
    testStateMachine = new SimulatorStateMachine(testIndicator);
  }

  @Test
  void testInit() {
    try {
      if (SimulatorState.PowerOff != testIndicator.getLastState()) {
        throw new Exception("Initialize Indicator Test Failed");
      }
      System.out.println("Initalize Indicator Test Passed");
    } catch (Exception e) {
      System.out.println("Error : " + e.getMessage());
    }
  }

  @Test
  void testLog() {
    try {
      testStateMachine.transition(SimulatorState.BootingUp);
      if (testStateMachine.getCurrentState() != testIndicator.getLastState()) {
        throw new Exception("Indicator Failed to Log PowerOff to Bootingup");
      }
      testStateMachine.transition(SimulatorState.Available);
      if (testStateMachine.getCurrentState() != testIndicator.getLastState()) {
        throw new Exception("Indicator Failed to Log Bootingup to Available");
      }
      testStateMachine.transition(SimulatorState.PowerOff);
      if (testStateMachine.getCurrentState() != testIndicator.getLastState()) {
        throw new Exception("Indicator Failed to Log Available to PowerOff");
      }

      List<SimulatorState> expectedResult =
          List.of(
              SimulatorState.PowerOff,
              SimulatorState.BootingUp,
              SimulatorState.Available,
              SimulatorState.PowerOff);

      if (!testIndicator.getHistory().equals(expectedResult)) {
        throw new Exception("Log Changes Failed");
      }
      System.out.println("Indicator Log Test Passed");
    } catch (Exception e) {
      System.out.println("Error : " + e.getMessage());
    }
  }
}
