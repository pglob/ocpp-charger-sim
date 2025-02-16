package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/*
 * Represents an OCPP 1.6 StopTransaction Request sent by a Charge Point to initiate a Stop Charging transaction.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "StopTransaction")
public class StopTransaction extends OCPPMessageRequest {
  @SerializedName("idTag")
  private String idTag;

  /*
   * TODO : transactionData needs to be implemented
   */
  @NotNull(message = "StopTransaction transactionId is required")
  @SerializedName("transactionId")
  private int transactionId;

  @NotNull(message = "StopTransaction meterStop is required")
  @SerializedName("meterStop")
  private int meterStop;

  @NotNull(message = "StopTransaction timestamp is required")
  @SerializedName("timestamp")
  private String timestamp;

  // Constructor
  public StopTransaction(String idTag, int transactionId, int meterStop, String timestamp) {
    this.idTag = idTag;
    this.transactionId = transactionId;
    this.meterStop = meterStop;
    this.timestamp = timestamp;
  }
}
