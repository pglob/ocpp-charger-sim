package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** OCPP 1.6 RemoteStopTransaction Request */
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "RemoteStopTransaction")
public class RemoteStopTransaction extends OCPPMessageRequest implements Cloneable {
  @Min(value = 0, message = "RemoteStartTransaction connectorId must be greater than or equal to 0")
  @NotNull(message = "RemoteStopTransaction transactionId is required")
  @SerializedName("transactionId")
  private final int transactionId;

  public RemoteStopTransaction(int transactionId) {
    this.transactionId = transactionId;
  }

  @Override
  protected StartTransaction clone() {
    return (StartTransaction) super.clone();
  }
}
