package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import lombok.EqualsAndHashCode;

/**
 * Represents an OCPP 1.6 Heartbeat Request sent by a Charge Point to notify the Central System that
 * it is operational and connected.
 */
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "Heartbeat")
public final class Heartbeat extends OCPPMessageRequest implements Cloneable {
  /***
   * A Heartbeat Message.
   */
  public Heartbeat() {
    super();
  }

  @Override
  protected Heartbeat clone() {
    return (Heartbeat) super.clone();
  }
}
