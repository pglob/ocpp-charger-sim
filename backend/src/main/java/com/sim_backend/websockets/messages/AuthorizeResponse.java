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
 * Represents an OCPP 1.6 Authorize Response sent by the Central System to confirm the authorization
 * status of a Charge Point's idTag.
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Getter
@Setter
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "AuthorizeResponse")
public class AuthorizeResponse extends OCPPMessageResponse {

  @SerializedName("idTagInfo")
  private IdTagInfo idTagInfo;

  // Inner class to represent the idTagInfo object
  @Getter
  @Setter
  @AllArgsConstructor
  public static class IdTagInfo {

    @SerializedName("status")
    private AuthorizationStatus status; // One of: Accepted, Blocked, Expired, Invalid, ConcurrentTx
  }

  public AuthorizeResponse(String status) {
    this.idTagInfo = new IdTagInfo(AuthorizationStatus.fromString(status));
  }
}
