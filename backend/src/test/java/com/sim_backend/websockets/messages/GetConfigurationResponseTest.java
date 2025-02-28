package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class GetConfigurationResponseTest {
  private static @NotNull GetConfigurationResponse getGetConfigurationResponse() {
    // Create an GetConfigurationResponse with valid and invalid key
    GetConfigurationResponse response =
        new GetConfigurationResponse(
            new GetConfiguration(List.of("MeterValueSampleInterval")),
            Arrays.asList(
                new GetConfigurationResponse.Configuration("MeterValueSampleInterval", "22", true),
                new GetConfigurationResponse.Configuration(
                    "MeterValuesSampledData", "ENERGY_ACTIVE_IMPORT_REGISTER", true)),
            Arrays.asList("InvalidKey", "InvalidValue"));

    return response;
  }

  @Test
  public void testGetConfigurationResponse() {
    GetConfigurationResponse response = getGetConfigurationResponse();

    // Ensure message generation works
    assert response.generateMessage().size() == 3;
    String message = GsonUtilities.toString(response.generateMessage().get(2));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/GetConfigurationResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    assert message.equals(
        "{\"configurationKey\":[{\"key\":\"MeterValueSampleInterval\",\"value\":\"22\",\"readonly\":true},{\"key\":\"MeterValuesSampledData\",\"value\":\"ENERGY_ACTIVE_IMPORT_REGISTER\",\"readonly\":true}],\"unknownKey\":[\"InvalidKey\",\"InvalidValue\"]}");
    assert errors.isEmpty();
  }
}
