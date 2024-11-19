package com.sim_backend.rest.controllers;

import com.sim_backend.rest.model.AuthorizeRequest;
import com.sim_backend.rest.model.AuthorizeResponse;
import com.sim_backend.rest.model.BootNotificationRequest;
import com.sim_backend.rest.model.HeartbeatRequest;
import com.sim_backend.rest.service.MessageService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class MessageController extends ControllerBase {

  private final MessageService messageService;

  public MessageController(Javalin app, MessageService service) {
    super(app);
    this.messageService = service;
  }

  private void authorize(Context ctx) {
    AuthorizeRequest request = ctx.bodyAsClass(AuthorizeRequest.class);
    AuthorizeResponse response = messageService.authorizeUser(request);
    ctx.result("OK");
  }

  private void boot(Context ctx) {
    BootNotificationRequest request = ctx.bodyAsClass(BootNotificationRequest.class);
    ctx.result("OK");
  }

  private void heartbeat(Context ctx) {
    HeartbeatRequest request = ctx.bodyAsClass(HeartbeatRequest.class);
    ctx.result("OK");
  }

  @Override
  public void registerRoutes(Javalin app) {
    app.post("/api/message/authorize", this::authorize);
    app.post("/api/message/boot", this::boot);
    app.get("/api/message/heartbeat", this::heartbeat);
  }
}
