/**
 * Represents an OCPP 1.6 Authorize Response sent by the Central System
 * to confirm the authorization status of a Charge Point's idTag.
 */
package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.OCPPMessage;
import com.sim_backend.websockets.OCPPMessageInfo;
import com.sim_backend.websockets.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_RESPONSE, messageName = "AuthorizeResponse")
public class AuthorizeResponse extends OCPPMessage {

  @SerializedName("status")
  private Status status; // Status that can be either Accepted, Pending, or Rejected.
}
