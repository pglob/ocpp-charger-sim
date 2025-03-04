package com.sim_backend.state;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  void testTransitionToFaulted() {
    // Transition from Available to Faulted
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    testStateMachine.transition(ChargerState.Faulted);
    assertEquals(
        ChargerState.Faulted,
        testStateMachine.getCurrentState(),
        "Transition from Available to Faulted should be allowed");

    // Transition from Preparing to Faulted
    testStateMachine.transition(ChargerState.PoweredOff);
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    testStateMachine.transition(ChargerState.Preparing);
    testStateMachine.transition(ChargerState.Faulted);
    assertEquals(
        ChargerState.Faulted,
        testStateMachine.getCurrentState(),
        "Transition from Preparing to Faulted should be allowed");

    // Transition from Charging to PoweredOff
    testStateMachine.transition(ChargerState.PoweredOff);
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    testStateMachine.transition(ChargerState.Preparing);
    testStateMachine.transition(ChargerState.Charging);
    testStateMachine.transition(ChargerState.Faulted);
    assertEquals(
        ChargerState.Faulted,
        testStateMachine.getCurrentState(),
        "Transition from Charging to Faulted should be allowed");
  }

  @Test
  void testInvalidTransitionFromPoweredOff() {
    // From PoweredOff, transitioning to Available is not allowed
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              testStateMachine.transition(ChargerState.Available);
            });
    assertTrue(
        exception.getMessage().contains("Invalid state transition"),
        "Expected an exception message indicating an invalid transition");
  }

  @Test
  void testInvalidTransitionFromBootingUp() {
    // From BootingUp, transitioning to Charging is not allowed
    testStateMachine.transition(ChargerState.BootingUp);
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              testStateMachine.transition(ChargerState.Charging);
            });
    assertTrue(
        exception.getMessage().contains("Invalid state transition"),
        "Expected an exception message indicating an invalid transition");
  }

  @Test
  void testInvalidTransitionFromAvailable() {
    // From Available, transitioning back to BootingUp is not allowed
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              testStateMachine.transition(ChargerState.BootingUp);
            });
    assertTrue(
        exception.getMessage().contains("Invalid state transition"),
        "Expected an exception message indicating an invalid transition");
  }

  @Test
  void testInvalidTransitionFromPreparing() {
    // From Preparing, transitioning to BootingUp is not allowed
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    testStateMachine.transition(ChargerState.Preparing);
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              testStateMachine.transition(ChargerState.BootingUp);
            });
    assertTrue(
        exception.getMessage().contains("Invalid state transition"),
        "Expected an exception message indicating an invalid transition");
  }

  @Test
  void testInvalidTransitionFromCharging() {
    // From Charging, transitioning to BootingUp is not allowed
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    testStateMachine.transition(ChargerState.Preparing);
    testStateMachine.transition(ChargerState.Charging);
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              testStateMachine.transition(ChargerState.BootingUp);
            });
    assertTrue(
        exception.getMessage().contains("Invalid state transition"),
        "Expected an exception message indicating an invalid transition");
  }

  @Test
  void testInvalidTransitionFromFaulted() {
    // From Faulted, transitioning to Charging is not allowed
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    testStateMachine.transition(ChargerState.Faulted);
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              testStateMachine.transition(ChargerState.Charging);
            });
    assertTrue(
        exception.getMessage().contains("Invalid state transition"),
        "Expected an exception message indicating an invalid transition");
  }

  @Test
  void testStateMachineTransaction() {
    // From Charging, transitioning to BootingUp is not allowed
    testStateMachine.transition(ChargerState.BootingUp);
    testStateMachine.transition(ChargerState.Available);
    testStateMachine.transition(ChargerState.Preparing);
    assertTrue(testStateMachine.inTransaction());

    testStateMachine.transition(ChargerState.Charging);
    assertTrue(testStateMachine.inTransaction());

    testStateMachine.transition(ChargerState.Available);
    assertFalse(testStateMachine.inTransaction());
  }

  @Test
  void testStateMachineBooted() {

    testStateMachine.transition(ChargerState.PoweredOff);
    assertFalse(testStateMachine.isBooted());

    testStateMachine.transition(ChargerState.BootingUp);
    assertFalse(testStateMachine.isBooted());

    testStateMachine.transition(ChargerState.Available);
    assertTrue(testStateMachine.isBooted());
  }
}
