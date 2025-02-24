package com.sim_backend.websockets.types;

import com.google.gson.JsonArray;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/** An OCPP CallResult message. */
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class OCPPMessageResponse extends OCPPMessage implements Cloneable {
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
    array.add(GsonUtilities.getGson().toJsonTree(this));

    return array;
  }

  @Override
  protected OCPPMessageResponse clone() {
    return (OCPPMessageResponse) super.clone();
  }
}
