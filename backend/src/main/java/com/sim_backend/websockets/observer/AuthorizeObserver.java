package com.sim_backend.websockets.observer;

import com.sim_backend.websockets.events.*;
import com.sim_backend.websockets.exceptions.*;
import com.sim_backend.websockets.messages.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorizeObserver {
  private List<OCPPMessageHandler> observers = new ArrayList<>();

  public AuthorizeObserver() {}

  public void addObserver(OCPPMessageHandler observer) {
    observers.add(observer);
  }

  public void notifyAuth(AuthorizeResponse response) {
    for (OCPPMessageHandler observer : observers) {
      if (response != null) {
        observer.onAuthorizeReceived(response);
      } else {
        throw new IllegalAuthorizeResponse("Invalid Response Received : NULL");
      }
      observer.onMessageReceived(new OnOCPPMessage(response));
    }
  }
}
