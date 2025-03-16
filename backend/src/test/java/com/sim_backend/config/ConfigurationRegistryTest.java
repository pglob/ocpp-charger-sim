package com.sim_backend.config;

import static org.junit.jupiter.api.Assertions.*;

import com.sim_backend.websockets.enums.MeterValuesSampledData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigurationRegistryTest {

  private static final Path CONFIG_PATH = Path.of("config.properties");
  private static final Path BACKUP_PATH = Path.of("config.properties.bak");

  // Expected keys with the _0 suffix
  private static final String KEY_SUFFIX = "_0";
  private static final String PROP_ID_TAG = "idTag" + KEY_SUFFIX;
  private static final String PROP_CENTRAL_SYSTEM_URL = "centralSystemUrl" + KEY_SUFFIX;
  private static final String PROP_METER_VALUE_SAMPLE_INTERVAL =
      "meterValueSampleInterval" + KEY_SUFFIX;
  private static final String PROP_METER_VALUES_SAMPLED_DATA =
      "meterValuesSampledData" + KEY_SUFFIX;

  @BeforeEach
  public void backupConfig() throws IOException {
    if (Files.exists(CONFIG_PATH)) {
      Files.copy(CONFIG_PATH, BACKUP_PATH, StandardCopyOption.REPLACE_EXISTING);
    }
    Files.deleteIfExists(CONFIG_PATH);
    System.clearProperty(PROP_ID_TAG);
    System.clearProperty(PROP_CENTRAL_SYSTEM_URL);
    System.clearProperty(PROP_METER_VALUE_SAMPLE_INTERVAL);
    System.clearProperty(PROP_METER_VALUES_SAMPLED_DATA);
  }

  @AfterEach
  public void restoreConfig() throws IOException {
    Files.deleteIfExists(CONFIG_PATH);
    if (Files.exists(BACKUP_PATH)) {
      Files.copy(BACKUP_PATH, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
      Files.delete(BACKUP_PATH);
    }
    System.clearProperty(PROP_ID_TAG);
    System.clearProperty(PROP_CENTRAL_SYSTEM_URL);
    System.clearProperty(PROP_METER_VALUE_SAMPLE_INTERVAL);
    System.clearProperty(PROP_METER_VALUES_SAMPLED_DATA);
  }

  @Test
  public void testLoadConfigurationWithNewParameters() throws Exception {
    // Set system properties using the keys with the _0 suffix
    System.setProperty(PROP_ID_TAG, "newTag");
    System.setProperty(PROP_CENTRAL_SYSTEM_URL, "ws://new.example.com");
    System.setProperty(PROP_METER_VALUE_SAMPLE_INTERVAL, "40");
    System.setProperty(PROP_METER_VALUES_SAMPLED_DATA, "POWER_ACTIVE_IMPORT");

    ConfigurationRegistry registry = ConfigurationRegistry.loadConfiguration(0);
    assertEquals("newTag", registry.getIdTag());
    assertEquals("ws://new.example.com", registry.getCentralSystemUrl());
    assertEquals(40, registry.getMeterValueSampleInterval());
    assertEquals(MeterValuesSampledData.POWER_ACTIVE_IMPORT, registry.getMeterValuesSampledData());

    // Verify that the config file was created and that the keys contain the _0 suffix
    File configFile = CONFIG_PATH.toFile();
    assertTrue(configFile.exists(), "config file is created");
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(configFile)) {
      props.load(fis);
    }
    // The stored properties should use keys with _0 appended
    assertEquals("newTag", props.getProperty(PROP_ID_TAG));
    assertEquals("ws://new.example.com", props.getProperty(PROP_CENTRAL_SYSTEM_URL));
    assertEquals("40", props.getProperty(PROP_METER_VALUE_SAMPLE_INTERVAL));
    assertEquals("POWER_ACTIVE_IMPORT", props.getProperty(PROP_METER_VALUES_SAMPLED_DATA));
  }

  @Test
  public void testLoadConfigurationFromExistingConfigFile() throws Exception {
    Properties props = new Properties();
    props.setProperty(PROP_ID_TAG, "fileTag");
    props.setProperty(PROP_CENTRAL_SYSTEM_URL, "ws://file.example.com");
    props.setProperty(PROP_METER_VALUE_SAMPLE_INTERVAL, "50");
    props.setProperty(PROP_METER_VALUES_SAMPLED_DATA, "POWER_ACTIVE_IMPORT");
    try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH.toFile())) {
      props.store(fos, "Test configuration");
    }

    ConfigurationRegistry registry = ConfigurationRegistry.loadConfiguration(0);
    assertEquals("fileTag", registry.getIdTag());
    assertEquals("ws://file.example.com", registry.getCentralSystemUrl());
    assertEquals(50, registry.getMeterValueSampleInterval());
    assertEquals(MeterValuesSampledData.POWER_ACTIVE_IMPORT, registry.getMeterValuesSampledData());
  }

  @Test
  public void testLoadConfigurationWithDefaults() {
    // With no command-line or file-based properties, defaults should be used
    ConfigurationRegistry registry = ConfigurationRegistry.loadConfiguration(0);
    assertEquals("test", registry.getIdTag());
    assertEquals("ws://host.docker.internal:9000", registry.getCentralSystemUrl());
    assertEquals(30, registry.getMeterValueSampleInterval());
    assertEquals(
        MeterValuesSampledData.ENERGY_ACTIVE_IMPORT_REGISTER, registry.getMeterValuesSampledData());
  }
}
