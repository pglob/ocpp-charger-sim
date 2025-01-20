package com.sim_backend;

import com.sim_backend.state.SimulatorState;
import com.sim_backend.websockets.constants.BootNotificationConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Config Registry holds configuration and identification for the charge point and its simulator
 * state.
 */
@AllArgsConstructor
@Getter
@Setter
public class ConfigurationRegistry {

  /** ChangeConfiguration authorization key */
  private String authKey;

  /** ChangeConfiguration authorization key value */
  private String authKeyValue;

  /** Authorize response id tag */
  private String idTag;

  /** The ChargeBox's Serial Number */
  private String centralSystemUrl;

  /** Current state of the simulator */
  private SimulatorState simulatorState;

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
}
