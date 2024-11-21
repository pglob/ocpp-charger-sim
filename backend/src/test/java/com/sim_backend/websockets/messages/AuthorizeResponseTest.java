package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.AuthorizationStatus;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class AuthorizeResponseTest {
  private static @NotNull AuthorizeResponse getAuthorizeResponse() {
    // Create an Authorize response with an Accepted status
    AuthorizeResponse response = new AuthorizeResponse("Accepted");

    // Verify the authorization request is created correctly
    assert response.getIdTagInfo().getStatus().getValue().equals("Accepted");
    assert response.getIdTagInfo().getStatus() == AuthorizationStatus.ACCEPTED;
    return response;
  }

  @Test
  public void testAuthorizeRequest() {
    AuthorizeResponse response = getAuthorizeResponse();

    // Ensure message generation works
    assert response.generateMessage().size() == 3;
    String message = GsonUtilities.toString(response.generateMessage().get(2));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/AuthorizeResponse.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    // Uncomment to print validation errors if needed
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    assert message.equals("{\"idTagInfo\":{\"status\":\"Accepted\"}}");
    assert errors.isEmpty();
  }
}
