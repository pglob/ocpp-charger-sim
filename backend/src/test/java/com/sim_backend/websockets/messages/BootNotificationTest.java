package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.utils.GsonUtilities;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class BootNotificationTest {

  private static @NotNull BootNotification getBootNotification() {
    BootNotification notification =
        new BootNotification(
            "CP Vendor",
            "CP Model",
            "CP S/N",
            "Box S/N",
            "Firmware",
            "ICCID",
            "IMSI",
            "Meter Type",
            "Meter S/N");
    assert notification.getChargePointVendor().equals("CP Vendor");
    assert notification.getChargePointModel().equals("CP Model");
    assert notification.getChargePointSerialNumber().equals("CP S/N");
    assert notification.getChargeBoxSerialNumber().equals("Box S/N");
    assert notification.getFirmwareVersion().equals("Firmware");
    assert notification.getIccid().equals("ICCID");
    assert notification.getImsi().equals("IMSI");
    assert notification.getMeterType().equals("Meter Type");
    assert notification.getMeterSerialNumber().equals("Meter S/N");
    return notification;
  }

  @Test
  public void testBootNotification() {
    BootNotification notification = getBootNotification();
    // assert notification.generateMessage().size() == 4;
    String message = GsonUtilities.toString(notification.generateMessage().get("body"));

    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/BootNotification.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);
    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    System.out.println(message);
    assert message.equals(
        "{\"chargePointVendor\":\"CP Vendor\",\"chargePointModel\":\"CP Model\",\"chargePointSerialNumber\":\"CP S/N\",\"chargeBoxSerialNumber\":\"Box S/N\",\"firmwareVersion\":\"Firmware\",\"iccid\":\"ICCID\",\"imsi\":\"IMSI\",\"meterType\":\"Meter Type\",\"meterSerialNumber\":\"Meter S/N\"}");
    assert errors.isEmpty();
  }
}
