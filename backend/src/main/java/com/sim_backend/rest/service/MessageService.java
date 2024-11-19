package com.sim_backend.rest.service;

import com.sim_backend.rest.model.*;

public class MessageService {

  public MessageService() {}

  public AuthorizeResponse authorizeUser(AuthorizeRequest request) {
    // return new AuthorizeResponse("Authorized");
    return new AuthorizeResponse("test");
  }

  public HeartbeatResponse getHeartbeat(HeartbeatRequest request) {
    return new HeartbeatResponse("test");
  }

  public BootNotificationResponse getBootNotification(BootNotificationRequest request) {
    // return new BootNotificationResponse("Accepted", 30);
    return new BootNotificationResponse("test");
  }
}
