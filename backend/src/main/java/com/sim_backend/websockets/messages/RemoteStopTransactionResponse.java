package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.RemoteStartStopStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** OCPP 1.6 RemoteStopTransaction Response */
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "RemoteStopTransactionResponse")
public class RemoteStopTransactionResponse extends OCPPMessageResponse implements Cloneable {
  @NotNull(message = "RemoteStopTransactionResponse Status is required and cannot be blank")
  @SerializedName("status")
  private final RemoteStartStopStatus status;

  public RemoteStopTransactionResponse(RemoteStopTransaction request, String status) {
    super(request);
    this.status = RemoteStartStopStatus.fromString(status);
  }

  @Override
  protected RemoteStopTransactionResponse clone() {
    return (RemoteStopTransactionResponse) super.clone();
  }
}
