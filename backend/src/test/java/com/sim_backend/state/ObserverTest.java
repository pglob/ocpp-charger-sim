package com.sim_backend.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObserverTest {
  private ChargerStateMachine testStateMachine;
  private StateLogger testObserver;

  @BeforeEach
  void setUp() {
    testObserver = new StateLogger();
    testStateMachine = new ChargerStateMachine();
    testStateMachine.addObserver(testObserver);
  }

  @Test
  void testInit() {
    assertEquals(null, testObserver.getLastState(), "Last State in Observer is not null");
  }

  @Test
  void testLog() {
    testStateMachine.transition(ChargerState.BootingUp);
    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log PoweredOff to Bootingup");

    testStateMachine.transition(ChargerState.Available);
    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log BootingUp to Available");

    testStateMachine.transition(ChargerState.Preparing);
    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log Available to Prepraring");

    testStateMachine.transition(ChargerState.Charging);
    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log Preparing to Charging");

    testStateMachine.transition(ChargerState.Available);
    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log Charging to Available");

    testStateMachine.transition(ChargerState.Preparing);
    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log Available to Prepraring");
    testStateMachine.transition(ChargerState.Available);

    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log Charging to Available");
    testStateMachine.transition(ChargerState.PoweredOff);

    assertEquals(
        testStateMachine.getCurrentState(),
        testObserver.getLastState(),
        "Observer Failed to Log Available to PoweredOff");

    List<ChargerState> expectedResult =
        List.of(
            ChargerState.BootingUp,
            ChargerState.Available,
            ChargerState.Preparing,
            ChargerState.Charging,
            ChargerState.Available,
            ChargerState.Preparing,
            ChargerState.Available,
            ChargerState.PoweredOff);

    assertEquals(
        testObserver.getHistory(),
        expectedResult,
        "Observer Failed to Log Available to PoweredOff");
  }
}
