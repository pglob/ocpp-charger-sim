package com.sim_backend.websockets.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** A Deserializer for a ZonedDateTime in the format OCPP expects. */
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
  @Override
  public ZonedDateTime deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return ZonedDateTime.parse(jsonElement.getAsString(), DATE_TIME_FORMATTER);
  }
}
