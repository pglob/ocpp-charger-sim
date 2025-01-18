package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StartTransactionTest {

  @Test
  public void testStartTransaction() {
    StartTransaction request = new StartTransaction(1, "testId", 5342, "2025-01-01T00:00:00Z");

    assertEquals(1, request.getConnectorId());
    assertEquals("testId", request.getIdTag());
    assertEquals(5342, request.getMeterStart());
    assertEquals("2025-01-01T00:00:00Z", request.getTimestamp());
  }
}
