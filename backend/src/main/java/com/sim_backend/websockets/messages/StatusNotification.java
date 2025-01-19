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
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.OffsetDateTime;

/** A OCPP Status Notification Request Message. */
@AllArgsConstructor
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

  /*Not sure how to structure it yet,use default values now*/
  public StatusNotification() {
    this.connectorId = 0; 
    this.errorCode = ChargePointErrorCode.NO_ERROR; 
    this.info = ""; 
    this.status = ChargePointStatus.AVAILABLE; 
    this.timestamp = OffsetDateTime.now(); 
    this.vendorId = StatusNotificationConstants.CHARGE_POINT_VENDOR;
    this.vendorErrorCode = ""; 
}
}