package com.sim_backend.websockets.observers;

import com.sim_backend.websockets.enums.*;
import com.sim_backend.websockets.events.*;
import com.sim_backend.websockets.messages.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Authorize Observer is responsible for handling AuthorizeResponse message received from the
 * Central System.
 */
public class AuthorizeObserver implements OnOCPPMessageListener {

  private List<AuthorizationStatus> status = new ArrayList<>();

  // Return last authorization status
  public AuthorizationStatus getLastStatus() {
    if (status.isEmpty()) {
      System.err.println("There is no Authorization History...");
      return null;
    }
    return status.get(status.size() - 1);
  }

  /**
   * Process AuthorizeResponse based on status that is provided by Central System
   *
   * <p>If the message is not an instance of AuthorizeResponse, ClassCastException is thrown.
   *
   * @param message OCPP AuthorizeResponse Message
   * @throws ClassCastException if message is not an AuthorizeResponse
   */
  @Override
  public void onMessageReceived(OnOCPPMessage message) {
    if (!(message.getMessage() instanceof AuthorizeResponse response)) {
      throw new ClassCastException("Message is not an AuthorizeResponse");
    }
    switch (response.getIdTagInfo().getStatus()) {
      case ACCEPTED:
        System.out.println("Authorization Accepted.");
        break;
      case BLOCKED:
        System.err.println("Authorization Blocked for ID.");
        break;
      case EXPIRED:
        System.err.println("Authorization Expired for ID.");
        break;
      case INVALID:
        System.err.println("Invalid ID.");
        break;
      case CONCURRENT:
        System.err.println("Concurrent Transaction Occured.");
      default:
        System.err.println("Unknown Status Received.");
    }
    status.add(response.getIdTagInfo().getStatus());
  }
}
