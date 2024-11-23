package com.sim_backend.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    assertEquals(
        SimulatorState.PowerOff,
        testIndicator.getLastState(),
        "Last State in Indicator is Not PowerOff : Indicator Initalize Failed");
  }

  @Test
  void testLog() {
    testStateMachine.transition(SimulatorState.BootingUp);
    assertEquals(
        testStateMachine.getCurrentState(),
        testIndicator.getLastState(),
        "Indicator Failed to Log PowerOff to Bootingup");
    testStateMachine.transition(SimulatorState.Available);
    assertEquals(
        testStateMachine.getCurrentState(),
        testIndicator.getLastState(),
        "Indicator Failed to Log BootingUp to Available");
    testStateMachine.transition(SimulatorState.PowerOff);
    assertEquals(
        testStateMachine.getCurrentState(),
        testIndicator.getLastState(),
        "Indicator Failed to Log Available to PowerOff");

    List<SimulatorState> expectedResult =
        List.of(
            SimulatorState.PowerOff,
            SimulatorState.BootingUp,
            SimulatorState.Available,
            SimulatorState.PowerOff);

    assertEquals(
        testIndicator.getHistory(),
        expectedResult,
        "Indicator Failed to Log Available to PowerOff");
  }
}
