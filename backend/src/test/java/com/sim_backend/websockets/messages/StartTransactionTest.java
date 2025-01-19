package com.sim_backend.websockets.messages;

import static org.mockito.Mockito.*;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.OCPPTime;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StartTransactionTest {

  OCPPWebSocketClient client;
  String time;

  @BeforeEach
  void setUp() throws URISyntaxException {
    client = spy(new OCPPWebSocketClient(new URI("")));
    OCPPTime ocppTime = client.getScheduler().getTime();
    ZonedDateTime zonetime = ocppTime.getSynchronizedTime();
    time = zonetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"));
  }

  @Test
  public void testStartTransaction() {
    StartTransaction request = new StartTransaction(1, AuthorizationStatus.ACCEPTED, 0, time);
    String tempTime = time;

    // Ensure message generation works
    assert request.generateMessage().size() == 4;
    String message = GsonUtilities.toString(request.generateMessage().get(3));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/StartTransaction.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    assert message.equals(
        "{\"connectorId\":1,\"idTag\":\"Accepted\",\"meterStart\":0,\"timestamp\":\""
            + tempTime
            + "\"}");

    assert errors.isEmpty();
  }
}
