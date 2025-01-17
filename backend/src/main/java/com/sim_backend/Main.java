package com.sim_backend;

import com.sim_backend.rest.TestMessageController;
import com.sim_backend.rest.controllers.ControllerBase;
import com.sim_backend.rest.controllers.MessageController;
import com.sim_backend.state.SimulatorState;
import com.sim_backend.state.SimulatorStateMachine;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.messages.BootNotificationResponse;
import com.sim_backend.websockets.observers.BootNotificationObserver;
import io.javalin.Javalin;
import java.net.URI;

/** The entry into our program. */
public final class Main {
  private Main() {}

  public static void main(final String[] args) {
    // Initialize environment variables and configurations
    SimConfig config = loadConfig();

    // Start Javalin server
    Javalin app = initializeFrontendAPI(config);

    // Initialize WebSocket client
    // TODO: Get central system URI from frontend or command line
    OCPPWebSocketClient wsClient = initializeWebSocketClient("dummy-server", "9000");

    // Register REST API controllers and routes
    registerRoutes(app, wsClient);

    // Create Simulator State
    SimulatorStateMachine stateMachine = new SimulatorStateMachine();
    // Create Observers
    // TODO: Add other observers
    BootNotificationObserver bootObserver = new BootNotificationObserver(wsClient, stateMachine);

    // Register observers with the state machine
    stateMachine.addObserver(bootObserver);

    // Register observers with the Websocket client
    wsClient.onReceiveMessage(BootNotificationResponse.class, bootObserver);

    // Boot the charger
    stateMachine.transition(SimulatorState.BootingUp);

    // Run the main simulator loop
    SimulatorLoop.runSimulatorLoop(wsClient);

  }

  /**
   * Load configuration from environment variables or defaults.
   *
   * @return configuration object
   */
  private static SimConfig loadConfig() {
    String host =
        System.getenv("FRONTEND_HOST") != null ? System.getenv("FRONTEND_HOST") : "localhost";
    int frontendPort =
        Integer.parseInt(
            System.getenv("FRONTEND_PORT") != null ? System.getenv("FRONTEND_PORT") : "3030");
    int backendPort =
        Integer.parseInt(
            System.getenv("BACKEND_PORT") != null ? System.getenv("BACKEND_PORT") : "8080");

    return new SimConfig(host, frontendPort, backendPort);
  }

  /**
   * Initialize and start the Javalin web server.
   *
   * @param config configuration object
   * @return initialized Javalin instance
   */
  private static Javalin initializeFrontendAPI(SimConfig config) {
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
   * Initialize the WebSocket client.
   *
   * @return initialized OCPPWebSocketClient instance
   */
  private static OCPPWebSocketClient initializeWebSocketClient(String name, String port) {
    URI wsUri = URI.create("ws://" + name + ":" + port);
    return new OCPPWebSocketClient(wsUri);
  }

  /**
   * Register API routes with the Javalin app.
   *
   * @param app the Javalin app
   * @param wsClient the WebSocket client
   */
  private static void registerRoutes(Javalin app, OCPPWebSocketClient wsClient) {
    ControllerBase messageController = new MessageController(app, wsClient);
    messageController.registerRoutes(app);
    TestMessageController.registerRoutes(app);
  }

  /** Configuration object to hold server settings. */
  private static class SimConfig {
    final String host;
    final int frontendPort;
    final int backendPort;

    SimConfig(String host, int frontendPort, int backendPort) {
      this.host = host;
      this.frontendPort = frontendPort;
      this.backendPort = backendPort;
    }
  }
}
