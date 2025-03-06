package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.*;

import com.sim_backend.websockets.enums.ChargingProfileKind;
import com.sim_backend.websockets.enums.ChargingProfilePurpose;
import com.sim_backend.websockets.enums.ChargingRateUnit;
import com.sim_backend.websockets.enums.RecurrencyKind;
import com.sim_backend.websockets.types.ChargingProfile;
import com.sim_backend.websockets.types.ChargingSchedule;
import com.sim_backend.websockets.types.ChargingSchedulePeriod;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.ZonedDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SetChargingProfileTest {
  private Validator validator;

  @BeforeEach
  public void setUp() {
    // Set up the Validator before each test
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void testSetChargingProfile_ValidData() {
    // Create a valid ChargingSchedulePeriod
    ChargingSchedulePeriod period = new ChargingSchedulePeriod(0, 5000, 3);

    // Create a valid ChargingSchedule

    ChargingSchedule schedule =
        new ChargingSchedule(
            3600, // duration in seconds
            ZonedDateTime.now(),
            ChargingRateUnit.WATTS, // Charging rate in watts
            Arrays.asList(period), // charging schedule period
            5000.0 // min charging rate
            );

    // Create a valid ChargingProfile

    ChargingProfile profile =
        new ChargingProfile(
            1, // chargingProfileId
            1234, // transactionId
            0, // stackLevel
            ChargingProfilePurpose.TX_PROFILE, // chargingProfilePurpose
            ChargingProfileKind.ABSOLUTE, // chargingProfileKind
            RecurrencyKind.DAILY, // recurrencyKind
            ZonedDateTime.now(), // validFrom
            ZonedDateTime.now().plusDays(1), // validTo
            schedule // chargingSchedule
            );
    // Create the SetChargingProfile request
    SetChargingProfile setChargingProfile =
        new SetChargingProfile(
            1, // connectorId
            profile // csChargingProfiles
            );
    // Validate the SetChargingProfile object
    var violations = validator.validate(setChargingProfile);
    // Assert that there are no validation errors
    assertTrue(violations.isEmpty(), "Expected no validation violations");
  }

  @Test
  public void testSetChargingProfile_MissingChargingProfile() {
    // Create the SetChargingProfile request with a missing charging profile (null)
    SetChargingProfile setChargingProfile =
        new SetChargingProfile(
            1, // connectorId
            null // csChargingProfiles is null (invalid)
            );
    // Validate the SetChargingProfile object
    var violations = validator.validate(setChargingProfile);
    // Assert that the validation finds the missing charging profile
    assertFalse(
        violations.isEmpty(), "Expected validation violations due to missing csChargingProfiles");
    assertEquals(
        "SetChargingProfile csChargingProfiles is required",
        violations.iterator().next().getMessage());
  }
}
