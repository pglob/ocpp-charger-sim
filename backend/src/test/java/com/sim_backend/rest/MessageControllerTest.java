package com.sim_backend.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sim_backend.config.ConfigurationRegistry;
import com.sim_backend.electrical.ElectricalTransition;
import com.sim_backend.rest.controllers.MessageController;
import com.sim_backend.simulator.Simulator;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.messages.Authorize;
import com.sim_backend.websockets.messages.BootNotification;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.StatusNotification;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MessageControllerTest {

  @Mock private Javalin mockApp;
  @Mock private Simulator mockSimulator;
  @Mock private OCPPWebSocketClient mockWsClient;
  @Mock private SimulatorStateMachine mockStateMachine;
  @Mock private ElectricalTransition mockElec;
  @Mock private TransactionHandler mockTHandler;
  @Mock private Context mockContext;
  @Mock private ConfigurationRegistry mockConfig;

  private MessageController messageController;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    // Allow chaining on the context: ctx.status(…) returns ctx.
    when(mockContext.status(anyInt())).thenReturn(mockContext);

    // Stub the Simulator to return mock components
    when(mockSimulator.getWsClient()).thenReturn(mockWsClient);
    when(mockSimulator.getStateMachine()).thenReturn(mockStateMachine);
    when(mockSimulator.getElec()).thenReturn(mockElec);
    when(mockSimulator.getTransactionHandler()).thenReturn(mockTHandler);
    when(mockSimulator.getConfig()).thenReturn(mockConfig);

    // Stub isRebootInProgress() to be false by default
    when(mockSimulator.isRebootInProgress()).thenReturn(false);

    messageController = new MessageController(mockApp, mockSimulator);
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
    when(mockStateMachine.getCurrentState()).thenReturn(SimulatorState.PoweredOff);

    // Act
    messageController.state(mockContext);

    // Assert
    verify(mockContext).result("PoweredOff");
  }

  @Test
  void testRebootNormal() {
    // Arrange
    doNothing().when(mockSimulator).Reboot();

    // Act
    messageController.reboot(mockContext);

    // Assert
    verify(mockSimulator).Reboot();
    verify(mockContext).result("OK");
  }

  @Test
  void testRebootInProgress() {
    // Arrange
    when(mockSimulator.isRebootInProgress()).thenReturn(true);

    // Act
    messageController.reboot(mockContext);

    // Assert
    verify(mockContext).status(503);
    verify(mockContext).result("Reboot already in progress");
    verify(mockSimulator, never()).Reboot();
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
            + "\"timestamp\": \"2025-02-02T12:00:00Z\","
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
    verify(mockApp).post(eq("/api/message/authorize"), any());
    verify(mockApp).post(eq("/api/message/boot"), any());
    verify(mockApp).post(eq("/api/message/heartbeat"), any());
    verify(mockApp).get(eq("/api/state"), any());
    verify(mockApp).post(eq("/api/simulator/reboot"), any());
    verify(mockApp).post(eq("/api/state/online"), any());
    verify(mockApp).post(eq("/api/state/offline"), any());
    verify(mockApp).post(eq("/api/state/status"), any());
    verify(mockApp).post(eq("/api/transaction/start-charge"), any());
    verify(mockApp).post(eq("/api/transaction/stop-charge"), any());
    verify(mockApp).get(eq("/api/electrical/meter-value"), any());
    verify(mockApp).get(eq("/api/electrical/max-current"), any());
    verify(mockApp).get(eq("/api/electrical/current-import"), any());
  }
}
