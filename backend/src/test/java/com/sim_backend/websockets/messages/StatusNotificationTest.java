package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.ChargePointErrorCode;
import com.sim_backend.websockets.enums.ChargePointStatus;
import java.time.ZonedDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class StatusNotificationTest {

  private StatusNotification getDefaultStatusNotification() {
    return new StatusNotification(
        1,
        ChargePointErrorCode.NoError,
        "Some info",
        ChargePointStatus.Available,
        ZonedDateTime.now(),
        "Vendor123",
        "VendorError456");
  }

  @Test
  public void testStatusNotificationGenerateMessage() {
    StatusNotification msg = getDefaultStatusNotification();

    JsonArray messageStructure = msg.generateMessage();
    assertEquals(4, messageStructure.size(), "OCPP message array should have 4 elements");
    JsonElement payload = messageStructure.get(3);
    String payloadJson = GsonUtilities.toString(payload);
    JsonSchema schema = JsonSchemaHelper.getJsonSchema("schemas/StatusNotification.json");
    Set<ValidationMessage> errors = schema.validate(payloadJson, InputFormat.JSON);

    if (!errors.isEmpty()) {
      errors.forEach(System.out::println);
    }
    assertTrue(errors.isEmpty(), "Payload should be valid according to the JSON schema");
    assertTrue(payloadJson.contains("\"connectorId\":1"), "Should contain connectorId:1");
    assertTrue(
        payloadJson.contains("\"errorCode\":\"NoError\""), "Should contain errorCode:NoError");
    assertTrue(payloadJson.contains("\"info\":\"Some info\""), "Should contain info:'Some info'");
    assertTrue(
        payloadJson.contains("\"status\":\"Available\""), "Should contain status:'Available'");
    assertTrue(
        payloadJson.contains("\"vendorId\":\"Vendor123\""), "Should contain vendorId:'Vendor123'");
    assertTrue(
        payloadJson.contains("\"vendorErrorCode\":\"VendorError456\""),
        "Should contain vendorErrorCode:'VendorError456'");
  }
}
