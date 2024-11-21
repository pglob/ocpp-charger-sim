package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class HeartBeatResponseTest {

  @Test
  public void testHeartBeatResponse() {
    HeartBeatResponse response = new HeartBeatResponse();

    // Ensure message generation works
    assert response.generateMessage().size() == 3;
    String message = GsonUtilities.toString(response.generateMessage().get(2));

    // Match JSON message to regex
    String regex = "\\{\"currentTime\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z\"\\}";

    Pattern pattern = Pattern.compile(regex);

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/HeartbeatResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message regex structure
    assertTrue(message.matches(regex));
    assert errors.isEmpty();
  }
}
