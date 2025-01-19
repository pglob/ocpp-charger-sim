package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class StopTransactionTest {

  private static @NotNull StopTransaction getStopTransaction() {
    // Create an StopTransaction request
    StopTransaction stopTransaction = new StopTransaction(1, 10, "2025-01-01T00:00:00Z");

    assert stopTransaction.getTransactionId() == 1;
    assert stopTransaction.getMeterStop() == 10;
    assert stopTransaction.getTimestamp().equals("2025-01-01T00:00:00Z");
    return stopTransaction;
  }

  @Test
  public void testStopTransaction() {
    StopTransaction request = getStopTransaction();

    // Ensure message generation works
    assert request.generateMessage().size() == 4;
    String message = GsonUtilities.toString(request.generateMessage().get(3));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/StopTransaction.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }
    // Check expected message structure
    assert message.equals(
        "{\"transactionId\":1,\"meterStop\":10,\"timestamp\":\"2025-01-01T00:00:00Z\"}");
    assert errors.isEmpty();
  }
}
