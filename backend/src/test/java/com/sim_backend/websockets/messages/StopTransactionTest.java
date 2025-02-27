package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.Reason;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class StopTransactionTest {

  private static @NotNull StopTransaction getStopTransaction() {
    // Create a StopTransaction request
    StopTransaction stopTransaction = new StopTransaction("tag", 1, 10, "2025-01-01T00:00:00Z");

    assert stopTransaction.getIdTag() == "tag";
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
        "{\"idTag\":\"tag\",\"transactionId\":1,\"meterStop\":10,\"timestamp\":\"2025-01-01T00:00:00Z\"}");
    assert errors.isEmpty();
  }

  private @NotNull StopTransaction getStopTransactionWithReasonNoTag() {
    // Create a StopTransaction request with the reason field populated
    StopTransaction stopTransaction =
        new StopTransaction(1, 10, "2025-01-01T00:00:00Z", Reason.EMERGENCY_STOP);

    assert stopTransaction.getTransactionId() == 1;
    assert stopTransaction.getMeterStop() == 10;
    assert stopTransaction.getTimestamp().equals("2025-01-01T00:00:00Z");
    assert stopTransaction.getReason().equals(Reason.EMERGENCY_STOP);
    return stopTransaction;
  }

  @Test
  public void testStopTransactionWithReasonNoTag() {
    StopTransaction request = getStopTransactionWithReasonNoTag();

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
        "{\"transactionId\":1,\"meterStop\":10,\"timestamp\":\"2025-01-01T00:00:00Z\",\"reason\":\"EmergencyStop\"}");
    assert errors.isEmpty();
  }
}
