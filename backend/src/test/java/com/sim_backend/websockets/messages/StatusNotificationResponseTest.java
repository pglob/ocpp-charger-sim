package com.sim_backend.websockets.messages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class StatusNotificationResponseTest {

  private StatusNotification getDefaultStatusNotificationRequest() {
    return new StatusNotification(
        1,
        ChargePointErrorCode.NoError,
        "Some info",
        ChargePointStatus.Available,
        ZonedDateTime.now(),
        "Vendor123",
        "VendorError456"
    );
  }

  @Test
  public void testStatusNotificationResponseGenerateMessage() {
    StatusNotification request = getDefaultStatusNotificationRequest();
    StatusNotificationResponse response = new StatusNotificationResponse(request);
    JsonArray messageStructure = response.generateMessage();
    assertEquals(4, messageStructure.size(), "OCPP response array should have 4 elements");
    JsonElement payload = messageStructure.get(3);
    String payloadJson = GsonUtilities.toString(payload);
    JsonSchema schema = JsonSchemaHelper.getJsonSchema("schemas/StatusNotificationResponse.json");
    Set<ValidationMessage> errors = schema.validate(payloadJson, InputFormat.JSON);

    if (!errors.isEmpty()) {
      errors.forEach(System.out::println);
    }
    assertTrue(errors.isEmpty(), "Payload should be valid according to the JSON schema");
    assertEquals("{}", payloadJson, "StatusNotificationResponse payload should be an empty JSON object");
  }
}