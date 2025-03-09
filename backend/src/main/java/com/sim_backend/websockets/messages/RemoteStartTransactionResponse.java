package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.RemoteStartStopStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** OCPP 1.6 RemoteStartTransaction Response */
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "RemoteStartTransactionResponse")
public class RemoteStartTransactionResponse extends OCPPMessageResponse implements Cloneable {
  @NotNull(message = "RemoteStartTransactionResponse Status is required and cannot be blank")
  @SerializedName("status")
  private final RemoteStartStopStatus status;

  public RemoteStartTransactionResponse(RemoteStartTransaction request, String status) {
    super(request);
    this.status = RemoteStartStopStatus.fromString(status);
  }

  @Override
  protected RemoteStartTransactionResponse clone() {
    return (RemoteStartTransactionResponse) super.clone();
  }
}
