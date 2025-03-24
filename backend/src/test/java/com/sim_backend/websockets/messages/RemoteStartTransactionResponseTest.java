package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class RemoteStartTransactionResponseTest {

  private static @NotNull RemoteStartTransactionResponse getRemoteStartTransactionResponse() {
    return new RemoteStartTransactionResponse(
        new RemoteStartTransaction(null, null, null), "Accepted");
  }

  @Test
  public void testRemoteStartTransactionResponse() {
    RemoteStartTransactionResponse response = getRemoteStartTransactionResponse();

    // Ensure message generation works
    assert response.generateMessage().size() == 3;
    String message = GsonUtilities.toString(response.generateMessage().get(2));

    // Validate against schema
    JsonSchema jsonSchema =
        JsonSchemaHelper.getJsonSchema("schemas/RemoteStartTransactionResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    String expectedMessage = "{\"status\":\"Accepted\"}";
    System.out.println("Generated Message: " + message);

    assert message.equals(expectedMessage);
    assert errors.isEmpty();
  }
}
