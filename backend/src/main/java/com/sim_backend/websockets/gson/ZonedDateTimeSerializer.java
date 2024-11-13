package com.sim_backend.websockets.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeSerializer implements JsonSerializer<ZonedDateTime> {
    /**
     * The Date Format wanted for the json schemas.
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Serialize a ZonedDateTime.
     * @param zonedDateTime Given ZonedDateTime.
     * @param type The Type.
     * @param jsonSerializationContext The Serialization Context.
     * @return A Serialized ZonedDateTime.
     */
    @SuppressWarnings("checkstyle:FinalParameters")
    @Override
    public JsonElement serialize(ZonedDateTime zonedDateTime,
                                 Type type,
                                 JsonSerializationContext
                                             jsonSerializationContext) {
        return new JsonPrimitive(DATE_TIME_FORMATTER.format(zonedDateTime));
    }
}
