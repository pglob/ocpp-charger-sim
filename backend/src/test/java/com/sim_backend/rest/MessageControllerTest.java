package com.sim_backend.rest;

import static org.mockito.Mockito.*;

import com.sim_backend.rest.controllers.MessageController;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.messages.Authorize;
import com.sim_backend.websockets.messages.BootNotification;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.StatusNotification;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MessageControllerTest {

  @Mock private Javalin mockApp;

  @Mock private OCPPWebSocketClient mockWebSocketClient;

  @Mock private Context mockContext;

  private MessageController messageController;

  @BeforeEach
  void setUp() throws URISyntaxException {
    MockitoAnnotations.openMocks(this);

    // Create a spy or mock MessageController to allow partial mocking
    messageController = spy(new MessageController(mockApp));

    // Replace the actual WebSocketClient with the mock
    // This requires making the webSocketClient field protected or package-private
    // or using reflection to set the field
    doReturn(mockWebSocketClient).when(messageController).getWebSocketClient();
  }

  @Test
  void testAuthorize() {
    // Arrange
    doNothing().when(mockWebSocketClient).pushMessage(any(Authorize.class));

    // Act
    messageController.authorize(mockContext);

    // Assert
    verify(mockContext).result("OK");
  }

  @Test
  void testBoot() {
    // Arrange
    doNothing().when(mockWebSocketClient).pushMessage(any(BootNotification.class));

    // Act
    messageController.boot(mockContext);

    // Assert
    verify(mockContext).result("OK");
  }

  @Test
  void testHeartbeat() {
    // Arrange
    doNothing().when(mockWebSocketClient).pushMessage(any(Heartbeat.class));

    // Act
    messageController.heartbeat(mockContext);

    // Assert
    verify(mockContext).result("OK");
  }

  @Test
  void testOnline() {
    // Act
    messageController.online(mockContext);

    // Assert
    verify(mockContext).result("OK");
  }

  @Test
  void testOffline() {
    // Act
    messageController.offline(mockContext);

    // Assert
    verify(mockContext).result("OK");
  }

  @Test
void testStatus() {
    // Arrange
    doNothing().when(mockWebSocketClient).pushMessage(any(StatusNotification.class));
    String jsonRequest = "{"
        + "\"connectorId\": \"1\","
        + "\"errorCode\": \"HighTemperature\","
        + "\"info\": \"\","
        + "\"status\": \"Faulted\","
        + "\"timestamp\": \"\","
        + "\"vendorId\": \"\","
        + "\"vendorErrorCode\": \"\""
        + "}";
    when(mockContext.body()).thenReturn(jsonRequest);

    // Act
    messageController.status(mockContext);

    // Assert
    verify(mockContext).result("OK");
}

  @Test
  void testRegisterRoutes() {
    // Act
    messageController.registerRoutes(mockApp);

    // Assert
    verify(mockApp).post(eq("/api/message/authorize"), any());
    verify(mockApp).post(eq("/api/message/boot"), any());
    verify(mockApp).post(eq("/api/message/heartbeat"), any());
    verify(mockApp).post(eq("/api/state/online"), any());
    verify(mockApp).post(eq("/api/state/offline"), any());
  }
}
