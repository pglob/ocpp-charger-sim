package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class RemoteStopTransactionResponseTest {

  private static @NotNull RemoteStopTransactionResponse getRemoteStopTransactionResponse() {
    return new RemoteStopTransactionResponse(new RemoteStopTransaction(11), "Accepted");
  }

  @Test
  public void testRemoteStopTransactionResponse() {
    RemoteStopTransactionResponse response = getRemoteStopTransactionResponse();

    // Ensure message generation works
    assert response.generateMessage().size() == 3;
    String message = GsonUtilities.toString(response.generateMessage().get(2));

    // Validate against schema
    JsonSchema jsonSchema =
        JsonSchemaHelper.getJsonSchema("schemas/RemoteStopTransactionResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

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
