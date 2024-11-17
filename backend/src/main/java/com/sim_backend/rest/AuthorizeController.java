package com.sim_backend.rest;
import io.javalin.Javalin;
import io.javalin.http.Context;
import com.sim_backend.websockets.messages.Authorize;


public class AuthorizeController extends ControllerBase{

    @Override
    public void registerRoutes(Javalin app) {
        app.post("/authorize", AuthorizeController::authorizeRequest);
    }

    private void authorizeRequest(Context ctx) {
        AuthorizeResponse response;
    }
}
