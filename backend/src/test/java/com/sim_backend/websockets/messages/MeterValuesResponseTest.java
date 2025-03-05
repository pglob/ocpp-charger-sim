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
import java.util.Set;
import org.junit.jupiter.api.Test;

public class MeterValuesResponseTest {

  /** Helper method to create a MeterValues request for testing. */
  private MeterValues getMeterValuesRequest() {
    ZonedDateTime timestamp = ZonedDateTime.parse("2025-03-04T21:53:04Z");
    MeterValues.SampledValue sampledValue =
        new MeterValues.SampledValue(
            "12.34",
            ReadingContext.SAMPLE_PERIODIC,
            MeterValuesSampledData.CURRENT_OFFERED,
            UnitOfMeasure.A);
    MeterValues.MeterValue meterValue =
        new MeterValues.MeterValue(timestamp, java.util.Collections.singletonList(sampledValue));
    return new MeterValues(1, 123, java.util.Collections.singletonList(meterValue));
  }

  @Test
  public void testMeterValuesResponseMessage() {
    // Create a MeterValues request to base the response on
    MeterValues request = getMeterValuesRequest();

    // Create the MeterValuesResponse from the request
    MeterValuesResponse response = new MeterValuesResponse(request);

    assertEquals(3, response.generateMessage().size());

    String message = GsonUtilities.toString(response.generateMessage().get(2));

    // Validate against the MeterValuesResponse JSON schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/MeterValuesResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      errors.forEach(System.out::println);
    }

    String expectedMessage = "{}";

    // Verify that the generated payload matches the expected JSON structure
    assertEquals(expectedMessage, message);
    assertTrue(errors.isEmpty());
  }
}
