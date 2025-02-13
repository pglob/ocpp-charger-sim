package com.sim_backend;

import com.sim_backend.charger.Charger;
import com.sim_backend.rest.TestMessageController;
import com.sim_backend.rest.controllers.ControllerBase;
import com.sim_backend.rest.controllers.MessageController;
import io.javalin.Javalin;

/** The entry into our program. */
public final class Main {
  private Main() {}

  public static void main(final String[] args) {
    // Initialize environment variables and configurations
    AppConfig config = loadConfig();

    // Start Javalin server
    Javalin app = initializeFrontendAPI(config);

    // Create the charger
    Charger charger = new Charger();

    // Register REST API controllers and routes
    registerRoutes(app, charger);

    // Start the charger
    charger.Boot();
  }

  /**
   * Load configuration from environment variables or defaults.
   *
   * @return configuration object
   */
  private static AppConfig loadConfig() {
    String host =
        System.getenv("FRONTEND_HOST") != null ? System.getenv("FRONTEND_HOST") : "localhost";
    int frontendPort =
        Integer.parseInt(
            System.getenv("FRONTEND_PORT") != null ? System.getenv("FRONTEND_PORT") : "3030");
    int backendPort =
        Integer.parseInt(
            System.getenv("BACKEND_PORT") != null ? System.getenv("BACKEND_PORT") : "8080");

    return new AppConfig(host, frontendPort, backendPort);
  }

  /**
   * Initialize and start the Javalin web server.
   *
   * @param config configuration object
   * @return initialized Javalin instance
   */
  private static Javalin initializeFrontendAPI(AppConfig config) {
    return Javalin.create(
            cfg -> {
              cfg.bundledPlugins.enableCors(
                  cors ->
                      cors.addRule(
                          // TODO: Determine best CORS policy or swap to reverse proxy
                          it -> it.anyHost()));
            })
        .start(config.backendPort);
  }

  /**
   * Register API routes with the Javalin app.
   *
   * @param app the Javalin app
   * @param wsClient the WebSocket client
   */
  private static void registerRoutes(Javalin app, Charger charger) {
    ControllerBase messageController = new MessageController(app, charger);
    messageController.registerRoutes(app);
    TestMessageController.registerRoutes(app);
  }

  /** Configuration object to hold server settings. */
  private static class AppConfig {
    final String host;
    final int frontendPort;
    final int backendPort;

    AppConfig(String host, int frontendPort, int backendPort) {
      this.host = host;
      this.frontendPort = frontendPort;
      this.backendPort = backendPort;
    }
  }
}
