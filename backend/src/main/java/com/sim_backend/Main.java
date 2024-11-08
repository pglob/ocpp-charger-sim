package com.sim_backend;

import com.sim_backend.rest.TestMessageController;

import io.javalin.Javalin;

public class Main {
    /**
     * The main method that starts the Javalin web server.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        // Retrieve environment variables
        String host = System.getenv("FRONTEND_HOST")
            != null ? System.getenv("FRONTEND_HOST") : "localhost";
        String frontendPortString = System.getenv("FRONTEND_PORT")
            != null ? System.getenv("FRONTEND_PORT") : "3030";
        String backendPortString = System.getenv("BACKEND_PORT")
            != null ? System.getenv("BACKEND_PORT") : "8080";

        int frontendPort = Integer.parseInt(frontendPortString);
        int backendPort = Integer.parseInt(backendPortString);

        // Setup the REST API for the frontend to use
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                // Allow CORS from the frontend host and port
                cors.addRule(it -> {
                    it.allowHost("http://" + host + ":" + frontendPort);
                });
            });
        }).start(backendPort); // Start the server

        // Register a test route
        //TestMessageController.registerRoutes(app);
    }
}
