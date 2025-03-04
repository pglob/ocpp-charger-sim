package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import lombok.EqualsAndHashCode;

/** A OCPP 1.6 MeterValues Response Message. */
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "MeterValuesResponse")
public class MeterValuesResponse extends OCPPMessageResponse implements Cloneable {
  // No fields are defined as per the protocol specification
  public MeterValuesResponse(MeterValues request) {
    super(request);
  }

  @Override
  protected MeterValuesResponse clone() {
    return (MeterValuesResponse) super.clone();
  }
}
