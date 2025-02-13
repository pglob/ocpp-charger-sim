package com.sim_backend.config;

import com.sim_backend.websockets.constants.BootNotificationConstants;
import com.sim_backend.websockets.enums.MeterValuesSampledData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Config Registry holds configuration and identification for the simulated charge point and its
 * state.
 */
@AllArgsConstructor
@Getter
@Setter
public class ConfigurationRegistry {

  /** Authorize response id tag */
  private String idTag;

  /** The Central System's url */
  private String centralSystemUrl;

  /** The Charge Point's Vendor. */
  private final String chargePointVendor = BootNotificationConstants.CHARGE_POINT_VENDOR;

  /** The Charge Point's Model. */
  private final String chargePointModel = BootNotificationConstants.CHARGE_POINT_MODEL;

  /** The Charge Point's Serial Number. */
  private final String chargePointSerialNumber =
      BootNotificationConstants.CHARGE_POINT_SERIAL_NUMBER;

  /** The Charge Box's Serial Number. */
  private final String chargeBoxSerialNumber = BootNotificationConstants.CHARGE_BOX_SERIAL_NUMBER;

  /** Firmware Version. */
  private final String firmwareVersion = BootNotificationConstants.FIRMWARE_VERSION;

  /** ICCID (Integrated Circuit Card Identifier) . */
  private final String iccid = BootNotificationConstants.ICCID;

  /** IMSI (International Mobile Subscriber Identity). */
  private final String imsi = BootNotificationConstants.IMSI;

  /** Meter Type. */
  private final String meterType = BootNotificationConstants.METER_TYPE;

  /** Meter Serial Number. */
  private final String meterSerialNumber = BootNotificationConstants.METER_SERIAL_NUMBER;

  /** A sample meter value interval. */
  private int MeterValueSampleInterval = 30;

  /** Sample meter value data. */
  private MeterValuesSampledData meterValuesSampledData =
      MeterValuesSampledData.ENERGY_ACTIVE_IMPORT_REGISTER;

  public ConfigurationRegistry(String idTag, String centralSystemUrl) {
    this.idTag = idTag;
    this.centralSystemUrl = centralSystemUrl;
  }
}
