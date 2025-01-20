/**
 * Controller class for handling OCPP charging simulator messages and states. This includes
 * processing authorize, boot, heartbeat, online, and offline requests.
 *
 * <p>The class interacts with the OCPPWebSocketClient to push messages to the backend and responds
 * to HTTP POST requests from the front end. Each endpoint corresponds to a specific action (e.g.,
 * authorize, boot, heartbeat) and allows the front end to trigger actions on the WebSocket client.
 */
package com.sim_backend.rest.controllers;

import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.messages.Authorize;
import com.sim_backend.websockets.messages.BootNotification;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.StatusNotification;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import java.time.OffsetDateTime;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.Getter;

@Getter
public class MessageController extends ControllerBase {

  private OCPPWebSocketClient webSocketClient;

  public MessageController(Javalin app) throws URISyntaxException {
    super(app);
    this.webSocketClient = new OCPPWebSocketClient(new URI(""));
  }

  public MessageController(Javalin app, OCPPWebSocketClient webSocketClient) {
    super(app);
    this.webSocketClient = webSocketClient;
  }

  public void authorize(Context ctx) {
    Authorize msg = new Authorize();
    webSocketClient.pushMessage(msg);
    ctx.result("OK");
  }

  public void boot(Context ctx) {
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
    webSocketClient.pushMessage(msg);
    ctx.result("OK");
  }

  public void heartbeat(Context ctx) {
    Heartbeat msg = new Heartbeat();
    webSocketClient.pushMessage(msg);
    ctx.result("OK");
  }

  public void online(Context ctx) {
    ctx.result("OK");
  }

  public void offline(Context ctx) {
    ctx.result("OK");
  }

  public void status(Context ctx) {
    String requestBody = ctx.body();
    JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
    
    if (!json.has("connectorId") || !json.has("errorCode") || !json.has("status")) {
        ctx.status(400).result("Missing required fields: connectorId, errorCode, status");
        return;
    }

    int connectorId = json.has("connectorId") ? json.get("connectorId").getAsInt() : 0; 
    ChargePointErrorCode errorCode = json.has("errorCode") ? ChargePointErrorCode.valueOf(json.get("errorCode").getAsString()) : ChargePointErrorCode.NoError;
    String info = json.has("info") ? json.get("info").getAsString() : "";
    ChargePointStatus status = json.has("status") ? ChargePointStatus.valueOf(json.get("status").getAsString()) : ChargePointStatus.Available;
    OffsetDateTime timestamp = json.has("timestamp") && !json.get("timestamp").getAsString().isEmpty()
        ? OffsetDateTime.parse(json.get("timestamp").getAsString())
        : OffsetDateTime.now();
    String vendorId = json.has("vendorId") ? json.get("vendorId").getAsString() : "";
    String vendorErrorCode = json.has("vendorErrorCode") ? json.get("vendorErrorCode").getAsString() : "";
    
    StatusNotification msg = new StatusNotification(connectorId, errorCode, info, status, timestamp, vendorId, vendorErrorCode);
    webSocketClient.pushMessage(msg);
    ctx.result("OK");
}

  @Override
  public void registerRoutes(Javalin app) {
    app.post("/api/message/authorize", this::authorize);
    app.post("/api/message/boot", this::boot);
    app.post("/api/message/heartbeat", this::heartbeat);
    app.post("/api/state/online", this::online);
    app.post("/api/state/offline", this::offline);
    app.post("/api/state/status", this::status);
  }
}
