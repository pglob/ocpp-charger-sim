package com.sim_backend.websockets.gson;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeDeserializer implements JsonDeserializer<ZonedDateTime> {
  /** The Date Format wanted for the json schemas. */
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  /**
   * Deserialize a ZonedDateTime.
   *
   * @param jsonElement Given JsonElement.
   * @param type The Type.
   * @param jsonDeserializationContext The Deserialization Context.
   * @return A Serialized ZonedDateTime.
   */
  @SuppressWarnings("checkstyle:FinalParameters")
  @Override
  public ZonedDateTime deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return ZonedDateTime.parse(jsonElement.getAsString(), DATE_TIME_FORMATTER);
  }
}
