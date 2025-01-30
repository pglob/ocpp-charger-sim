package com.sim_backend.websockets.types;

import com.google.gson.JsonArray;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import lombok.EqualsAndHashCode;

/** An OCPP Call message. */
@EqualsAndHashCode(callSuper = true)
public class OCPPMessageRequest extends OCPPMessage {

  /**
   * Define the structure for a request message.
   *
   * @return The Generated JsonArray.
   */
  @Override
  public JsonArray generateMessage() {
    assert this.getClass().isAnnotationPresent(OCPPMessageInfo.class);
    OCPPMessageInfo messageInfo = this.getClass().getAnnotation(OCPPMessageInfo.class);
    JsonArray array = new JsonArray();
    array.add(messageInfo.messageCallID());
    array.add(this.getMessageID());
    array.add(messageInfo.messageName());
    array.add(GsonUtilities.getGson().toJsonTree(this));

    return array;
  }
}
