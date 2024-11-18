package com.sim_backend.websockets.messages;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sim_backend.websockets.enums.ErrorCode;
import com.sim_backend.websockets.types.OcppMessageError;
import org.junit.jupiter.api.Test;

public class OcppMessageErrorTest {

  @Test
  public void testMessageError() {
    OcppMessageError error =
        new OcppMessageError(ErrorCode.InternalError, "Not Found", new JsonObject());
    JsonArray array = error.generateMessage();
    assert array.get(2).getAsString().equals(error.getErrorCode().toString());
    assert array.get(3).getAsString().equals("Not Found");
    assert array.get(4).getAsJsonObject() != null;

    assert error.getErrorCode() == ErrorCode.InternalError;
    assert error.getErrorDescription().equals("Not Found");
    assert error.getErrorCode() != null;
  }
}
