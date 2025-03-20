package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class RemoteStopTransactionTest {

  private static @NotNull RemoteStopTransaction getRemoteStopTransaction() {
    return new RemoteStopTransaction(11);
  }

  @Test
  public void testRemoteStopTransaction() {
    RemoteStopTransaction request = getRemoteStopTransaction();

    // Ensure message generation works
    assert request.generateMessage().size() == 4;
    String message = GsonUtilities.toString(request.generateMessage().get(3));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/RemoteStopTransaction.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    assert message.equals("{\"transactionId\":11}");
    assert errors.isEmpty();
  }
}
