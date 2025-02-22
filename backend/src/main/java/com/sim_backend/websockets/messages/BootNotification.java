/**
 * Represents an OCPP 1.6 Boot Notification Request sent by a Charge Point to provide its identity,
 * configuration, and status to the Central System.
 */
package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.constants.BootNotificationConstants;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** A OCPP Boot Notification Request Message. */
@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "BootNotification")
public final class BootNotification extends OCPPMessageRequest implements Cloneable {

  /** The Charge Point's Vendor. */
  @NotBlank(message = "BootNotification chargePointVendor is required")
  @Size(max = 20, message = "BootNotification chargePointVendor must not exceed 20 characters")
  @SerializedName("chargePointVendor")
  private final String chargePointVendor;

  /** The Charge Point's Model. */
  @NotBlank(message = "BootNotification chargePointModel is required")
  @Size(max = 20, message = "BootNotification chargePointModel must not exceed 20 characters")
  @SerializedName("chargePointModel")
  private final String chargePointModel;

  /** The Charge Point's Serial Number. */
  @Size(
      max = 25,
      message = "BootNotification chargePointSerialNumber must not exceed 25 characters")
  @SerializedName("chargePointSerialNumber")
  private final String chargePointSerialNumber;

  /** The Charge Box's Serial Number. */
  @Size(max = 25, message = "BootNotification chargeBoxSerialNumber must not exceed 25 characters")
  @SerializedName("chargeBoxSerialNumber")
  private final String chargeBoxSerialNumber;

  /** Firmware Version. */
  @Size(max = 50, message = "BootNotification firmwareVersion must not exceed 50 characters")
  @SerializedName("firmwareVersion")
  private final String firmwareVersion;

  /** ICCID (Integrated Circuit Card Identifier) . */
  @Size(max = 20, message = "BootNotification ICCID must not exceed 20 characters")
  @SerializedName("iccid")
  private final String iccid;

  /** IMSI (International Mobile Subscriber Identity). */
  @Size(max = 20, message = "BootNotification IMSI must not exceed 20 characters")
  @SerializedName("imsi")
  private final String imsi;

  /** Meter Type. */
  @Size(max = 25, message = "BootNotification meterType must not exceed 25 characters")
  @SerializedName("meterType")
  private final String meterType;

  /** Meter Serial Number. */
  @Size(max = 25, message = "BootNotification meterSerialNumber must not exceed 25 characters")
  @SerializedName("meterSerialNumber")
  private final String meterSerialNumber;

  /**
   * Default constructor for BootNotification. Initializes all fields using the predefined constants
   * from {@link BootNotificationConstants}.
   */
  public BootNotification() {
    this.chargePointVendor = BootNotificationConstants.CHARGE_POINT_VENDOR;
    this.chargePointModel = BootNotificationConstants.CHARGE_POINT_MODEL;
    this.chargePointSerialNumber = BootNotificationConstants.CHARGE_POINT_SERIAL_NUMBER;
    this.chargeBoxSerialNumber = BootNotificationConstants.CHARGE_BOX_SERIAL_NUMBER;
    this.firmwareVersion = BootNotificationConstants.FIRMWARE_VERSION;
    this.iccid = BootNotificationConstants.ICCID;
    this.imsi = BootNotificationConstants.IMSI;
    this.meterType = BootNotificationConstants.METER_TYPE;
    this.meterSerialNumber = BootNotificationConstants.METER_SERIAL_NUMBER;
  }

  @Override
  protected Authorize clone() {
    return (Authorize) super.clone();
  }
}
