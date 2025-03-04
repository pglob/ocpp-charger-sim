package com.sim_backend.websockets.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AvailabilityStatusTest {
  @Test
  public void testFromString() {
    assertEquals(AvailabilityStatus.ACCEPTED, AvailabilityStatus.fromString("Accepted"));
    assertEquals(AvailabilityStatus.SCHEDULED, AvailabilityStatus.fromString("Scheduled"));
    assertEquals(AvailabilityStatus.REJECTED, AvailabilityStatus.fromString("Rejected"));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              AvailabilityStatus.fromString("ABC");
            });

    assertEquals("Unexpected AvailabilityStatus: ABC", exception.getMessage());
  }
}
