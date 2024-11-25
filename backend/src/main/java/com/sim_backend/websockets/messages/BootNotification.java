/**
 * Represents an OCPP 1.6 Boot Notification Request sent by a Charge Point to provide its identity,
 * configuration, and status to the Central System.
 */
package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** A OCPP Boot Notification Request Message. */
@AllArgsConstructor
@Getter
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "BootNotification")
public final class BootNotification extends OCPPMessageRequest {

  /** The Charge Point's Vendor. */
  @SerializedName("chargePointVendor")
  private final String chargePointVendor;

  /** The Charge Point's Model. */
  @SerializedName("chargePointModel")
  private final String chargePointModel;

  /** The Charge Point's Serial Number. */
  @SerializedName("chargePointSerialNumber")
  private final String chargePointSerialNumber;

  /** The Charge Box's Serial Number. */
  @SerializedName("chargeBoxSerialNumber")
  private final String chargeBoxSerialNumber;

  /** Firmware Version. */
  @SerializedName("firmwareVersion")
  private final String firmwareVersion;

  /** ICCID (Integrated Circuit Card Identifier) . */
  @SerializedName("iccid")
  private final String iccid;

  /** IMSI (International Mobile Subscriber Identity). */
  @SerializedName("imsi")
  private final String imsi;

  /** Meter Type. */
  @SerializedName("meterType")
  private final String meterType;

  /** Meter Serial Number. */
  @SerializedName("meterSerialNumber")
  private final String meterSerialNumber;
}
