package com.sim_backend.websockets.observers;

import static org.mockito.Mockito.*;

import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.AuthorizeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class AuthorizeObserverTest {
  @Mock private OnOCPPMessage testMessage;

  private AuthorizeObserver testObserver;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    testObserver = new AuthorizeObserver();
  }

  @Test
  void testAuthorizeMessage() {
    AuthorizeResponse responseAccepted = new AuthorizeResponse("Accepted");
    when(testMessage.getMessage()).thenReturn(responseAccepted);
    testObserver.onMessageReceived(testMessage);

    AuthorizeResponse responseBlocked = new AuthorizeResponse("Blocked");
    when(testMessage.getMessage()).thenReturn(responseBlocked);
    testObserver.onMessageReceived(testMessage);

    AuthorizeResponse responseExpired = new AuthorizeResponse("Expired");
    when(testMessage.getMessage()).thenReturn(responseExpired);
    testObserver.onMessageReceived(testMessage);

    AuthorizeResponse responseInvalid = new AuthorizeResponse("Invalid");
    when(testMessage.getMessage()).thenReturn(responseInvalid);
    testObserver.onMessageReceived(testMessage);

    AuthorizeResponse responseConcurrent = new AuthorizeResponse("ConcurrentTx");
    when(testMessage.getMessage()).thenReturn(responseConcurrent);
    testObserver.onMessageReceived(testMessage);
  }
}
