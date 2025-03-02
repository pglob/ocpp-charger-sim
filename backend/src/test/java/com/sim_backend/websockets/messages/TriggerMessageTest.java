package com.sim_backend.websockets.messages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.MessageTrigger;
import com.sim_backend.websockets.messages.TriggerMessage;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class TriggerMessageTest {

  private static @NotNull TriggerMessage getTriggerMessageWithoutConnector() {
    TriggerMessage message = new TriggerMessage(MessageTrigger.Heartbeat);
    assert message.getRequestedMessage() == MessageTrigger.Heartbeat;
    return message;
  }

  private static @NotNull TriggerMessage getTriggerMessageWithConnector() {
    TriggerMessage message = new TriggerMessage(MessageTrigger.StatusNotification, 1);
    assert message.getRequestedMessage() == MessageTrigger.StatusNotification;
    assert message.getConnectorId() == 1;
    return message;
  }

  @Test
  public void testTriggerMessageWithoutConnector() {
    TriggerMessage msg = getTriggerMessageWithoutConnector();
    JsonArray messageStructure = msg.generateMessage();
    assert messageStructure.size() == 4;
    JsonElement payload = messageStructure.get(3);
    String payloadJson = GsonUtilities.toString(payload);
    JsonSchema schema = JsonSchemaHelper.getJsonSchema("schemas/TriggerMessage.json");
    Set<ValidationMessage> errors = schema.validate(payloadJson, InputFormat.JSON);
    if (!errors.isEmpty()) {
      errors.forEach(System.out::println);
    }
    assert errors.isEmpty();
    assert payloadJson.contains("\"requestedMessage\":\"Heartbeat\"");
  }

  @Test
  public void testTriggerMessageWithConnector() {
    TriggerMessage msg = getTriggerMessageWithConnector();
    JsonArray messageStructure = msg.generateMessage();
    assert messageStructure.size() == 4;

    JsonElement payload = messageStructure.get(3);
    String payloadJson = GsonUtilities.toString(payload);

    JsonSchema schema = JsonSchemaHelper.getJsonSchema("schemas/TriggerMessage.json");
    Set<ValidationMessage> errors = schema.validate(payloadJson, InputFormat.JSON);
    if (!errors.isEmpty()) {
      errors.forEach(System.out::println);
    }
    assert errors.isEmpty();

    assert payloadJson.contains("\"requestedMessage\":\"StatusNotification\"");
    assert payloadJson.contains("\"connectorId\":1");
  }
}
