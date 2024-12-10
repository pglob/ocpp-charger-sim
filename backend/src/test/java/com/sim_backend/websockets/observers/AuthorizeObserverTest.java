package com.sim_backend.websockets.observers;

import static org.mockito.Mockito.*;

import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.messages.AuthorizeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class AuthorizeObserverTest {
  @Mock private OCPPWebSocketClient testClient;
  @Mock private OnOCPPMessage testMessage;

  private AuthorizeObserver testObserver;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    testObserver = new AuthorizeObserver();
  }

  @Test
  void testAuthorizeAccepted() {
    AuthorizeResponse response = new AuthorizeResponse("Accepted");
    testObserver.handleAuthorize(testClient, response);

    ArgumentCaptor<AuthorizeResponse> captor = ArgumentCaptor.forClass(AuthorizeResponse.class);
    verify(testClient).pushMessage(captor.capture());
  }

  @Test
  void testAuthorizeBlocked() {
    AuthorizeResponse response = new AuthorizeResponse("Blocked");
    testObserver.handleAuthorize(testClient, response);

    verify(testClient, never()).pushMessage(any(AuthorizeResponse.class));
  }

  @Test
  void testAuthorizeExpired() {
    AuthorizeResponse response = new AuthorizeResponse("Expired");
    testObserver.handleAuthorize(testClient, response);

    verify(testClient, never()).pushMessage(any(AuthorizeResponse.class));
  }

  @Test
  void testAuthorizeInvalid() {
    AuthorizeResponse response = new AuthorizeResponse("Invalid");
    testObserver.handleAuthorize(testClient, response);

    verify(testClient, never()).pushMessage(any(AuthorizeResponse.class));
  }

  @Test
  void testAuthorizeConcurrent() {
    AuthorizeResponse response = new AuthorizeResponse("ConcurrentTx");
    testObserver.handleAuthorize(testClient, response);

    verify(testClient, never()).pushMessage(any(AuthorizeResponse.class));
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
