package com.sim_backend.websockets.gson;

import com.ethlo.time.DateTime;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class ZonedDateTimeSerializer implements JsonSerializer<ZonedDateTime> {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    @Override
    public JsonElement serialize(ZonedDateTime zonedDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(dateFormat.format(zonedDateTime));
    }
}
