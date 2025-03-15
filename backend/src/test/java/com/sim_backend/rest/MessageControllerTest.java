package com.sim_backend.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.charger.Charger;
import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.rest.controllers.MessageController;
import com.sim_backend.state.ChargerState;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.messages.Authorize;
import com.sim_backend.websockets.messages.BootNotification;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.StatusNotification;
import com.sim_backend.websockets.observers.StatusNotificationObserver;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MessageControllerTest {

  @Mock private Javalin mockApp;
  @Mock private Charger mockCharger;
  @Mock private OCPPWebSocketClient mockWsClient;
  @Mock private ChargerStateMachine mockStateMachine;
  @Mock private ElectricalTransition mockElec;
  @Mock private TransactionHandler mockTHandler;
  @Mock private StatusNotificationObserver mockStatusNotificationObserver;
  @Mock private Context mockContext;
  @Mock private ConfigurationRegistry mockConfig;

  private MessageController messageController;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    when(mockContext.pathParam("chargerId")).thenReturn("1");
    // Allow chaining on the context: ctx.status(…) returns ctx.
    when(mockContext.status(anyInt())).thenReturn(mockContext);

    // Stub the Charger to return mock components
    when(mockCharger.getWsClient()).thenReturn(mockWsClient);
    when(mockCharger.getStateMachine()).thenReturn(mockStateMachine);
    when(mockCharger.getElec()).thenReturn(mockElec);
    when(mockCharger.getTransactionHandler()).thenReturn(mockTHandler);
    when(mockCharger.getConfig()).thenReturn(mockConfig);
    when(mockCharger.getStatusNotificationObserver()).thenReturn(mockStatusNotificationObserver);

    // Stub isRebootInProgress() to be false by default
    when(mockCharger.isRebootInProgress()).thenReturn(false);

    messageController = new MessageController(mockApp, new Charger[] {mockCharger});
  }

  @Test
  void testAuthorize() {
    // Arrange
    doReturn(true).when(mockWsClient).pushMessage(any(Authorize.class));

    // Act
    messageController.authorize(mockContext);

    // Assert
    verify(mockContext).result("OK");
  }

  @Test
  void testBoot() {
    // Arrange
    doReturn(true).when(mockWsClient).pushMessage(any(BootNotification.class));

    // Act
    messageController.boot(mockContext);

    // Assert
    verify(mockContext).result("OK");
  }

  @Test
  void testHeartbeat() {
    // Arrange
    doReturn(true).when(mockWsClient).pushMessage(any(Heartbeat.class));

    // Act
    messageController.heartbeat(mockContext);

    // Assert
    verify(mockContext).result("OK");
  }

  @Test
  void testState() {
    // Arrange
    when(mockStateMachine.getCurrentState()).thenReturn(ChargerState.PoweredOff);
    when(mockWsClient.isOnline()).thenReturn(true);

    // Act
    messageController.state(mockContext);

    // Assert
    verify(mockContext).result("PoweredOff");
  }

  @Test
  void testStateOffline() {
    // Arrange
    when(mockStateMachine.getCurrentState()).thenReturn(ChargerState.PoweredOff);
    when(mockWsClient.isOnline()).thenReturn(false);

    // Act
    messageController.state(mockContext);

    // Assert
    verify(mockContext).result("PoweredOff (Offline)");
  }

  @Test
  void testRebootNormal() {
    // Arrange
    doNothing().when(mockCharger).reboot();

    // Act
    messageController.reboot(mockContext);

    // Assert
    verify(mockCharger).reboot();
    verify(mockContext).result("OK");
  }

  @Test
  void testRebootInProgress() {
    // Arrange
    when(mockCharger.isRebootInProgress()).thenReturn(true);

    // Act
    messageController.reboot(mockContext);

    // Assert
    verify(mockContext).status(503);
    verify(mockContext).result("Reboot already in progress");
    verify(mockCharger, never()).reboot();
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
  void testOfflineBooting() {
    // Arrange
    when(mockStateMachine.getCurrentState()).thenReturn(ChargerState.BootingUp);

    // Act
    messageController.offline(mockContext);

    // Assert
    InOrder inOrder = inOrder(mockContext, mockWsClient);
    inOrder.verify(mockContext).status(503);
    inOrder.verify(mockContext).result("Charger is booting");
    
    inOrder.verify(mockWsClient).goOffline();
    inOrder.verify(mockContext).result("OK");
  }

  @Test
  void testStatus() {
    // Arrange
    when(mockStateMachine.getCurrentState()).thenReturn(ChargerState.Available);
    doReturn(true).when(mockWsClient).pushMessage(any(StatusNotification.class));
    String jsonRequest =
        "{"
            + "\"connectorId\": \"1\","
            + "\"errorCode\": \"NoError\","
            + "\"info\": \"\","
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
  void testStatus_invalidConnector() {
    // Arrange
    // Use an invalid connectorId to trigger an exception
    String requestBody = "{" + "\"connectorId\": \"2\"," + "\"errorCode\": \"NoError\"" + "}";
    when(mockContext.body()).thenReturn(requestBody);

    // Act
    messageController.status(mockContext);

    // Assert
    verify(mockContext).status(400);
    verify(mockContext).result("Invalid values for connectorId, errorCode");
  }

  @Test
  void testStatusWithFault() {
    // Arrange
    String requestBody =
        "{"
            + "\"connectorId\": \"0\","
            + "\"errorCode\": \"HighTemperature\","
            + "\"info\": \"Error occurred\","
            + "\"vendorId\": \"Vendor\","
            + "\"vendorErrorCode\": \"VError\""
            + "}";
    when(mockContext.body()).thenReturn(requestBody);
    when(mockStateMachine.getCurrentState()).thenReturn(ChargerState.Available);

    // When fault() is called, simulate that the charger state becomes Faulted
    doAnswer(
            invocation -> {
              when(mockStateMachine.getCurrentState()).thenReturn(ChargerState.Faulted);
              return null;
            })
        .when(mockCharger)
        .fault(any());

    // Act
    messageController.status(mockContext);

    // Assert
    verify(mockCharger).fault(ChargePointErrorCode.HighTemperature);
    verify(mockStatusNotificationObserver)
        .sendStatusNotification(
            eq(0),
            eq(ChargePointErrorCode.HighTemperature),
            eq("Error occurred"),
            eq(ChargePointStatus.Faulted),
            eq(null),
            eq("Vendor"),
            eq("VError"));
    verify(mockContext).result("OK");
  }

  @Test
  void testClearFault() {
    // Act
    messageController.clearFault(mockContext);

    // Assert
    verify(mockCharger).clearFault();
  }

  @Test
  void testStartCharge() {
    // Arrange
    when(mockConfig.getIdTag()).thenReturn("testIdTag");

    // Act
    messageController.startCharge(mockContext);

    // Assert
    verify(mockTHandler).startCharging(eq(1), eq("testIdTag"));
    verify(mockContext).result("OK");
  }

  @Test
  void testStopCharge() {
    // Arrange
    when(mockConfig.getIdTag()).thenReturn("testIdTag");

    // Act
    messageController.stopCharge(mockContext);

    // Assert
    verify(mockTHandler).stopCharging(eq("testIdTag"), eq(null));
    verify(mockContext).result("OK");
  }

  @Test
  void testMeterValue() {
    // Arrange
    float energyValue = 123.456f;
    when(mockElec.getEnergyActiveImportRegister()).thenReturn(energyValue);
    String expectedFormatted = String.format("%.4g", energyValue);

    // Act
    messageController.meterValue(mockContext);

    // Assert
    verify(mockContext).result(expectedFormatted);
  }

  @Test
  void testMaxCurrent() {
    // Arrange
    when(mockElec.getMaxCurrent()).thenReturn(40);

    // Act
    messageController.maxCurrent(mockContext);

    // Assert
    verify(mockContext).result("40");
  }

  @Test
  void testCurrentImport() {
    // Arrange
    when(mockElec.getCurrentImport()).thenReturn(40.0);

    // Act
    messageController.currentImport(mockContext);

    // Assert
    verify(mockContext).result("40.0");
  }

  @Test
  void testGetSentMessages() {
    // Arrange
    List<String> sentMessages = List.of("1", "2", "3");
    when(mockWsClient.getSentMessages()).thenReturn(sentMessages);

    // Act
    messageController.getSentMessages(mockContext);

    // Assert
    verify(mockContext).json(sentMessages);
  }

  @Test
  void testGetReceivedMessages() {
    // Arrange
    List<String> receivedMessages = List.of("4", "5", "6");
    when(mockWsClient.getReceivedMessages()).thenReturn(receivedMessages);

    // Act
    messageController.getReceivedMessages(mockContext);

    // Assert
    verify(mockContext).json(receivedMessages);
  }

  @Test
  void testGetIdTagCSurl() {
    // Arrange
    when(mockConfig.getIdTag()).thenReturn("testTag");
    when(mockConfig.getCentralSystemUrl()).thenReturn("ws://example.com");

    // Act
    messageController.getIdTagCSurl(mockContext);

    // Assert
    String expectedJson = "{\"idTag\":\"testTag\", \"centralSystemUrl\":\"ws://example.com\"}";
    verify(mockContext).json(expectedJson);
  }

  @Test
  void testUpdateIdTagCSurl_valid() {
    // Arrange
    String requestBody = "{\"idTag\": \"newTag\", \"centralSystemUrl\": \"ws://newurl.com\"}";
    when(mockContext.body()).thenReturn(requestBody);

    // Act
    messageController.updateIdTagCSurl(mockContext);

    // Assert
    verify(mockConfig).setIdTag("newTag");
    verify(mockConfig).setCentralSystemUrl("ws://newurl.com");
    String expectedMessage =
        "Config updated successfully. idTag: newTag, centralSystemUrl: ws://newurl.com";
    verify(mockContext).status(200);
    verify(mockContext).result(expectedMessage);
  }

  @Test
  void testUpdateIdTagCSurl_missingIdTag() {
    // Arrange
    String requestBody = "{\"centralSystemUrl\": \"ws://newurl.com\"}";
    when(mockContext.body()).thenReturn(requestBody);

    // Act
    messageController.updateIdTagCSurl(mockContext);

    // Assert
    verify(mockContext).status(400);
    verify(mockContext).result("Error: Missing idTag or centralSystemUrl.");
  }

  @Test
  void testUpdateIdTagCSurl_missingCentralSystemUrl() {
    // Arrange
    String requestBody = "{\"idTag\": \"newTag\"}";
    when(mockContext.body()).thenReturn(requestBody);

    // Act
    messageController.updateIdTagCSurl(mockContext);

    // Assert
    verify(mockContext).status(400);
    verify(mockContext).result("Error: Missing idTag or centralSystemUrl.");
  }

  @Test
  void testUpdateIdTagCSurl_idTagTooLong() {
    // Arrange
    String longIdTag = "thisisaverylongidtagexceedinglimit";
    String requestBody =
        String.format(
            "{\"idTag\": \"%s\", \"centralSystemUrl\": \"ws://newurl.com\"}", longIdTag);
    when(mockContext.body()).thenReturn(requestBody);

    // Act
    messageController.updateIdTagCSurl(mockContext);

    // Assert
    verify(mockContext).status(400);
    verify(mockContext).result("Error: idTag cannot exceed 20 characters.");
  }

  @Test
  void testRegisterRoutes() {
    // Act
    messageController.registerRoutes(mockApp);

    // Assert
    verify(mockApp).post(eq("/api/{chargerId}/message/authorize"), any());
    verify(mockApp).post(eq("/api/{chargerId}/message/boot"), any());
    verify(mockApp).post(eq("/api/{chargerId}/message/heartbeat"), any());
    verify(mockApp).get(eq("/api/{chargerId}/state"), any());
    verify(mockApp).post(eq("/api/{chargerId}/charger/reboot"), any());
    verify(mockApp).post(eq("/api/{chargerId}/charger/clear-fault"), any());
    verify(mockApp).post(eq("/api/{chargerId}/state/online"), any());
    verify(mockApp).post(eq("/api/{chargerId}/state/offline"), any());
    verify(mockApp).post(eq("/api/{chargerId}/state/status"), any());
    verify(mockApp).post(eq("/api/{chargerId}/transaction/start-charge"), any());
    verify(mockApp).post(eq("/api/{chargerId}/transaction/stop-charge"), any());
    verify(mockApp).get(eq("/api/{chargerId}/electrical/meter-value"), any());
    verify(mockApp).get(eq("/api/{chargerId}/electrical/max-current"), any());
    verify(mockApp).get(eq("/api/{chargerId}/electrical/current-import"), any());
    verify(mockApp).get(eq("/api/{chargerId}/get-idtag-csurl"), any());
    verify(mockApp).post(eq("/api/{chargerId}/update-idtag-csurl"), any());
  }
}
