package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class AuthorizeTest {

  private static @NotNull Authorize getAuthorize() {
    // Create an Authorize request with a specific idTag
    Authorize authorize = new Authorize("12345ABCDE");

    // Verify the authorization request is created correctly
    assert authorize.getIdTag().equals("12345ABCDE");
    return authorize;
  }

  @Test
  public void testAuthorizeRequest() {
    Authorize authorize = getAuthorize();

    // Ensure message generation works
    assert authorize.generateMessage().size() == 4;
    String message = GsonUtilities.toString(authorize.generateMessage().get(3));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/Authorize.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    // Uncomment to print validation errors if needed
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    assert message.equals("{\"idTag\":\"12345ABCDE\"}");
    assert errors.isEmpty();
  }

  @Test
  public void testAuthorizeWithGeneratedIdTag() {
    // Test the no-argument constructor that generates an idTag
    Authorize authorize = new Authorize();

    // Verify that an idTag was generated
    assert authorize.getIdTag() != null;
    assert authorize.getIdTag().length() == 20;
  }
}
