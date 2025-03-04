package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** A OCPP Status Notification Request Message. */
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "StatusNotification")
public final class StatusNotification extends OCPPMessageRequest implements Cloneable {

  @Min(value = 0, message = "StatusNotification connectorId must be a non-negative integer")
  @SerializedName("connectorId")
  private int connectorId;

  @NotNull(message = "Status Notification Error code is required and cannot be blank")
  @SerializedName("errorCode")
  private ChargePointErrorCode errorCode;

  @Size(max = 50, message = "StatusNotification Info must not exceed 50 characters")
  @SerializedName("info")
  private String info;

  @NotNull(message = "StatusNotification Status is required and cannot be blank")
  @SerializedName("status")
  private ChargePointStatus status;

  @SerializedName("timestamp")
  private ZonedDateTime timestamp;

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
      ZonedDateTime timestamp,
      String vendorId,
      String vendorErrorCode) {
    this.connectorId = connectorId;
    this.errorCode = errorCode;
    this.info = info.isEmpty() ? null : info;
    this.status = status;
    this.timestamp = timestamp;
    this.vendorId = vendorId.isEmpty() ? null : vendorId;
    this.vendorErrorCode = vendorErrorCode.isEmpty() ? null : vendorErrorCode;
  }

  @Override
  protected StatusNotification clone() {
    return (StatusNotification) super.clone();
  }
}
