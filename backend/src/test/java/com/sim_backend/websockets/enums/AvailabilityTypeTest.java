package com.sim_backend.websockets.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AvailabilityTypeTest {
  @Test
  public void testFromString() {
    assertEquals(AvailabilityType.OPERATIVE, AvailabilityType.fromString("Operative"));
    assertEquals(AvailabilityType.INOPERATIVE, AvailabilityType.fromString("Inoperative"));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              AvailabilityType.fromString("ABC");
            });

    assertEquals("Unexpected AvailabilityType: ABC", exception.getMessage());
  }
}
