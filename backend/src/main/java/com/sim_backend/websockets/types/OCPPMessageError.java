package com.sim_backend.websockets.types;

import com.google.gson.JsonArray;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;

public class OCPPMessageError extends OCPPMessage {
  private transient String errorDescription;

  protected OCPPMessageError(final String errDescription) {
    this.errorDescription = errDescription;
  }

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
    array.add(errorDescription);
    array.add(GsonUtilities.getGson().toJsonTree(this));

    return array;
  }
}
