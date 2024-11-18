package com.sim_backend.websockets;

import com.sim_backend.websockets.messages.HeartBeat;
import com.sim_backend.websockets.types.OcppMessage;
import org.junit.jupiter.api.Test;

public class OcppMessageTest {

  @Test
  public void testIncrementTries() {
    OcppMessage message = new HeartBeat();
    int tries = message.incrementTries();
    assert tries == 1;
    tries = message.incrementTries();
    assert tries == 2;
  }
}
