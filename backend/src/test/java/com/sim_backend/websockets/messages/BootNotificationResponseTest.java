package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class BootNotificationResponseTest {

  private static @NotNull BootNotificationResponse getBootNotificationResponse() {
    BootNotificationResponse response =
        new BootNotificationResponse("Accepted", "2024-02-20T12:34:56Z", 5);

    assert response.getStatus().equals("Accepted");
    assert response.getInterval() == 5;
    return response;
  }

  @Test
  public void testBootNotificationResponse() {
    BootNotificationResponse response = getBootNotificationResponse();

    // Ensure message generation works
    assert response.generateMessage().size() == 4;
    String message = GsonUtilities.toString(response.generateMessage().get(3));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/BootNotificationResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    assert message.equals(
        "{\"status\":\"Accepted\",\"currentTime\":\"2024-02-20T12:34:56Z\",\"interval\":5}");
    assert errors.isEmpty();
  }
}
