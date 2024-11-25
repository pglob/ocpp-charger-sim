package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.BootNotificationStatus;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class BootNotificationResponseTest {

  private static @NotNull BootNotificationResponse getBootNotificationResponse(
      ZonedDateTime dateTime) {
    BootNotificationResponse response = new BootNotificationResponse("Accepted", dateTime, 5);

    assert response.getStatus().getValue().equals("Accepted");
    assert response.getStatus() == BootNotificationStatus.ACCEPTED;
    assert response.getCurrentTime() == dateTime;
    assert response.getInterval() == 5;
    return response;
  }

  @Test
  public void testBootNotificationResponse() {
    ZonedDateTime testDateTime = ZonedDateTime.of(2024, 11, 20, 20, 0, 0, 0, ZoneId.of("UTC"));

    BootNotificationResponse response = getBootNotificationResponse(testDateTime);

    // Ensure message generation works
    assert response.generateMessage().size() == 3;
    String message = GsonUtilities.toString(response.generateMessage().get(2));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/BootNotificationResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    System.out.println(message);
    assert message.equals(
        "{\"status\":\"Accepted\",\"currentTime\":\"2024-11-20T20:00:00Z\",\"interval\":5}");
    assert errors.isEmpty();
  }
}
