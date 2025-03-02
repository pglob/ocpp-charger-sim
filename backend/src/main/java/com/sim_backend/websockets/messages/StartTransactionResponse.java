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
 * Represents an OCPP 1.6 StartTransactionResponse sent by the Central System for transaction
 * details.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(
    messageCallID = OCPPMessage.CALL_ID_RESPONSE,
    messageName = "StartTransactionResponse")
public class StartTransactionResponse extends OCPPMessageResponse implements Cloneable {
  @SerializedName("transactionId")
  private int transactionId;

  @SerializedName("idTagInfo")
  private idTagInfo idTagInfo;

  @Getter
  @Setter
  @AllArgsConstructor
  public static class idTagInfo {

    @NotNull(message = "StartTransactionResponse Status is required and cannot be blank")
    @SerializedName("status")
    private AuthorizationStatus status; // Status of the idTag (e.g., Accepted, Blocked, etc.).
  }

  // Constructor
  public StartTransactionResponse(StartTransaction request, int transactionId, String idTagInfo) {
    super(request);
    this.transactionId = transactionId;
    this.idTagInfo = new idTagInfo(AuthorizationStatus.fromString(idTagInfo));
  }

  @Override
  protected StartTransactionResponse clone() {
    return (StartTransactionResponse) super.clone();
  }
}
