package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.TriggerMessageStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "TriggerMessageResponse")
public class TriggerMessageResponse extends OCPPMessageResponse implements Cloneable {
  private TriggerMessageStatus status;

  public TriggerMessageResponse(TriggerMessageStatus status) {
    this.status = status;
  }

  @Override
  protected TriggerMessageResponse clone() {
    return (TriggerMessageResponse) super.clone();
  }
}
