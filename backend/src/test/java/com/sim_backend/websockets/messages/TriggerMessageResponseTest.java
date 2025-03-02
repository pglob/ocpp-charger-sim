package com.sim_backend.websockets.messages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.TriggerMessageStatus;
import com.sim_backend.websockets.enums.MessageTrigger;
import com.sim_backend.websockets.messages.TriggerMessage;
import com.sim_backend.websockets.messages.TriggerMessageResponse;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class TriggerMessageResponseTest {

  private static @NotNull TriggerMessageResponse getTriggerMessageResponseAccepted() {
    TriggerMessage dummyRequest = new TriggerMessage(MessageTrigger.Heartbeat);
    return new TriggerMessageResponse(dummyRequest, TriggerMessageStatus.Accepted);
  }

  private static @NotNull TriggerMessageResponse getTriggerMessageResponseRejected() {
    TriggerMessage dummyRequest = new TriggerMessage(MessageTrigger.Heartbeat);
    return new TriggerMessageResponse(dummyRequest, TriggerMessageStatus.Rejected);
  }

  private static @NotNull TriggerMessageResponse getTriggerMessageResponseNotImplemented() {
    TriggerMessage dummyRequest = new TriggerMessage(MessageTrigger.Heartbeat);
    return new TriggerMessageResponse(dummyRequest, TriggerMessageStatus.NotImplemented);
  }

  @Test
  public void testTriggerMessageResponseAccepted() {
    TriggerMessageResponse response = getTriggerMessageResponseAccepted();
    JsonArray messageStructure = response.generateMessage();
    assert messageStructure.size() == 3;
    JsonElement payload = messageStructure.get(2);
    String payloadJson = GsonUtilities.toString(payload);
    JsonSchema schema = JsonSchemaHelper.getJsonSchema("schemas/TriggerMessageResponse.json");
    Set<ValidationMessage> errors = schema.validate(payloadJson, InputFormat.JSON);
    if (!errors.isEmpty()) {
      errors.forEach(System.out::println);
    }
    assert errors.isEmpty();
    assert payloadJson.contains("\"status\":\"Accepted\"");
  }

  @Test
  public void testTriggerMessageResponseRejected() {
    TriggerMessageResponse response = getTriggerMessageResponseRejected();
    JsonArray messageStructure = response.generateMessage();
    assert messageStructure.size() == 3;
    JsonElement payload = messageStructure.get(2);
    String payloadJson = GsonUtilities.toString(payload);
    JsonSchema schema = JsonSchemaHelper.getJsonSchema("schemas/TriggerMessageResponse.json");
    Set<ValidationMessage> errors = schema.validate(payloadJson, InputFormat.JSON);
    errors.forEach(System.out::println);
    assert errors.isEmpty();
    assert payloadJson.contains("\"status\":\"Rejected\"");
  }

  @Test
  public void testTriggerMessageResponseNotImplemented() {
    TriggerMessageResponse response = getTriggerMessageResponseNotImplemented();
    JsonArray messageStructure = response.generateMessage();
    assert messageStructure.size() == 3;
    JsonElement payload = messageStructure.get(2);
    String payloadJson = GsonUtilities.toString(payload);
    JsonSchema schema = JsonSchemaHelper.getJsonSchema("schemas/TriggerMessageResponse.json");
    Set<ValidationMessage> errors = schema.validate(payloadJson, InputFormat.JSON);
    errors.forEach(System.out::println);
    assert errors.isEmpty();
    assert payloadJson.contains("\"status\":\"NotImplemented\"");
  }
}
