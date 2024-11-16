package com.sim_backend.websockets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sim_backend.utils.GsonUtilities;
import org.junit.jupiter.api.Test;

public class GsonUtilitiesTest {

  @Test
  public void testToString() {
    assert GsonUtilities.toString(new JsonObject()).equals("{}");
    assert GsonUtilities.toString(new JsonArray()).equals("[]");
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("foo", "bar");
    assert GsonUtilities.toString(jsonObject).equals("{\"foo\":\"bar\"}");
  }
}
