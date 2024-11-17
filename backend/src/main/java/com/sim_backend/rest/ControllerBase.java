package com.sim_backend.rest;

import io.javalin.http.Context;

public abstract class ControllerBase {

    /**
     * Registers all the routes handled by the controller.
     *
     * @param app The Javalin application instance
     */
    public abstract void registerRoutes(io.javalin.Javalin app);

    /**
     * A helper method to send a successful JSON response.
     *
     * @param ctx    The Javalin context
     * @param object The object to be serialized into JSON
     */
    protected void sendJsonResponse(Context ctx, Object object) {
        ctx.json(object).status(200);
    }

    /**
     * A helper method to send an error response.
     *
     * @param ctx       The Javalin context
     * @param status    The HTTP status code
     * @param message   The error message
     */
    protected void sendErrorResponse(Context ctx, int status, String message) {
        ctx.status(status).json(new ErrorResponse(message));
    }

    /**
     * A simple inner class to structure error responses.
     */
    protected static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}

