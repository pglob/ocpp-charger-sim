package com.sim_backend.websockets.messages;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.types.OCPPMessageRequest;

/** A OCPP Boot Notification Message. */
@OCPPMessageInfo(messageName = "BootNotification")
public final class BootNotification extends OCPPMessageRequest {
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
   * @param chargePVendor The Charge Point Vendor.
   * @param chargePModel The Charge Point Model.
   * @param chargePSN THe Charge Point Serial Number.
   * @param chargeboxSN The Charge Box Serial Number.
   * @param fwversion The Firmware Version.
   * @param inICCID The ICCID.
   * @param inIMSI The IMSI.
   * @param mType The Meter Type.
   * @param mSN The Meter Serial Number.
   */
  public BootNotification(
      final String chargePVendor,
      final String chargePModel,
      final String chargePSN,
      final String chargeboxSN,
      final String fwversion,
      final String inICCID,
      final String inIMSI,
      final String mType,
      final String mSN) {
    this.chargePointVendor = chargePVendor;
    this.chargePointModel = chargePModel;
    this.chargePointSerialNumber = chargePSN;
    this.chargeBoxSerialNumber = chargeboxSN;
    this.firmwareVersion = fwversion;
    this.iccid = inICCID;
    this.imsi = inIMSI;
    this.meterType = mType;
    this.meterSerialNumber = mSN;
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
