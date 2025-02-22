package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.HeartbeatResponse;
import com.sim_backend.websockets.messages.StopTransaction;
import com.sim_backend.websockets.messages.StopTransactionResponse;
import com.sim_backend.websockets.types.OCPPMessage;
import java.time.ZonedDateTime;
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

  @Test
  void testGenerateID() {
    OCPPMessage message = new Heartbeat();
    String id = message.getMessageID();
    OCPPMessage cloned = message.cloneMessage();
    assertNotEquals(cloned.getMessageID(), id);
  }

  @Test
  void testClone() {
    HeartbeatResponse res1 = new HeartbeatResponse(ZonedDateTime.now());
    HeartbeatResponse res2 = (HeartbeatResponse) res1.cloneMessage();
    assertEquals(res1.getCurrentTime(), res2.getCurrentTime());

    StopTransaction stopTrans1 = new StopTransaction("Why", 2, 3, "Whe");
    StopTransaction stopTrans2 = (StopTransaction) stopTrans1.cloneMessage();
    assertEquals("Why", stopTrans2.getIdTag());
    assertEquals(2, stopTrans2.getTransactionId());
    assertEquals(3, stopTrans2.getMeterStop());
    assertEquals("Whe", stopTrans2.getTimestamp());

    StopTransactionResponse stopTransRes1 = new StopTransactionResponse("Accepted");
    StopTransactionResponse stopTransRes2 = (StopTransactionResponse) stopTransRes1.cloneMessage();
    assertEquals(AuthorizationStatus.ACCEPTED, stopTransRes2.getIdTagInfo().getStatus());
  }
}
