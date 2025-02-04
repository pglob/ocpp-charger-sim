/**
 * Controller class for handling OCPP charging simulator messages and states. This includes
 * processing authorize, boot, heartbeat, online, and offline requests.
 *
 * <p>The class interacts with the OCPPWebSocketClient to push messages to the backend and responds
 * to HTTP POST requests from the front end. Each endpoint corresponds to a specific action (e.g.,
 * authorize, boot, heartbeat) and allows the front end to trigger actions on the WebSocket client.
 */
package com.sim_backend.rest.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sim_backend.simulator.Simulator;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.messages.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class MessageController extends ControllerBase {

  private final Simulator sim;

  public MessageController(Javalin app, Simulator sim) {
    super(app);
    this.sim = sim;
  }

  // Helper methods to check if components are available

  private boolean checkWsClient(Context ctx) {
    if (sim.getWsClient() == null) {
      ctx.status(503).result("Simulator is rebooting");
      return false;
    }
    return true;
  }

  private boolean checkTransactionHandler(Context ctx) {
    if (sim.getTransactionHandler() == null) {
      ctx.status(503).result("Simulator is rebooting");
      return false;
    }
    return true;
  }

  private boolean checkElec(Context ctx) {
    if (sim.getElec() == null) {
      ctx.status(503).result("Simulator is rebooting");
      return false;
    }
    return true;
  }

  public void authorize(Context ctx) {
    if (!checkWsClient(ctx)) return;
    Authorize msg = new Authorize();
    
    if (!MessageValidator.isValid(msg)) {
      throw new IllegalArgumentException(MessageValidator.log_message(msg));
    }

    sim.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  public void boot(Context ctx) {
    if (!checkWsClient(ctx)) return;
    BootNotification msg =
        new BootNotification(
            "CP Vendor",
            "CP Model",
            "CP S/N",
            "Box S/N",
            "Firmware",
            "ICCID",
            "IMSI",
            "Meter Type",
            "Meter S/N");

    if (!MessageValidator.isValid(msg)) {
      throw new IllegalArgumentException(MessageValidator.log_message(msg));
    }

    sim.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  public void heartbeat(Context ctx) {
    if (!checkWsClient(ctx)) return;
    Heartbeat msg = new Heartbeat();

    if (!MessageValidator.isValid(msg)) {
      throw new IllegalArgumentException(MessageValidator.log_message(msg));
    }

    sim.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  public void state(Context ctx) {
    SimulatorStateMachine stateMachine = sim.getStateMachine();
    if (stateMachine == null) {
      ctx.result("PoweredOff");
      return;
    }
    ctx.result(stateMachine.getCurrentState().toString());
  }

  public void reboot(Context ctx) {
    if (sim.isRebootInProgress()) {
      ctx.status(503).result("Reboot already in progress");
      return;
    }

    if (!checkWsClient(ctx)) return;
    if (!checkElec(ctx)) return;
    if (!checkTransactionHandler(ctx)) return;
    sim.Reboot();
    ctx.result("OK");
  }

  public void online(Context ctx) {
    if (!checkWsClient(ctx)) return;
    if (!checkElec(ctx)) return;
    if (!checkTransactionHandler(ctx)) return;
    ctx.result("OK");
  }

  public void offline(Context ctx) {
    if (!checkWsClient(ctx)) return;
    if (!checkElec(ctx)) return;
    if (!checkTransactionHandler(ctx)) return;
    ctx.result("OK");
  }

  public void status(Context ctx) {
    if (!checkWsClient(ctx)) return;

    String requestBody = ctx.body();
    JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

    if (!json.has("connectorId") || !json.has("errorCode") || !json.has("status")) {
      ctx.status(400).result("Missing required fields: connectorId, errorCode, status");
      return;
    }

    int connectorId = json.get("connectorId").getAsInt();
    ChargePointErrorCode errorCode =
        ChargePointErrorCode.valueOf(json.get("errorCode").getAsString());
    String info = json.has("info") ? json.get("info").getAsString() : "";
    ChargePointStatus status =
        json.has("status")
            ? ChargePointStatus.valueOf(json.get("status").getAsString())
            : ChargePointStatus.Available;
    ZonedDateTime timestamp =
        json.has("timestamp") && !json.get("timestamp").getAsString().isEmpty()
            ? ZonedDateTime.parse(json.get("timestamp").getAsString())
            : ZonedDateTime.now();
    String vendorId = json.has("vendorId") ? json.get("vendorId").getAsString() : "";
    String vendorErrorCode =
        json.has("vendorErrorCode") ? json.get("vendorErrorCode").getAsString() : "";

    StatusNotification msg =
        new StatusNotification(
            connectorId, errorCode, info, status, timestamp, vendorId, vendorErrorCode);
    sim.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  public void getSentMessages(Context ctx) {
    ctx.json(sim.getWsClient().getSentMessages()); // Return sent messages as JSON
  }

  public void getReceivedMessages(Context ctx) {
    ctx.json(sim.getWsClient().getReceivedMessages()); // Return received messages as JSON
  }

  public void startCharge(Context ctx) {
    if (!checkTransactionHandler(ctx)) return;
    sim.getTransactionHandler().StartCharging(1, sim.getConfig().getIdTag());
    ctx.result("OK");
  }

  public void stopCharge(Context ctx) {
    if (!checkTransactionHandler(ctx)) return;
    sim.getTransactionHandler().StopCharging(sim.getConfig().getIdTag());
    ctx.result("OK");
  }

  public void meterValue(Context ctx) {
    if (!checkElec(ctx)) return;
    // Display only 4 significant digits
    ctx.result(String.format("%.4g", sim.getElec().getEnergyActiveImportRegister()));
  }

  public void maxCurrent(Context ctx) {
    if (!checkElec(ctx)) return;
    ctx.result(String.valueOf(sim.getElec().getMaxCurrent()));
  }

  public void currentImport(Context ctx) {
    if (!checkElec(ctx)) return;
    ctx.result(String.valueOf(sim.getElec().getCurrentImport()));
  }

  @Override
  public void registerRoutes(Javalin app) {
    app.post("/api/message/authorize", this::authorize);
    app.post("/api/message/boot", this::boot);
    app.post("/api/message/heartbeat", this::heartbeat);
    app.get("/api/state", this::state);
    app.post("/api/simulator/reboot", this::reboot);
    app.post("/api/state/online", this::online);
    app.post("/api/state/offline", this::offline);
    app.post("/api/state/status", this::status);
    app.get("/api/log/sentmessage", this::getSentMessages);
    app.get("/api/log/receivedmessage", this::getReceivedMessages);
    app.post("/api/transaction/start-charge", this::startCharge);
    app.post("/api/transaction/stop-charge", this::stopCharge);
    app.get("/api/electrical/meter-value", this::meterValue);
    app.get("/api/electrical/max-current", this::maxCurrent);
    app.get("/api/electrical/current-import", this::currentImport);
  }
}
