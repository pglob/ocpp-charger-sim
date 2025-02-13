/**
 * Controller class for handling OCPP charger messages and states. This includes processing
 * authorize, boot, heartbeat, online, and offline requests.
 *
 * <p>The class interacts with the OCPPWebSocketClient to push messages to the backend and responds
 * to HTTP POST requests from the front end. Each endpoint corresponds to a specific action (e.g.,
 * authorize, boot, heartbeat) and allows the front end to trigger actions on the WebSocket client.
 */
package com.sim_backend.rest.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sim_backend.charger.Charger;
import com.sim_backend.state.ChargerStateMachine;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.messages.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class MessageController extends ControllerBase {

  private final Charger charger;

  public MessageController(Javalin app, Charger charger) {
    super(app);
    this.charger = charger;
  }

  // Helper methods to check if components are available

  private boolean checkWsClient(Context ctx) {
    if (charger.getWsClient() == null) {
      ctx.status(503).result("Charger is rebooting");
      return false;
    }
    return true;
  }

  private boolean checkTransactionHandler(Context ctx) {
    if (charger.getTransactionHandler() == null) {
      ctx.status(503).result("Charger is rebooting");
      return false;
    }
    return true;
  }

  private boolean checkElec(Context ctx) {
    if (charger.getElec() == null) {
      ctx.status(503).result("Charger is rebooting");
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

    charger.getWsClient().pushMessage(msg);
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

    charger.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  public void heartbeat(Context ctx) {
    if (!checkWsClient(ctx)) return;
    Heartbeat msg = new Heartbeat();

    if (!MessageValidator.isValid(msg)) {
      throw new IllegalArgumentException(MessageValidator.log_message(msg));
    }

    charger.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  public void state(Context ctx) {
    ChargerStateMachine stateMachine = charger.getStateMachine();
    if (stateMachine == null) {
      ctx.result("PoweredOff");
      return;
    }
    ctx.result(stateMachine.getCurrentState().toString());
  }

  public void reboot(Context ctx) {
    if (charger.isRebootInProgress()) {
      ctx.status(503).result("Reboot already in progress");
      return;
    }

    if (!checkWsClient(ctx)) return;
    if (!checkElec(ctx)) return;
    if (!checkTransactionHandler(ctx)) return;
    charger.Reboot();
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

    try {
      int connectorId = json.get("connectorId").getAsInt();
      if (connectorId != 0 && connectorId != 1) {
        throw new IllegalArgumentException();
      }

      String errorCodeStr = json.get("errorCode").getAsString().trim();
      ChargePointErrorCode errorCode =
          ChargePointErrorCode.valueOf(errorCodeStr); // if not valid, it will throw exception

      String statusStr = json.get("status").getAsString().trim();
      ChargePointStatus status =
          ChargePointStatus.valueOf(statusStr); // //if not valid, it will throw exception

      String info = json.has("info") ? json.get("info").getAsString().trim() : null;
      if (info != null && info.isEmpty()) info = null;

      ZonedDateTime timestamp = json.has("timestamp") ? ZonedDateTime.now() : null;

      String vendorId = json.has("vendorId") ? json.get("vendorId").getAsString().trim() : null;
      if (vendorId != null && vendorId.isEmpty()) vendorId = null;

      String vendorErrorCode =
          json.has("vendorErrorCode") ? json.get("vendorErrorCode").getAsString().trim() : null;
      if (vendorErrorCode != null && vendorErrorCode.isEmpty()) vendorErrorCode = null;

      StatusNotification msg =
          new StatusNotification(
              connectorId, errorCode, info, status, timestamp, vendorId, vendorErrorCode);

      charger.getWsClient().pushMessage(msg);
      ctx.result("OK");

    } catch (Exception e) {
      ctx.status(400).result("Invalid values for connectorId, errorCode, status");
    }
  }

  public void getSentMessages(Context ctx) {
    ctx.json(charger.getWsClient().getSentMessages()); // Return sent messages as JSON
  }

  public void getReceivedMessages(Context ctx) {
    ctx.json(charger.getWsClient().getReceivedMessages()); // Return received messages as JSON
  }

  public void startCharge(Context ctx) {
    if (!checkTransactionHandler(ctx)) return;
    charger.getTransactionHandler().StartCharging(1, charger.getConfig().getIdTag());
    ctx.result("OK");
  }

  public void stopCharge(Context ctx) {
    if (!checkTransactionHandler(ctx)) return;
    charger.getTransactionHandler().StopCharging(charger.getConfig().getIdTag());
    ctx.result("OK");
  }

  public void meterValue(Context ctx) {
    if (!checkElec(ctx)) return;
    // Display only 4 significant digits
    ctx.result(String.format("%.4g", charger.getElec().getEnergyActiveImportRegister()));
  }

  public void maxCurrent(Context ctx) {
    if (!checkElec(ctx)) return;
    ctx.result(String.valueOf(charger.getElec().getMaxCurrent()));
  }

  public void currentImport(Context ctx) {
    if (!checkElec(ctx)) return;
    ctx.result(String.valueOf(charger.getElec().getCurrentImport()));
  }

  @Override
  public void registerRoutes(Javalin app) {
    app.post("/api/message/authorize", this::authorize);
    app.post("/api/message/boot", this::boot);
    app.post("/api/message/heartbeat", this::heartbeat);

    app.get("/api/state", this::state);

    app.post("/api/charger/reboot", this::reboot);

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
