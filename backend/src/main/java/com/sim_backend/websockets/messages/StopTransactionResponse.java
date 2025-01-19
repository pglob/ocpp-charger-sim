package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an OCPP 1.6 StopTransactionResponse sent by the Central System for transaction
 * details.
 */
@Getter
@Setter
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "StopTransactionResponse")
public class StopTransactionResponse extends OCPPMessageResponse {
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
  public StopTransactionResponse(String idTaginfo) {
    this.idTaginfo = new IdTagInfo(AuthorizationStatus.fromString(idTaginfo));
  }
}
