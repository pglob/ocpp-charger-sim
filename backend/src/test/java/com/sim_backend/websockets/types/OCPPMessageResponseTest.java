package com.sim_backend.websockets.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sim_backend.websockets.messages.Authorize;
import com.sim_backend.websockets.messages.AuthorizeResponse;
import org.junit.jupiter.api.Test;

public class OCPPMessageResponseTest {
  @Test
  public void testResponseID() {
    Authorize authorize = new Authorize();
    AuthorizeResponse response = new AuthorizeResponse(authorize, "Accepted");
    assertEquals(response.getMessageID(), authorize.getMessageID());
  }
}
