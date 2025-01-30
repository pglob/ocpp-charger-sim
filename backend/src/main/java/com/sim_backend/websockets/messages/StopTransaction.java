package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import lombok.Getter;
import lombok.Setter;

/*
 * Represents an OCPP 1.6 StopTransaction Request sent by a Charge Point to initiate a Stop Charging transaction.
 */
@Getter
@Setter
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "StopTransaction")
public class StopTransaction extends OCPPMessageRequest {
  /*
   * TODO : transactionData needs to be implemented
   */
  @SerializedName("transactionId")
  private int transactionId;

  @SerializedName("meterStop")
  private int meterStop;

  @SerializedName("timestamp")
  private String timestamp;

  // Constructor
  public StopTransaction(int transactionId, int meterStop, String timestamp) {
    this.transactionId = transactionId;
    this.meterStop = meterStop;
    this.timestamp = timestamp;
  }
}
