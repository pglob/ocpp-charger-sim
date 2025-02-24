package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.*;

import com.sim_backend.websockets.enums.ChargingProfilePurpose;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearChargingProfileTest {

  private Validator validator;

  @BeforeEach
  public void setUp() {
    // Set up the Validator before each test
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void testClearChargingProfileConstructor() {

    // Create the ClearChargingProfile object
    Integer id = 1;
    Integer connectorId = 2;
    ChargingProfilePurpose purpose = ChargingProfilePurpose.TX_PROFILE;
    Integer stackLevel = 3;

    ClearChargingProfile profile = new ClearChargingProfile(id, connectorId, purpose, stackLevel);

    // Assert expected values
    assertSame(id, profile.getId());
    assertSame(connectorId, profile.getConnectorId());
    assertSame(purpose, profile.getChargingProfilePurpose());
    assertSame(stackLevel, profile.getStackLevel());

    // Validate the ClearChargingProfile object
    var violations = validator.validate(profile);

    // Assert that there are no validation errors
    assertTrue(violations.isEmpty(), "Expected no validation violations");
  }

  @Test
  public void testClearChargingProfileWithViolations() {
    Integer id = 1;
    Integer connectorId = -2; // Violation here
    ChargingProfilePurpose purpose = ChargingProfilePurpose.TX_PROFILE;
    Integer stackLevel = 3;

    ClearChargingProfile profile = new ClearChargingProfile(id, connectorId, purpose, stackLevel);

    assertSame(id, profile.getId());
    assertSame(connectorId, profile.getConnectorId());
    assertSame(purpose, profile.getChargingProfilePurpose());
    assertSame(stackLevel, profile.getStackLevel());

    // Validate the ClearChargingProfile object
    var violations = validator.validate(profile);

    // Assert that there are validation errors
    assertFalse(violations.isEmpty(), "Expected a validation violation");
  }
}
