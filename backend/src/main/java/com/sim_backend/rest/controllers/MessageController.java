package com.sim_backend.rest.controllers;

import com.sim_backend.rest.model.Authorize;
import com.sim_backend.rest.model.AuthorizeResponse;
import com.sim_backend.rest.model.BootNotification;
import com.sim_backend.rest.model.HeartBeat;
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
    Authorize request = ctx.bodyAsClass(Authorize.class);
    AuthorizeResponse response = messageService.authorizeUser(request);
    ctx.json(response);
  }

  private void boot(Context ctx) {
    BootNotification request = ctx.bodyAsClass(BootNotification.class);
    ctx.json(messageService.getBootNotification(request));
  }

  private void heartbeat(Context ctx) {
    HeartBeat request = ctx.bodyAsClass(HeartBeat.class);
    ctx.json(messageService.getHeartbeat(request));
  }

  @Override
  public void registerRoutes(Javalin app) {
    app.post("/api/message/authorize", this::authorize);
    app.post("/api/message/boot", this::boot);
    app.get("/api/message/heartbeat", this::heartbeat);
  }
}
