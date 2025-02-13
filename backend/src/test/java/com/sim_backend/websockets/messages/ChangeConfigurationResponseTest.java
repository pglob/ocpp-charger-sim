package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.ConfigurationStatus;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class ChangeConfigurationResponseTest {
  private static @NotNull ChangeConfigurationResponse getChangeConfigurationResponse() {
    // Create an ChangeConfigurationResponse with an Accepted status
    ChangeConfigurationResponse response = new ChangeConfigurationResponse("Accepted");

    // Verify the ChangeConfiguration request is created correctly
    assert response.getStatus().getValue().equals("Accepted");
    assert response.getStatus() == ConfigurationStatus.ACCEPTED;
    return response;
  }

  @Test
  public void testChangeConfigurationResponse() {
    ChangeConfigurationResponse response = getChangeConfigurationResponse();

    // Ensure message generation works
    assert response.generateMessage().size() == 3;
    String message = GsonUtilities.toString(response.generateMessage().get(2));

    // Validate against schema
    JsonSchema jsonSchema =
        JsonSchemaHelper.getJsonSchema("schemas/ChangeConfigurationResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    // Uncomment to print validation errors if needed
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }
    // Check expected message structure
    assert message.equals("{\"status\":\"Accepted\"}");
    assert errors.isEmpty();
  }
}
