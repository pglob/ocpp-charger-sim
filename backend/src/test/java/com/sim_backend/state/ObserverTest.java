package com.sim_backend.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObserverTest {
  private SimulatorStateMachine testStateMachine;
  private StateLogger testObserver;

  @BeforeEach
  void setUp() {
    testObserver = new StateLogger();
    testStateMachine = new SimulatorStateMachine();
    testStateMachine.addObserver(testObserver);
  }

  @Test
  void testInit() {
    assertEquals(
        null,
        testObserver.getLastState(),
        "Last State in Observer is not null");
  }

  @Test
  void testLog() {
    testStateMachine.transition(SimulatorState.BootingUp);
    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log PoweredOff to Bootingup");
    testStateMachine.transition(SimulatorState.Available);
    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log BootingUp to Available");
    testStateMachine.transition(SimulatorState.PoweredOff);
    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log Available to PoweredOff");

    List<SimulatorState> expectedResult =
        List.of(
            SimulatorState.BootingUp,
            SimulatorState.Available,
            SimulatorState.PoweredOff);

    assertEquals(
        testObserver.getHistory(),
        expectedResult,
        "Observer Failed to Log Available to PoweredOff");
  }
}
