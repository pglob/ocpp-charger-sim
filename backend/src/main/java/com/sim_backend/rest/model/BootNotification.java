package com.sim_backend.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/** A OCPP Boot Notification Request Message. */
@Getter
@SuperBuilder
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "BootNotification")
public final class BootNotification extends OCPPMessage {

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

  // Jackson annotations for deserialization
  @JsonCreator
  public BootNotification(
      @JsonProperty("chargePointVendor") String chargePointVendor,
      @JsonProperty("chargePointModel") String chargePointModel,
      @JsonProperty("chargePointSerialNumber") String chargePointSerialNumber,
      @JsonProperty("chargeBoxSerialNumber") String chargeBoxSerialNumber,
      @JsonProperty("firmwareVersion") String firmwareVersion,
      @JsonProperty("iccid") String iccid,
      @JsonProperty("imsi") String imsi,
      @JsonProperty("meterType") String meterType,
      @JsonProperty("meterSerialNumber") String meterSerialNumber) {
    this.chargePointVendor = chargePointVendor;
    this.chargePointModel = chargePointModel;
    this.chargePointSerialNumber = chargePointSerialNumber;
    this.chargeBoxSerialNumber = chargeBoxSerialNumber;
    this.firmwareVersion = firmwareVersion;
    this.iccid = iccid;
    this.imsi = imsi;
    this.meterType = meterType;
    this.meterSerialNumber = meterSerialNumber;
  }
}
