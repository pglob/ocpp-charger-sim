package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import java.util.Arrays;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class GetConfigurationTest {

  private static @NotNull GetConfiguration getGetConfiguration() {
    GetConfiguration configuration =
        new GetConfiguration(Arrays.asList("MeterValueSampleInterval", "MeterValuesSampledData"));
    return configuration;
  }

  @Test
  public void testGetConfigurationRequest() {
    GetConfiguration configuration = getGetConfiguration();

    // Ensure message generation works
    assert configuration.generateMessage().size() == 4;
    String message = GsonUtilities.toString(configuration.generateMessage().get(3));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/GetConfiguration.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    assert message.equals("{\"key\":[\"MeterValueSampleInterval\",\"MeterValuesSampledData\"]}");
    assert errors.isEmpty();
  }
}
