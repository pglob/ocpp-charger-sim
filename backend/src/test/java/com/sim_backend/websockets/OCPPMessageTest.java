package com.sim_backend.websockets;

import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.types.OCPPMessage;
import org.junit.jupiter.api.Test;

public class OCPPMessageTest {

  @Test
  public void testIncrementTries() {
    OCPPMessage message = new Heartbeat();
    int tries = message.incrementTries();
    assert tries == 1;
    tries = message.incrementTries();
    assert tries == 2;
  }
}
