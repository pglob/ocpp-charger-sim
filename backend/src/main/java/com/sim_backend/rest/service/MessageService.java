package com.sim_backend.rest.service;

import com.sim_backend.rest.model.*;

public class MessageService {

  public MessageService() {}

  public AuthorizeResponse authorizeUser(Authorize request) {
    return new AuthorizeResponse("Authorized");
  }

  public HeartBeatResponse getHeartbeat(HeartBeat request) {
    return new HeartBeatResponse();
  }

  public BootNotificationResponse getBootNotification(BootNotification request) {
    return new BootNotificationResponse("Accepted", 30);
  }
}
