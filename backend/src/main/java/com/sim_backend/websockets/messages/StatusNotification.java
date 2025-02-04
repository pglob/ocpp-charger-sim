package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** A OCPP Status Notification Request Message. */
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "StatusNotification")
public final class StatusNotification extends OCPPMessageRequest {

  @NotNull(message = "StatusNotification connectorId is required")
  @SerializedName("connectorId")
  private int connectorId;

  @NotBlank(message = "Status Notification Error code is required and cannot be blank")
  @SerializedName("errorCode")
  private ChargePointErrorCode errorCode;

  @Size(max = 50, message = "StatusNotification Info must not exceed 50 characters")
  @SerializedName("info")
  private String info;

  @NotBlank(message = "StatusNotification Status is required and cannot be blank")
  @SerializedName("status")
  private ChargePointStatus status;

  @SerializedName("timestamp")
  private OffsetDateTime timestamp;

  @Size(max = 255, message = "StatusNotification Vendor ID must not exceed 255 characters")
  @SerializedName("vendorId")
  private String vendorId;

  @Size(max = 50, message = "StatusNotification Vendor error code must not exceed 50 characters")
  @SerializedName("vendorErrorCode")
  private String vendorErrorCode;

  public StatusNotification(
      int connectorId,
      ChargePointErrorCode errorCode,
      String info,
      ChargePointStatus status,
      OffsetDateTime timestamp,
      String vendorId,
      String vendorErrorCode) {
    this.connectorId = connectorId;
    this.errorCode = errorCode;
    this.info = info != null ? info : "";
    this.status = status;
    this.timestamp = timestamp;
    this.vendorId = vendorId != null ? vendorId : "";
    this.vendorErrorCode = vendorErrorCode != null ? vendorErrorCode : "";
  }
}
