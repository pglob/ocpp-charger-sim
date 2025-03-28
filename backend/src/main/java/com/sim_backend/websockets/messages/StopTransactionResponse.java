package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an OCPP 1.6 StopTransactionResponse sent by the Central System for transaction
 * details.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "StopTransactionResponse")
public class StopTransactionResponse extends OCPPMessageResponse implements Cloneable {
  @SerializedName("idTagInfo")
  private idTagInfo idTagInfo;

  @Getter
  @Setter
  @AllArgsConstructor
  public static class idTagInfo {

    @NotNull(message = "StopTransactionResponse status is required")
    @SerializedName("status")
    private AuthorizationStatus status; // Status of the idTag (e.g., Accepted, Blocked, etc.).
  }

  // Constructor
  public StopTransactionResponse(StopTransaction request, String idTagInfo) {
    super(request);
    this.idTagInfo = new idTagInfo(AuthorizationStatus.fromString(idTagInfo));
  }

  @Override
  protected StopTransactionResponse clone() {
    return (StopTransactionResponse) super.clone();
  }
}
