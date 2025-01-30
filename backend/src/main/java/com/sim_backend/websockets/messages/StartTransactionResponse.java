package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an OCPP 1.6 StartTransactionResponse sent by the Central System for transaction
 * details.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "StartTransactionResponse")
public class StartTransactionResponse extends OCPPMessageResponse {
  @SerializedName("transactionId")
  private int transactionId;

  @SerializedName("idTagInfo")
  private IdTagInfo idTaginfo;

  @Getter
  @Setter
  @AllArgsConstructor
  public static class IdTagInfo {

    @SerializedName("status")
    private AuthorizationStatus status; // Status of the idTag (e.g., Accepted, Blocked, etc.).
  }

  // Constructor
  public StartTransactionResponse(int transactionId, String idTaginfo) {
    this.transactionId = transactionId;
    this.idTaginfo = new IdTagInfo(AuthorizationStatus.fromString(idTaginfo));
  }
}
