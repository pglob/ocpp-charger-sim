package com.sim_backend.websockets.types;

import com.google.gson.JsonArray;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.annotations.OcppMessageInfo;

/** An OCPP Call message. */
public class OcppMessageRequest extends OcppMessage {

  /**
   * Define the structure for a request message.
   *
   * @return The Generated JsonArray.
   */
  @Override
  public JsonArray generateMessage() {
    assert this.getClass().isAnnotationPresent(OcppMessageInfo.class);
    OcppMessageInfo messageInfo = this.getClass().getAnnotation(OcppMessageInfo.class);
    JsonArray array = new JsonArray();
    array.add(messageInfo.messageCallId());
    array.add(this.getMessageId());
    array.add(messageInfo.messageName());
    array.add(GsonUtilities.getGson().toJsonTree(this));

    return array;
  }
}
