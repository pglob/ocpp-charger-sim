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
import com.sim_backend.state.ChargerState;
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

  /** Array holding the charger instances managed by this controller */
  private final Charger[] chargers;

  /**
   * Constructor that initializes the controller with the Javalin app and the array of chargers.
   *
   * @param app the Javalin application instance
   * @param chargers the array of Charger objects
   */
  public MessageController(Javalin app, Charger[] chargers) {
    super(app);
    this.chargers = chargers;
  }

  /**
   * Retrieves the Charger instance based on the "chargerId" path parameter.
   *
   * @param ctx the Javalin HTTP context containing the request parameters
   * @return the Charger instance if found and valid; otherwise, returns null after setting an error
   *     response
   */
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
    // Return the charger from the array (adjusting for 0-based index)
    return chargers[chargerId - 1];
  }

  /**
   * Checks if the OCPPWebSocketClient is available for the given charger.
   *
   * @param charger the Charger instance to check
   * @param ctx the HTTP context used to return an error message if not available
   * @return true if the OCPPWebSocketClient is available; false otherwise
   */
  private boolean checkWsClient(Charger charger, Context ctx) {
    if (charger.getWsClient() == null) {
      ctx.status(503).result("Charger is rebooting");
      return false;
    }
    return true;
  }

  /**
   * Checks if the TransactionHandler is available for the given charger.
   *
   * @param charger the Charger instance to check
   * @param ctx the HTTP context used to return an error message if not available
   * @return true if the TransactionHandler is available; false otherwise
   */
  private boolean checkTransactionHandler(Charger charger, Context ctx) {
    if (charger.getTransactionHandler() == null) {
      ctx.status(503).result("Charger is rebooting");
      return false;
    }
    return true;
  }

  /**
   * Checks if the ChargerStateMachine is available for the given charger.
   *
   * @param charger the Charger instance to check
   * @param ctx the HTTP context used to return an error message if not available
   * @return true if the ChargerStateMachine is available; false otherwise
   */
  private boolean checkStateMachine(Charger charger, Context ctx) {
    if (charger.getStateMachine() == null) {
      ctx.status(503).result("Charger is rebooting");
      return false;
    }
    return true;
  }

  /**
   * Checks if the ElectricalTransition is available for the given charger.
   *
   * @param charger the Charger instance to check
   * @param ctx the HTTP context used to return an error message if not available
   * @return true if the ElectricalTransition is available; false otherwise
   */
  private boolean checkElec(Charger charger, Context ctx) {
    if (charger.getElec() == null) {
      ctx.status(503).result("Charger is rebooting");
      return false;
    }
    return true;
  }

  /**
   * Processes the "authorize" request by sending an Authorize message via the WebSocket client.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void authorize(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    Authorize msg = new Authorize(charger.getConfig().getIdTag());

    charger.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  /**
   * Processes the "boot" request by sending a BootNotification message to the backend.
   *
   * @param ctx the HTTP context representing the request/response
   */
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

    charger.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  /**
   * Processes the "heartbeat" request by sending a Heartbeat message to the backend. This will
   * result in the simulator clock synchronizing.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void heartbeat(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    Heartbeat msg = new Heartbeat();

    charger.getWsClient().pushMessage(msg);
    ctx.result("OK");
  }

  /**
   * Returns the current state of the charger. If the charger is offline, appends " (offline)"".
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void state(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;

    ChargerStateMachine stateMachine = charger.getStateMachine();
    if (stateMachine == null) {
      ctx.result("PoweredOff");
      return;
    }

    String offlineState = charger.getWsClient().isOnline() ? "" : " (Offline)";

    ctx.result(stateMachine.getCurrentState().toString() + offlineState);
  }

  /**
   * Initiates a reboot sequence for the charger. It checks that no reboot is already in progress
   * and that all necessary components are available.
   *
   * @param ctx the HTTP context representing the request/response
   */
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

  /**
   * Processes the "online" request, which marks the charger as online.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void online(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;

    charger.getWsClient().goOnline();
    ctx.result("OK");
  }

  /**
   * Processes the "offline" request, marking the charger as offline. It also checks the state
   * machine to ensure the charger is not in the booting state.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void offline(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    if (!checkStateMachine(charger, ctx)) return;

    if (charger.getStateMachine().getCurrentState() == ChargerState.BootingUp) {
      ctx.status(503).result("Charger is booting");
    }

    charger.getWsClient().goOffline();
    ctx.result("OK");
  }

  /**
   * Processes a status update request. It reads the JSON payload for "connectorId", "errorCode",
   * and additional optional fields, and then sends a status notification.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void status(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    if (!checkStateMachine(charger, ctx)) return;

    String requestBody = ctx.body();
    JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

    // Validate that required fields are present
    if (!json.has("connectorId") || !json.has("errorCode")) {
      ctx.status(400).result("Missing required fields: connectorId, errorCode");
      return;
    }

    try {
      // Parse and validate the connectorId value
      int connectorId = json.get("connectorId").getAsInt();
      if (connectorId != 0 && connectorId != 1) {
        throw new IllegalArgumentException();
      }

      // Parse the errorCode and trim any whitespace
      String errorCodeStr = json.get("errorCode").getAsString().trim();
      ChargePointErrorCode errorCode =
          ChargePointErrorCode.valueOf(errorCodeStr); // if not valid, it will throw exception

      // Optionally parse the "info" field
      String info = json.has("info") ? json.get("info").getAsString().trim() : null;
      if (info != null && info.isEmpty()) info = null;

      // Parse timestamp if provided; otherwise set as null
      ZonedDateTime timestamp =
          json.has("timestamp")
              ? charger.getWsClient().getScheduler().getTime().getSynchronizedTime()
              : null;

      // Parse vendor-specific information if provided
      String vendorId = json.has("vendorId") ? json.get("vendorId").getAsString().trim() : null;
      if (vendorId != null && vendorId.isEmpty()) vendorId = null;

      String vendorErrorCode =
          json.has("vendorErrorCode") ? json.get("vendorErrorCode").getAsString().trim() : null;
      if (vendorErrorCode != null && vendorErrorCode.isEmpty()) vendorErrorCode = null;

      // If there is an error (other than NoError), mark the charger as faulted
      if (errorCode != ChargePointErrorCode.NoError) {
        charger.fault(errorCode);
      }

      // Determine the current status based on the state machine
      ChargePointStatus status =
          ChargePointStatus.fromString(charger.getStateMachine().getCurrentState().toString());

      // Send the status notification with all the parsed parameters
      charger
          .getStatusNotificationObserver()
          .sendStatusNotification(
              connectorId, errorCode, info, status, timestamp, vendorId, vendorErrorCode);

      ctx.result("OK");

    } catch (Exception e) {
      // If any parsing or validation fails, return a bad request error
      ctx.status(400).result("Invalid values for connectorId, errorCode");
    }
  }

  /**
   * Clears any fault condition on the charger.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void clearFault(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (charger.clearFault()) {
      ctx.result("OK");
    } else {
      ctx.status(500);
    }
  }

  /**
   * Returns a JSON list of messages sent by the charger via the WebSocket client.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void getSentMessages(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    ctx.json(charger.getWsClient().getSentMessages()); // Return sent messages as JSON
  }

  /**
   * Returns a JSON list of messages received by the charger via the WebSocket client.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void getReceivedMessages(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkWsClient(charger, ctx)) return;
    ctx.json(charger.getWsClient().getReceivedMessages()); // Return received messages as JSON
  }

  /**
   * Starts a charging transaction on the charger.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void startCharge(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkTransactionHandler(charger, ctx)) return;
    // Start the charging process using a hard-coded connector (1) and the configured idTag
    charger.getTransactionHandler().startCharging(1, charger.getConfig().getIdTag());
    ctx.result("OK");
  }

  /**
   * Stops the current charging transaction on the charger.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void stopCharge(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkTransactionHandler(charger, ctx)) return;
    // Stop charging using the configured idTag and no specified Reason
    charger.getTransactionHandler().stopCharging(charger.getConfig().getIdTag(), null);
    ctx.result("OK");
  }

  /**
   * Retrieves the current meter value (energy active import register) and returns it formatted to 4
   * significant digits.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void meterValue(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkElec(charger, ctx)) return;
    // Display only 4 significant digits
    ctx.result(String.format("%.4g", charger.getElec().getEnergyActiveImportRegister()));
  }

  /**
   * Returns the maximum current value available from the charger.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void maxCurrent(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkElec(charger, ctx)) return;
    ctx.result(String.valueOf(charger.getElec().getMaxCurrent()));
  }

  /**
   * Returns the current import value from the charger.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void currentImport(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    if (!checkElec(charger, ctx)) return;
    ctx.result(String.valueOf(charger.getElec().getCurrentImport()));
  }

  /**
   * Retrieves the current idTag and central system URL from the charger's configuration and returns
   * them as a JSON object.
   *
   * @param ctx the HTTP context representing the request/response
   */
  public void getIdTagCSurl(Context ctx) {
    Charger charger = getChargerID(ctx);
    if (charger == null) return;
    String idTag = charger.getConfig().getIdTag();
    String centralSystemUrl = charger.getConfig().getCentralSystemUrl();
    String configString =
        String.format("{\"idTag\":\"%s\", \"centralSystemUrl\":\"%s\"}", idTag, centralSystemUrl);
    ctx.json(configString);
  }

  /**
   * Updates the idTag and central system URL in the charger configuration.
   *
   * @param ctx the HTTP context representing the request/response
   */
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

  /**
   * Registers all the API routes/endpoints with the Javalin app.
   *
   * @param app the Javalin application instance
   */
  @Override
  public void registerRoutes(Javalin app) {
    // Message related endpoints
    app.post("/api/{chargerId}/message/authorize", this::authorize);
    app.post("/api/{chargerId}/message/boot", this::boot);
    app.post("/api/{chargerId}/message/heartbeat", this::heartbeat);

    // Charger state endpoint
    app.get("/api/{chargerId}/state", this::state);

    // Charger control endpoints
    app.post("/api/{chargerId}/charger/reboot", this::reboot);
    app.post("/api/{chargerId}/charger/clear-fault", this::clearFault);

    // Charger online/offline endpoints
    app.post("/api/{chargerId}/state/online", this::online);
    app.post("/api/{chargerId}/state/offline", this::offline);
    app.post("/api/{chargerId}/state/status", this::status);

    // Log endpoints for sent and received messages
    app.get("/api/{chargerId}/log/sentmessage", this::getSentMessages);
    app.get("/api/{chargerId}/log/receivedmessage", this::getReceivedMessages);

    // Transaction control endpoints
    app.post("/api/{chargerId}/transaction/start-charge", this::startCharge);
    app.post("/api/{chargerId}/transaction/stop-charge", this::stopCharge);

    // Electrical measurements endpoints
    app.get("/api/{chargerId}/electrical/meter-value", this::meterValue);
    app.get("/api/{chargerId}/electrical/max-current", this::maxCurrent);
    app.get("/api/{chargerId}/electrical/current-import", this::currentImport);

    // Configuration endpoints for idTag and central system URL
    app.get("/api/{chargerId}/get-idtag-csurl", this::getIdTagCSurl);
    app.post("/api/{chargerId}/update-idtag-csurl", this::updateIdTagCSurl);
  }
}
