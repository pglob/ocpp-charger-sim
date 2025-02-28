package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.*;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.ClearProfileStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
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
  public void testClearChargingProfileResponse() {

    // Create the ClearChargingProfileResponse object
    ClearChargingProfileResponse profile =
        new ClearChargingProfileResponse(
            new ClearChargingProfile(1, 1, null, 1), ClearProfileStatus.ACCEPTED);

    // Assert expected values
    assertSame(ClearProfileStatus.ACCEPTED, profile.getStatus());

    // Validate the ClearChargingProfile object
    var violations = validator.validate(profile);

    // Assert that there are no validation errors
    assertTrue(violations.isEmpty(), "Expected no validation violations");

    // Ensure message generation works
    assert profile.generateMessage().size() == 3;
    String message = GsonUtilities.toString(profile.generateMessage().get(2));

    // Validate against schema
    JsonSchema jsonSchema =
        JsonSchemaHelper.getJsonSchema("schemas/ClearChargingProfileResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }
  }

  @Test
  public void testClearChargingProfileResponseWithViolations() {
    // Create the ClearChargingProfileResponse object
    ClearChargingProfileResponse profile =
        new ClearChargingProfileResponse(
            new ClearChargingProfile(1, 1, null, 1), null); // Violation here

    // Assert expected values
    assertSame(null, profile.getStatus());

    // Validate the ClearChargingProfile object
    var violations = validator.validate(profile);

    // Assert that there are no validation errors
    assertFalse(violations.isEmpty(), "Expected a validation violation");
  }
}
