/**
 * Represents an OCPP 1.6 Status Notification Request sent by a Charge Point to provide its identity,
 * configuration, and status to the Central System.
 */
package com.sim_backend.websockets.messages;


import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import lombok.Getter;
import java.time.OffsetDateTime;

/** A OCPP Status Notification Request Message. */
@Getter
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "StatusNotification")
public final class StatusNotification extends OCPPMessageRequest {

  @SerializedName("connectorId")
  private int connectorId;

  @SerializedName("errorCode")
  private ChargePointErrorCode errorCode;

  @SerializedName("info")
  private String info;

  @SerializedName("status")
  private ChargePointStatus status;

  @SerializedName("timestamp")
  private OffsetDateTime timestamp;

  @SerializedName("vendorId")
  private String vendorId;

  @SerializedName("vendorErrorCode")
  private String vendorErrorCode;

  public StatusNotification(int connectorId, ChargePointErrorCode errorCode, String info, ChargePointStatus status, OffsetDateTime timestamp, String vendorId, String vendorErrorCode) {
    this.connectorId = connectorId;
    this.errorCode = errorCode;
    this.info = info != null ? info : "";
    this.status = status;
    this.timestamp = timestamp != null ? timestamp : OffsetDateTime.now();
    this.vendorId = vendorId != null ? vendorId : "";
    this.vendorErrorCode = vendorErrorCode != null ? vendorErrorCode : "";
  }
}