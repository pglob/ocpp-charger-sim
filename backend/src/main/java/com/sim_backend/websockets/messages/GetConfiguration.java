package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/** Represents an OCPP 1.6 GetConfiguration Request from Central System to a Charge Point */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "GetConfiguration")
public class GetConfiguration extends OCPPMessageRequest {
  @NotEmpty(message = "ChangeConfiguration key is required")
  @SerializedName("key")
  private final List<String> key;

  public GetConfiguration(List<String> key) {
    this.key = key;
  }
}
