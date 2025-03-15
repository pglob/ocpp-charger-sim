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

  @BeforeEach
  public void backupConfig() throws IOException {
    if (Files.exists(CONFIG_PATH)) {
      Files.copy(CONFIG_PATH, BACKUP_PATH, StandardCopyOption.REPLACE_EXISTING);
    }
    Files.deleteIfExists(CONFIG_PATH);
    System.clearProperty("idTag");
    System.clearProperty("centralSystemUrl");
    System.clearProperty("meterValueSampleInterval");
    System.clearProperty("meterValuesSampledData");
  }

  @AfterEach
  public void restoreConfig() throws IOException {
    Files.deleteIfExists(CONFIG_PATH);
    if (Files.exists(BACKUP_PATH)) {
      Files.copy(BACKUP_PATH, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
      Files.delete(BACKUP_PATH);
    }
    System.clearProperty("idTag");
    System.clearProperty("centralSystemUrl");
    System.clearProperty("meterValueSampleInterval");
    System.clearProperty("meterValuesSampledData");
  }

  @Test
  public void testLoadConfigurationWithNewParameters() throws Exception {
    System.setProperty("idTag", "newTag");
    System.setProperty("centralSystemUrl", "ws://new.example.com");
    System.setProperty("meterValueSampleInterval", "40");
    System.setProperty("meterValuesSampledData", "POWER_ACTIVE_IMPORT");

    ConfigurationRegistry registry = ConfigurationRegistry.loadConfiguration();
    assertEquals("newTag", registry.getIdTag());
    assertEquals("ws://new.example.com", registry.getCentralSystemUrl());
    assertEquals(40, registry.getMeterValueSampleInterval());
    assertEquals(MeterValuesSampledData.POWER_ACTIVE_IMPORT, registry.getMeterValuesSampledData());

    File configFile = CONFIG_PATH.toFile();
    assertTrue(configFile.exists(), "config file is created");
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(configFile)) {
      props.load(fis);
    }
    assertEquals("newTag", props.getProperty("idTag"));
    assertEquals("ws://new.example.com", props.getProperty("centralSystemUrl"));
    assertEquals("40", props.getProperty("meterValueSampleInterval"));
    assertEquals("POWER_ACTIVE_IMPORT", props.getProperty("meterValuesSampledData"));
  }

  @Test
  public void testLoadConfigurationFromExistingConfigFile() throws Exception {
    Properties props = new Properties();
    props.setProperty("idTag", "fileTag");
    props.setProperty("centralSystemUrl", "ws://file.example.com");
    props.setProperty("meterValueSampleInterval", "50");
    props.setProperty("meterValuesSampledData", "POWER_ACTIVE_IMPORT");
    try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH.toFile())) {
      props.store(fos, "Test configuration");
    }

    ConfigurationRegistry registry = ConfigurationRegistry.loadConfiguration();

    assertEquals("fileTag", registry.getIdTag());
    assertEquals("ws://file.example.com", registry.getCentralSystemUrl());
    assertEquals(50, registry.getMeterValueSampleInterval());
    assertEquals(MeterValuesSampledData.POWER_ACTIVE_IMPORT, registry.getMeterValuesSampledData());
  }

  @Test
  public void testLoadConfigurationWithDefaults() {
    ConfigurationRegistry registry = ConfigurationRegistry.loadConfiguration();

    assertEquals("test", registry.getIdTag());
    assertEquals("ws://host.docker.internal:9000", registry.getCentralSystemUrl());
    assertEquals(30, registry.getMeterValueSampleInterval());
    assertEquals(
        MeterValuesSampledData.ENERGY_ACTIVE_IMPORT_REGISTER, registry.getMeterValuesSampledData());
  }
}
