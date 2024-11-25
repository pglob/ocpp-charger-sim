package com.sim_backend.rest;

import io.javalin.Javalin;
import io.javalin.http.Context;

/** A test rest API. */
public final class TestMessageController {

  private TestMessageController() {}

  /**
   * Registers the routes for test-related endpoints.
   *
   * @param app the Javalin app instance to add routes to
   */
  public static void registerRoutes(final Javalin app) {
    app.get("/api/test", TestMessageController::getTestMessage);
  }

  /**
   * Handles GET requests to "/api/test".
   *
   * @param ctx the Javalin context
   */
  public static void getTestMessage(final Context ctx) {
    ctx.result("Hello from the backend!");
  }
}
