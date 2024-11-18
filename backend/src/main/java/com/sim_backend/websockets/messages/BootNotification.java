package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OcppMessageInfo;
import com.sim_backend.websockets.types.OcppMessageRequest;

/** A OCPP Boot Notification Message. */
@OcppMessageInfo(messageName = "BootNotification")
public final class BootNotification extends OcppMessageRequest {
  /** The Charge Point's Vendor. */
  private final String chargePointVendor;

  /** The Charge Point's Model. */
  private final String chargePointModel;

  /** The Charge Point's Serial Number. */
  private final String chargePointSerialNumber;

  /** The Charge Box's Serial Number. */
  private final String chargeBoxSerialNumber;

  /** Firmware Version. */
  private final String firmwareVersion;

  /** ICCID. */
  private final String iccid;

  /** IMSI. */
  private final String imsi;

  /** Meter Type. */
  private final String meterType;

  /** Meter Serial Number. */
  private final String meterSerialNumber;

  /**
   * Create a Boot Notification Message.
   *
   * @param chargePointVdr The Charge Point Vendor.
   * @param chargePointMdl The Charge Point Model.
   * @param chargePointSn THe Charge Point Serial Number.
   * @param chargeBoxSn The Charge Box Serial Number.
   * @param fwVersion The Firmware Version.
   * @param inIccid The ICCID.
   * @param inImsi The IMSI.
   * @param mtrType The Meter Type.
   * @param mtrSn The Meter Serial Number.
   */
  public BootNotification(
      final String chargePointVdr,
      final String chargePointMdl,
      final String chargePointSn,
      final String chargeBoxSn,
      final String fwVersion,
      final String inIccid,
      final String inImsi,
      final String mtrType,
      final String mtrSn) {
    this.chargePointVendor = chargePointVdr;
    this.chargePointModel = chargePointMdl;
    this.chargePointSerialNumber = chargePointSn;
    this.chargeBoxSerialNumber = chargeBoxSn;
    this.firmwareVersion = fwVersion;
    this.iccid = inIccid;
    this.imsi = inImsi;
    this.meterType = mtrType;
    this.meterSerialNumber = mtrSn;
  }

  /**
   * Get the Charge Point's Vendor.
   *
   * @return The Charge Points Vendor.
   */
  public String getChargePointVendor() {
    return chargePointVendor;
  }

  /**
   * Get the Charge Point's Model.
   *
   * @return The Charge Point's Model.
   */
  public String getChargePointModel() {
    return chargePointModel;
  }

  /**
   * Get the Charge Point's Serial Number.
   *
   * @return The Charge Point Serial Number.
   */
  public String getChargePointSerialNumber() {
    return chargePointSerialNumber;
  }

  /**
   * Get the Charge Box's Serial Number.
   *
   * @return The Charge Box Serial Number.
   */
  public String getChargeBoxSerialNumber() {
    return chargeBoxSerialNumber;
  }

  /**
   * Get the Firmware Version.
   *
   * @return The Firmware Version.
   */
  public String getFirmwareVersion() {
    return firmwareVersion;
  }

  /**
   * Get the ICCID.
   *
   * @return THE ICCID.
   */
  public String getIccid() {
    return iccid;
  }

  /**
   * Get the IMSI.
   *
   * @return The IMSI.
   */
  public String getImsi() {
    return imsi;
  }

  /**
   * Get the Meter Type.
   *
   * @return The Meter Type
   */
  public String getMeterType() {
    return meterType;
  }

  /**
   * Get the Meter Serial Number.
   *
   * @return The Meter's Serial Number.
   */
  public String getMeterSerialNumber() {
    return meterSerialNumber;
  }
}
