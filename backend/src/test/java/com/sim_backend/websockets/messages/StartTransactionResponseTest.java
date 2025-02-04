package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class StartTransactionResponseTest {
  private static @NotNull StartTransactionResponse getStartTransactionResponse() {
    // Create an StartTransaction response
    StartTransactionResponse response = new StartTransactionResponse(1, "Accepted");

    // Verify the StartTransaction Request is created correctly
    assert response.getTransactionId() == 1;
    assert response.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED;
    return response;
  }

  @Test
  public void testStartTransactionResponse() {
    StartTransactionResponse response = getStartTransactionResponse();

    // Ensure message generation works
    assert response.generateMessage().size() == 3;
    String message = GsonUtilities.toString(response.generateMessage().get(2));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/StartTransactionResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    assert message.equals("{\"transactionId\":1,\"idTagInfo\":{\"status\":\"Accepted\"}}");
    assert errors.isEmpty();
  }
}
