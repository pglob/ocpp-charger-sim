package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.AvailabilityType;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class ChangeAvailabilityTest {
  private static @NotNull ChangeAvailability getChangeAvailability() {
    ChangeAvailability availability = new ChangeAvailability(0, AvailabilityType.OPERATIVE);
    assertEquals(AvailabilityType.OPERATIVE, availability.getType());
    assertEquals(0, availability.getConnectorID());

    return availability;
  }

  @Test
  public void testChangeAvailability() {
    ChangeAvailability availability = getChangeAvailability();
    JsonArray jsonMessage = availability.generateMessage();
    assert jsonMessage.size() == 4;
    String message = GsonUtilities.toString(availability.generateMessage().get(3));

    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/ChangeAvailability.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }
    assertEquals("{\"connectorId\":0,\"type\":\"Operative\"}", message);
    assert errors.isEmpty();
  }

  @Test
  public void testClone() {
    ChangeAvailability availability = getChangeAvailability();
    ChangeAvailability clone = availability.clone();
    assertEquals(availability, clone);
  }
}
