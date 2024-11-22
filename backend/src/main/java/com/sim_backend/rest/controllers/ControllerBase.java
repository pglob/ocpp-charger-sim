package com.sim_backend.rest.controllers;

import io.javalin.Javalin;

public abstract class ControllerBase {

  protected final Javalin app;

  public ControllerBase(Javalin app) {
    this.app = app;
  }

  /**
   * Registers all the routes handled by the controller.
   *
   * @param app The Javalin application instance
   */
  public abstract void registerRoutes(io.javalin.Javalin app);
}
