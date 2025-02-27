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

  private final Charger[] chargers;

  public MessageController(Javalin app, Charger[] chargers) {
    super(app);
    this.chargers = chargers;
  }

  private Charger getChargerID(Context ctx) {
    String chargerIdStr = ctx.pathParam("chargerId");
    int chargerId;
    try {
      chargerId = Integer.parseInt(chargerIdStr);
    } catch (NumberFormatException e) {
      ctx.status(400).result("Invalid chargerId");
      return null;
    }
    if (chargerId < 1 || chargerId > chargers.length) {
      ctx.status(404).result("Charger not found");
      return null;
    }
    return chargers[chargerId - 1];
  }

  // Helper methods to check if components are available

  private boolean checkWsClient(Charger charger, Context ctx) {
    if (charger.getWsClient() == null) {
      ctx.status(503).result("Charger is rebooting");
      return false;
    }
    return true;
  }

  private boolean checkTransactionHandler(Charger charger, Context ctx) {
    if (charger.getTransactionHandler() == null) {
      ctx.status(503).result("Charger is rebooting");
      return false;
    }
    return true;
  }

  private boolean checkStateMachine(Charger charger, Context ctx) {
    if (charger.getStateMachine() == null) {
      ctx.status(503).result("Charger is rebooting");
      return false;
    }
    return true;
  }

  private boolean checkElec(Charger charger, Context ctx) {
    if (charger.getElec() == null) {
      ctx.status(503).result("Charger is rebooting");
      return false;
    }
    return true;
  }

  public void authorize(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    Authorize msg = new Authorize();

    if (!MessageValidator.isValid(msg)) {
      throw new IllegalArgumentException(MessageValidator.log_message(msg));
    }

    charger.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  public void boot(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
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
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    Heartbeat msg = new Heartbeat();

    if (!MessageValidator.isValid(msg)) {
      throw new IllegalArgumentException(MessageValidator.log_message(msg));
    }

    charger.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  public void state(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    if (!checkStateMachine(charger, ctx)) return;
    ChargerStateMachine stateMachine = charger.getStateMachine();
    if (stateMachine == null) {
      ctx.result("PoweredOff");
      return;
    }
    String offlineState = charger.getWsClient().isOnline() ? "" : " (Offline)";

    ctx.result(stateMachine.getCurrentState().toString() + offlineState);
  }

  public void reboot(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (charger.isRebootInProgress()) {
      ctx.status(503).result("Reboot already in progress");
      return;
    }

    if (!checkWsClient(charger, ctx)) return;
    if (!checkElec(charger, ctx)) return;
    if (!checkTransactionHandler(charger, ctx)) return;
    charger.reboot();
    ctx.result("OK");
  }

  public void online(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;

    charger.getWsClient().goOnline();
    ctx.result("OK");
  }

  public void offline(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;

    charger.getWsClient().goOffline();
    ctx.result("OK");
  }

  public void status(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    if (!checkStateMachine(charger, ctx)) return;

    String requestBody = ctx.body();
    JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

    if (!json.has("connectorId") || !json.has("errorCode")) {
      ctx.status(400).result("Missing required fields: connectorId, errorCode");
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

      String info = json.has("info") ? json.get("info").getAsString().trim() : null;
      if (info != null && info.isEmpty()) info = null;

      ZonedDateTime timestamp =
          json.has("timestamp")
              ? charger.getWsClient().getScheduler().getTime().getSynchronizedTime()
              : null;

      String vendorId = json.has("vendorId") ? json.get("vendorId").getAsString().trim() : null;
      if (vendorId != null && vendorId.isEmpty()) vendorId = null;

      String vendorErrorCode =
          json.has("vendorErrorCode") ? json.get("vendorErrorCode").getAsString().trim() : null;
      if (vendorErrorCode != null && vendorErrorCode.isEmpty()) vendorErrorCode = null;

      if (errorCode != ChargePointErrorCode.NoError) {
        charger.fault(errorCode);
      }

      ChargePointStatus status =
          ChargePointStatus.fromString(charger.getStateMachine().getCurrentState().toString());

      charger
          .getStatusNotificationObserver()
          .sendStatusNotification(
              connectorId, errorCode, info, status, timestamp, vendorId, vendorErrorCode);

      ctx.result("OK");

    } catch (Exception e) {
      ctx.status(400).result("Invalid values for connectorId, errorCode");
    }
  }

  public void clearFault(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (charger.clearFault()) {
      ctx.result("OK");
    } else {
      ctx.status(500);
    }
  }

  public void getSentMessages(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    ctx.json(charger.getWsClient().getSentMessages()); // Return sent messages as JSON
  }

  public void getReceivedMessages(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    ctx.json(charger.getWsClient().getReceivedMessages()); // Return received messages as JSON
  }

  public void startCharge(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkTransactionHandler(charger, ctx)) return;
    charger.getTransactionHandler().startCharging(1, charger.getConfig().getIdTag());
    ctx.result("OK");
  }

  public void stopCharge(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkTransactionHandler(charger, ctx)) return;
    charger.getTransactionHandler().stopCharging(charger.getConfig().getIdTag(), null);
    ctx.result("OK");
  }

  public void meterValue(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkElec(charger, ctx)) return;
    // Display only 4 significant digits
    ctx.result(String.format("%.4g", charger.getElec().getEnergyActiveImportRegister()));
  }

  public void maxCurrent(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkElec(charger, ctx)) return;
    ctx.result(String.valueOf(charger.getElec().getMaxCurrent()));
  }

  public void currentImport(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkElec(charger, ctx)) return;
    ctx.result(String.valueOf(charger.getElec().getCurrentImport()));
  }

  public void getIdTagCSurl(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    String idTag = charger.getConfig().getIdTag();
    String centralSystemUrl = charger.getConfig().getCentralSystemUrl();
    String configString =
        String.format("{\"idTag\":\"%s\", \"centralSystemUrl\":\"%s\"}", idTag, centralSystemUrl);
    ctx.json(configString);
  }

  public void updateIdTagCSurl(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    String requestBody = ctx.body();
    JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

    // Extract idTag and centralSystemUrl from the JSON body
    String idTag = json.has("idTag") ? json.get("idTag").getAsString() : null;
    String centralSystemUrl =
        json.has("centralSystemUrl") ? json.get("centralSystemUrl").getAsString() : null;

    // Check if either idTag or centralSystemUrl is null
    if (idTag == null || centralSystemUrl == null) {
      ctx.status(400).result("Error: Missing idTag or centralSystemUrl.");
      return;
    }

    // Check if idTag exceeds the maximum length of 20 characters
    if (idTag.length() > 20) {
      ctx.status(400).result("Error: idTag cannot exceed 20 characters.");
      return;
    }

    charger.getConfig().setIdTag(idTag);
    charger.getConfig().setCentralSystemUrl(centralSystemUrl);

    String successMessage =
        String.format(
            "Config updated successfully. idTag: %s, centralSystemUrl: %s",
            idTag, centralSystemUrl);
    ctx.status(200).result(successMessage);
  }

  @Override
  public void registerRoutes(Javalin app) {
    app.post("/api/{chargerId}/message/authorize", this::authorize);
    app.post("/api/{chargerId}/message/boot", this::boot);
    app.post("/api/{chargerId}/message/heartbeat", this::heartbeat);

    app.get("/api/{chargerId}/state", this::state);

    app.post("/api/{chargerId}/charger/reboot", this::reboot);
    app.post("/api/{chargerId}/charger/clear-fault", this::clearFault);

    app.post("/api/{chargerId}/state/online", this::online);
    app.post("/api/{chargerId}/state/offline", this::offline);
    app.post("/api/{chargerId}/state/status", this::status);

    app.get("/api/{chargerId}/log/sentmessage", this::getSentMessages);
    app.get("/api/{chargerId}/log/receivedmessage", this::getReceivedMessages);

    app.post("/api/{chargerId}/transaction/start-charge", this::startCharge);
    app.post("/api/{chargerId}/transaction/stop-charge", this::stopCharge);

    app.get("/api/{chargerId}/electrical/meter-value", this::meterValue);
    app.get("/api/{chargerId}/electrical/max-current", this::maxCurrent);
    app.get("/api/{chargerId}/electrical/current-import", this::currentImport);

    app.get("/api/{chargerId}/get-idtag-csurl", this::getIdTagCSurl);
    app.post("/api/{chargerId}/update-idtag-csurl", this::updateIdTagCSurl);
  }
}
