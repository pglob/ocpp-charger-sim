package com.sim_backend.config;

import com.sim_backend.websockets.constants.BootNotificationConstants;
import com.sim_backend.websockets.enums.MeterValuesSampledData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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

  private static final String CONFIG_FILE_PATH = "config.properties";

  public static ConfigurationRegistry loadConfiguration() {
      Properties props = new Properties();
      File configFile = new File(CONFIG_FILE_PATH);
      boolean updatedFromCmd = false;

      // Default values
      String idTag = "temptag";
      String centralSystemUrl = "ws://host.docker.internal:9000";
      int meterValueSampleInterval = 30;
      MeterValuesSampledData meterValuesSampledData = MeterValuesSampledData.ENERGY_ACTIVE_IMPORT_REGISTER;

      String cmdIdTag = System.getProperty("idTag");
      if (cmdIdTag != null && !cmdIdTag.isEmpty()) {
          idTag = cmdIdTag;
          updatedFromCmd = true;
      }
      String cmdCentralSystemUrl = System.getProperty("centralSystemUrl");
      if (cmdCentralSystemUrl != null && !cmdCentralSystemUrl.isEmpty()) {
          centralSystemUrl = cmdCentralSystemUrl;
          updatedFromCmd = true;
      }
      String cmdInterval = System.getProperty("meterValueSampleInterval");
      if (cmdInterval != null && !cmdInterval.isEmpty()) {
          try {
              meterValueSampleInterval = Integer.parseInt(cmdInterval);
              updatedFromCmd = true;
          } catch (NumberFormatException e) {
              System.err.println("Invalid meterValueSampleInterval: " + cmdInterval);
          }
      }
      String cmdSampledData = System.getProperty("meterValuesSampledData");
      if (cmdSampledData != null && !cmdSampledData.isEmpty()) {
          try {
              meterValuesSampledData = MeterValuesSampledData.valueOf(cmdSampledData);
              updatedFromCmd = true;
          } catch (IllegalArgumentException e) {
              System.err.println("Invalid meterValuesSampledData: " + cmdSampledData);
          }
      }

      // If no command line arguments, try to load from config file
      if (!updatedFromCmd && configFile.exists()) {
          try (FileInputStream fis = new FileInputStream(configFile)) {
              props.load(fis);
              idTag = props.getProperty("idTag", idTag);
              centralSystemUrl = props.getProperty("centralSystemUrl", centralSystemUrl);
              meterValueSampleInterval = Integer.parseInt(
                      props.getProperty("meterValueSampleInterval", String.valueOf(meterValueSampleInterval)));
              String fileSampledData = props.getProperty("meterValuesSampledData", meterValuesSampledData.name());
              try {
                  meterValuesSampledData = MeterValuesSampledData.valueOf(fileSampledData);
              } catch (IllegalArgumentException e) {
                  // If the configuration file contains invalid data, keep the default value
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
      } else if (updatedFromCmd) {
          // If command line arguments are present, save to config file
          props.setProperty("idTag", idTag);
          props.setProperty("centralSystemUrl", centralSystemUrl);
          props.setProperty("meterValueSampleInterval", String.valueOf(meterValueSampleInterval));
          props.setProperty("meterValuesSampledData", meterValuesSampledData.name());
          try (FileOutputStream fos = new FileOutputStream(configFile)) {
              props.store(fos, "Simulated Charge Point Configuration");
          } catch (IOException e) {
              e.printStackTrace();
          }
      }

      ConfigurationRegistry registry = new ConfigurationRegistry(idTag, centralSystemUrl);
      registry.setMeterValueSampleInterval(meterValueSampleInterval);
      registry.setMeterValuesSampledData(meterValuesSampledData);

      return registry;
  }
}
