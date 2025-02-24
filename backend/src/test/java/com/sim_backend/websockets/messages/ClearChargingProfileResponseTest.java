package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.*;

import com.sim_backend.websockets.enums.ClearProfileStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearChargingProfileResponseTest {

  private Validator validator;

  @BeforeEach
  public void setUp() {
    // Set up the Validator before each test
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void testClearChargingProfileResponseConstructor() {

    // Create the ClearChargingProfileResponse object
    ClearChargingProfileResponse profile =
        new ClearChargingProfileResponse(ClearProfileStatus.ACCEPTED);

    // Assert expected values
    assertSame(ClearProfileStatus.ACCEPTED, profile.getStatus());

    // Validate the ClearChargingProfile object
    var violations = validator.validate(profile);

    // Assert that there are no validation errors
    assertTrue(violations.isEmpty(), "Expected no validation violations");
  }

  @Test
  public void testClearChargingProfileResponseWithViolations() {
    // Create the ClearChargingProfileResponse object
    ClearChargingProfileResponse profile = new ClearChargingProfileResponse(null); // Violation here

    // Assert expected values
    assertSame(null, profile.getStatus());

    // Validate the ClearChargingProfile object
    var violations = validator.validate(profile);

    // Assert that there are no validation errors
    assertFalse(violations.isEmpty(), "Expected a validation violation");
  }
}
