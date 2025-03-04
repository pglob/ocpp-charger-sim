package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.AvailabilityStatus;
import com.sim_backend.websockets.enums.AvailabilityType;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class ChangeAvailabilityResponseTest {
  private static @NotNull ChangeAvailabilityResponse getChangeAvailabilityResponse() {
    ChangeAvailabilityResponse availability =
        new ChangeAvailabilityResponse(
            new ChangeAvailability(0, AvailabilityType.OPERATIVE), AvailabilityStatus.ACCEPTED);
    assertEquals(AvailabilityStatus.ACCEPTED, availability.getStatus());

    return availability;
  }

  @Test
  public void testChangeAvailability() {
    ChangeAvailabilityResponse availability = getChangeAvailabilityResponse();
    JsonArray jsonMessage = availability.generateMessage();
    assert jsonMessage.size() == 3;
    String message = GsonUtilities.toString(availability.generateMessage().get(2));

    JsonSchema jsonSchema =
        JsonSchemaHelper.getJsonSchema("schemas/ChangeAvailabilityResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    assertEquals("{\"status\":\"Accepted\"}", message);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void testClone() {
    ChangeAvailabilityResponse availability = getChangeAvailabilityResponse();
    ChangeAvailabilityResponse clone = availability.clone();
    assertEquals(availability, clone);
  }
}
