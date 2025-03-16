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

    int numberOfChargers = 3;

    // Create chargers
    Charger[] chargers = new Charger[numberOfChargers];
    for (int i = 0; i < numberOfChargers; i++) {
      chargers[i] = new Charger(i + 1); // id starts at 1
    }

    // Register REST API controllers and routes
    registerRoutes(app, chargers);

    // Start the charger
    for (Charger charger : chargers) {
      charger.boot();
    }
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
   * @param chargers the array of Charger instances
   */
  private static void registerRoutes(Javalin app, Charger[] chargers) {
    ControllerBase messageController = new MessageController(app, chargers);
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
