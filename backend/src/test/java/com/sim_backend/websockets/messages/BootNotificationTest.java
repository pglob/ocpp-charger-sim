package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class BootNotificationTest {

    @Test
    public void testBootNotification() {
        BootNotification notification = new BootNotification(
                "CP Vendor",
                "CP Model",
                "CP S/N",
                "Box S/N",
                "Firmware",
                "ICCID",
                "IMSI",
                "Meter Type",
                "Meter S/N");
        String message = GsonUtilities.toString(notification.generateMessage());


        JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/BootNotification.json");
        Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);
        if (!errors.isEmpty()) {
            for (ValidationMessage error : errors) {
                System.out.println(error);
            }
        }

        assert message.equals("{\"chargePointVendor\":\"CP Vendor\",\"chargePointModel\":\"CP Model\",\"chargePointSerialNumber\":\"CP S/N\",\"chargeBoxSerialNumber\":\"Box S/N\",\"firmwareVersion\":\"Firmware\",\"iccid\":\"ICCID\",\"imsi\":\"IMSI\",\"meterType\":\"Meter Type\",\"meterSerialNumber\":\"Meter S/N\"}");
        assert errors.isEmpty();
    }
}
