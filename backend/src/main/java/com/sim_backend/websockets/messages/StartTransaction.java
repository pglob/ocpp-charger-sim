package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/*
 * Represents an OCPP 1.6 StartTransaction Request sent by a Charge Point to initiate a charging transaction.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "StartTransaction")
public class StartTransaction extends OCPPMessageRequest {
  @SerializedName("connectorId")
  private int connectorId;

  @SerializedName("idTag")
  private String idTag;

  @SerializedName("meterStart")
  private int meterStart;

  @SerializedName("timestamp")
  private String timestamp;

  // Constructor
  public StartTransaction(int connectorId, String idTag, int meterStart, String timestamp) {
    this.connectorId = connectorId;
    this.idTag = idTag;
    this.meterStart = meterStart;
    this.timestamp = timestamp;
  }
}
