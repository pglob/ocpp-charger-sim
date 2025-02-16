package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class ChangeConfigurationTest {

  private static @NotNull ChangeConfiguration getChangeConfiguration() {
    ChangeConfiguration configuration = new ChangeConfiguration("idTag", "ChangedidTag");
    return configuration;
  }

  @Test
  public void testChangeConfigurationRequest() {
    ChangeConfiguration configuration = getChangeConfiguration();

    // Ensure message generation works
    assert configuration.generateMessage().size() == 4;
    String message = GsonUtilities.toString(configuration.generateMessage().get(3));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/ChangeConfiguration.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    // Uncomment to print validation errors if needed
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    assert message.equals("{\"key\":\"idTag\",\"value\":\"ChangedidTag\"}");
    assert errors.isEmpty();
  }
}
