package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an OCPP 1.6 Authorize Response sent by the Central System to confirm the authorization
 * status of a Charge Point's idTag.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "AuthorizeResponse")
public class AuthorizeResponse extends OCPPMessageResponse implements Cloneable {
  @SerializedName("idTagInfo")
  private IdTagInfo idTagInfo;

  // Inner class to represent the idTagInfo object
  @Getter
  @Setter
  @AllArgsConstructor
  public static class IdTagInfo {

    @NotBlank(message = "AuthorizeResponse status is required and cannot be blank")
    @SerializedName("status")
    private AuthorizationStatus status; // One of: Accepted, Blocked, Expired, Invalid, ConcurrentTx
  }

  public AuthorizeResponse(Authorize request, IdTagInfo info) {
    super(request);
    this.idTagInfo = info;
  }

  public AuthorizeResponse(Authorize request, String status) {
    super(request);
    this.idTagInfo = new IdTagInfo(AuthorizationStatus.fromString(status));
  }

  @Override
  protected AuthorizeResponse clone() {
    return (AuthorizeResponse) super.clone();
  }
}
