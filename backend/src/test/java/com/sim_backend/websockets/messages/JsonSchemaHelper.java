package com.sim_backend.websockets.messages;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;

import java.io.InputStream;

public abstract class JsonSchemaHelper {
    /**
     * You cannot create this.
     */
    private JsonSchemaHelper() {}

    /**
     * Get a JSON Schema from a Resource.
     * @param name The Resource to be loaded in.
     * @return A Processed JsonSchema.
     */
    public static JsonSchema getJsonSchema(String name) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        InputStream test = JsonSchemaHelper.class.getResourceAsStream(name);
        return factory.getSchema(test);
    }
}
