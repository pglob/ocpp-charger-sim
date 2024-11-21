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
import com.sim_backend.websockets.messages.HeartBeat;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.net.URI;
import java.net.URISyntaxException;

public class MessageController extends ControllerBase {

  private OCPPWebSocketClient webSocketClient;

  public MessageController(Javalin app) throws URISyntaxException {
    super(app);
    this.webSocketClient = new OCPPWebSocketClient(new URI(""));
  }

  private void authorize(Context ctx) {
    Authorize msg = new Authorize();
    webSocketClient.pushMessage(msg);
    ctx.result("OK");
  }

  private void boot(Context ctx) {
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

  private void heartbeat(Context ctx) {
    HeartBeat msg = new HeartBeat();
    webSocketClient.pushMessage(msg);
    ctx.result("OK");
  }

  private void online(Context ctx) {
    ctx.result("OK");
  }

  private void offline(Context ctx) {
    ctx.result("OK");
  }

  @Override
  public void registerRoutes(Javalin app) {
    app.post("/api/message/authorize", this::authorize);
    app.post("/api/message/boot", this::boot);
    app.post("/api/message/heartbeat", this::heartbeat);
    app.post("/api/state/online", this::online);
    app.post("/api/state/offline", this::offline);
  }
}
