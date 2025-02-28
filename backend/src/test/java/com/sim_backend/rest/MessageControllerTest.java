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
import com.sim_backend.websockets.messages.Authorize;
import com.sim_backend.websockets.messages.BootNotification;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.StatusNotification;
import com.sim_backend.websockets.observers.StatusNotificationObserver;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    doNothing().when(mockCharger).Reboot();

    // Act
    messageController.reboot(mockContext);

    // Assert
    verify(mockCharger).Reboot();
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
    verify(mockCharger, never()).Reboot();
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
    doReturn(true).when(mockWsClient).pushMessage(any(StatusNotification.class));
    String jsonRequest =
        "{"
            + "\"connectorId\": \"1\","
            + "\"errorCode\": \"NoError\","
            + "\"info\": \"\","
            + "\"status\": \"Available\","
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
  void testStartCharge() {
    // Arrange
    when(mockConfig.getIdTag()).thenReturn("testIdTag");

    // Act
    messageController.startCharge(mockContext);

    // Assert
    verify(mockTHandler).StartCharging(eq(1), eq("testIdTag"));
    verify(mockContext).result("OK");
  }

  @Test
  void testStopCharge() {
    // Arrange
    when(mockConfig.getIdTag()).thenReturn("testIdTag");

    // Act
    messageController.stopCharge(mockContext);

    // Assert
    verify(mockTHandler).StopCharging(eq("testIdTag"));
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
    when(mockElec.getCurrentImport()).thenReturn(40);

    // Act
    messageController.currentImport(mockContext);

    // Assert
    verify(mockContext).result("40");
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
    verify(mockApp).post(eq("/api/{chargerId}/state/online"), any());
    verify(mockApp).post(eq("/api/{chargerId}/state/offline"), any());
    verify(mockApp).post(eq("/api/{chargerId}/state/status"), any());
    verify(mockApp).post(eq("/api/{chargerId}/transaction/start-charge"), any());
    verify(mockApp).post(eq("/api/{chargerId}/transaction/stop-charge"), any());
    verify(mockApp).get(eq("/api/{chargerId}/electrical/meter-value"), any());
    verify(mockApp).get(eq("/api/{chargerId}/electrical/max-current"), any());
    verify(mockApp).get(eq("/api/{chargerId}/electrical/current-import"), any());
  }
}
