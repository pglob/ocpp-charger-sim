package com.sim_backend.websockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.sim_backend.websockets.gson.ZonedDateTimeDeserializer;
import com.sim_backend.websockets.gson.ZonedDateTimeSerializer;
import java.time.ZonedDateTime;

public abstract class GsonUtilities {
  /** You cannot create this. */
  private GsonUtilities() {}

  /**
   * Get a Gson Object with our custom TypeAdapters installed.
   *
   * @return The Gson Object.
   */
  public static Gson getGson() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeSerializer());
    gsonBuilder.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeDeserializer());

    return gsonBuilder.create();
  }

  /**
   * Converts a JsonElement to a JSON Formatted String.
   *
   * @param json The JsonElement to convert.
   * @return The JsonElement converted to a String.
   */
  public static String toString(final JsonElement json) {
    Gson gson = getGson();
    return gson.toJson(json, JsonElement.class);
  }
}
