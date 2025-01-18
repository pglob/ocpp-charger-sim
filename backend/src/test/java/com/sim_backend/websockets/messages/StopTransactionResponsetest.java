package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class StopTransactionResponsetest {
  private static @NotNull StopTransactionResponse getStopTransactionResponse() {
    // Create an StopTransaction response
    StopTransactionResponse response = new StopTransactionResponse("Accepted");

    // Verify the StopTransaction Request is created correctly
    assert response.getIdTaginfo().getStatus() == AuthorizationStatus.ACCEPTED;
    return response;
  }

  @Test
  public void testStopTransactionResponse() {
    StopTransactionResponse response = getStopTransactionResponse();

    // Ensure message generation works
    System.out.println(response.generateMessage().size());
    assert response.generateMessage().size() == 2;
    String message = GsonUtilities.toString(response.generateMessage().get(1));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/StopTransactionResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    System.out.println(message);

    // Check expected message structure
    assert errors.isEmpty();
  }
}
