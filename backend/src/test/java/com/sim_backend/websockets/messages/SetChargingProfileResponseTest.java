package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.*;

import com.sim_backend.websockets.enums.ChargingProfileStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test class for SetChargingProfileResponse */
public class SetChargingProfileResponseTest {

  private Validator validator;

  @BeforeEach
  public void setUp() {
    // Set up the Validator before each test
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void testSetChargingProfileResponse_ValidStatus() {
    // Create a valid SetChargingProfileResponse with a non-blank status
    SetChargingProfileResponse response =
        new SetChargingProfileResponse(ChargingProfileStatus.ACCEPTED);

    // Validate the response object
    var violations = validator.validate(response);

    // Assert that there are no validation errors
    assertTrue(violations.isEmpty(), "Expected no validation violations");
  }

  @Test
  public void testSetChargingProfileResponse_BlankStatus() {
    // Create a SetChargingProfileResponse with a null or blank status (invalid)
    SetChargingProfileResponse response = new SetChargingProfileResponse(null);

    // Validate the response object
    var violations = validator.validate(response);

    // Assert that there is a violation (since status should not be blank)
    assertFalse(violations.isEmpty(), "Expected validation violations due to blank status");

    // Check if the expected violation message is present
    assertEquals(
        "SetChargingProfileResponse status is required and cannot be blank",
        violations.iterator().next().getMessage());
  }

  @Test
  public void testSetChargingProfileResponse_ValidChargingProfileStatus() {
    // Create a valid SetChargingProfileResponse with a specific status
    SetChargingProfileResponse response =
        new SetChargingProfileResponse(ChargingProfileStatus.REJECTED);

    // Assert that the status is correctly set
    assertEquals(
        ChargingProfileStatus.REJECTED, response.getStatus(), "The status should be REJECTED");
  }
}
