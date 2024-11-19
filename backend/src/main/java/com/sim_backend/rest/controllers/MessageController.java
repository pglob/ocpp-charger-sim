package com.sim_backend.rest.controllers;

import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.messages.AuthorizeMessage;
import com.sim_backend.websockets.messages.BootNotificationMessage;
import com.sim_backend.websockets.messages.HeartBeatMessage;
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
    AuthorizeMessage msg = new AuthorizeMessage();
    webSocketClient.pushMessage(msg);
    ctx.result("OK");
  }

  private void boot(Context ctx) {
    BootNotificationMessage msg =
        new BootNotificationMessage(
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
    HeartBeatMessage msg = new HeartBeatMessage();
    webSocketClient.pushMessage(msg);
    ctx.result("OK");
  }

  @Override
  public void registerRoutes(Javalin app) {
    app.post("/api/message/authorize", this::authorize);
    app.post("/api/message/boot", this::boot);
    app.post("/api/message/heartbeat", this::heartbeat);
  }
}
