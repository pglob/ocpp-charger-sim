package com.sim_backend.websockets;

import com.sim_backend.websockets.events.*;
import com.sim_backend.websockets.exceptions.*;
import com.sim_backend.websockets.messages.*;
import com.sim_backend.websockets.observer.*;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AuthObserverTest {
  @Test
  void testAuthObserver() {
    AuthorizeObserver testObserver = new AuthorizeObserver();
    OCPPMessageHandler testHandler = new OCPPMessageHandler();

    testObserver.addObserver(testHandler);
    AuthorizeResponse testResponse = new AuthorizeResponse("Accepted");
    testObserver.notifyAuth(testResponse);
    AuthorizeResponse testResponse2 = new AuthorizeResponse("Blocked");
    testObserver.notifyAuth(testResponse2);
  }
}
