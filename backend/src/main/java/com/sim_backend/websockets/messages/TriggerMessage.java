package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.MessageTrigger;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "TriggerMessage")
public final class TriggerMessage extends OCPPMessageRequest implements Cloneable {

  @NotNull(message = "TriggerMessage requestedMessage is required")
  @SerializedName("requestedMessage")
  private MessageTrigger requestedMessage;

  @SerializedName("connectorId")
  private Integer connectorId;

  public TriggerMessage(MessageTrigger requestedMessage) {
    this.requestedMessage = requestedMessage;
  }

  public TriggerMessage(MessageTrigger requestedMessage, Integer connectorId) {
    this.requestedMessage = requestedMessage;
    this.connectorId = connectorId;
  }

  @Override
  protected TriggerMessage clone() {
    return (TriggerMessage) super.clone();
  }
}