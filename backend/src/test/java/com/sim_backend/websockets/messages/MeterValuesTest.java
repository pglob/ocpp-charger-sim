package com.sim_backend.websockets.messages;

import static org.junit.jupiter.api.Assertions.*;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.MeterValuesSampledData;
import com.sim_backend.websockets.enums.ReadingContext;
import com.sim_backend.websockets.enums.UnitOfMeasure;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class MeterValuesTest {

  private static @NotNull MeterValues getMeterValues() {
    ZonedDateTime timestamp = ZonedDateTime.parse("2025-03-04T21:53:04Z");

    MeterValues.SampledValue sampledValue =
        new MeterValues.SampledValue(
            "12.34",
            ReadingContext.SAMPLE_PERIODIC,
            MeterValuesSampledData.CURRENT_OFFERED,
            UnitOfMeasure.A);

    MeterValues.MeterValue meterValue =
        new MeterValues.MeterValue(timestamp, Collections.singletonList(sampledValue));

    return new MeterValues(1, 123, Collections.singletonList(meterValue));
  }

  @Test
  public void testMeterValuesRequest() {
    MeterValues meterValues = getMeterValues();

    // Ensure message generation returns a list with 4 elements
    assertEquals(4, meterValues.generateMessage().size());

    String message = GsonUtilities.toString(meterValues.generateMessage().get(3));

    // Validate the JSON against the MeterValues schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/MeterValues.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      errors.forEach(System.out::println);
    }

    String expectedMessage =
        "{\"connectorId\":1,\"transactionId\":123,\"meterValue\":[{\"timestamp\":\"2025-03-04T21:53:04Z\",\"sampledValue\":[{\"value\":\"12.34\",\"context\":\"Sample.Periodic\",\"measurand\":\"Current.Offered\",\"unit\":\"A\"}]}]}";

    // Verify that the generated message matches the expected JSON
    assertEquals(expectedMessage, message);
    assertTrue(errors.isEmpty());
  }
}
