package com.sim_backend.websockets.messages;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.ChargingProfileKind;
import com.sim_backend.websockets.enums.ChargingProfilePurpose;
import com.sim_backend.websockets.enums.ChargingRateUnit;
import com.sim_backend.websockets.messages.SetChargingProfile.ChargingProfile;
import com.sim_backend.websockets.messages.SetChargingProfile.ChargingSchedule;
import com.sim_backend.websockets.messages.SetChargingProfile.ChargingSchedulePeriod;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class RemoteStartTransactionTest {

  private static @NotNull RemoteStartTransaction getRemoteStartTransaction() {
    ChargingSchedulePeriod chargingSchedulePeriodtoadd = new ChargingSchedulePeriod(0, 0, null);
    List<ChargingSchedulePeriod> chargingSchedulePeriod = List.of(chargingSchedulePeriodtoadd);
    ChargingSchedule chargingSchedule =
        new ChargingSchedule(null, null, ChargingRateUnit.AMPS, chargingSchedulePeriod, null);
    ChargingProfile testChargingProfile =
        new ChargingProfile(
            0,
            11,
            0,
            ChargingProfilePurpose.TX_PROFILE,
            ChargingProfileKind.RECURRING,
            null,
            null,
            null,
            chargingSchedule);
    return new RemoteStartTransaction("testIdTag", 1, testChargingProfile);
  }

  @Test
  public void testRemoteStartTransactionRequest() {
    RemoteStartTransaction request = getRemoteStartTransaction();

    // Ensure message generation works
    assert request.generateMessage().size() == 4;
    String message = GsonUtilities.toString(request.generateMessage().get(3));

    // Validate against schema
    JsonSchema jsonSchema = JsonSchemaHelper.getJsonSchema("schemas/RemoteStartTransaction.json");
    Set<ValidationMessage> errors = jsonSchema.validate(message, InputFormat.JSON);

    if (!errors.isEmpty()) {
      for (ValidationMessage error : errors) {
        System.out.println(error);
      }
    }

    // Check expected message structure
    assert message.equals(
        "{\"idTag\":\"testIdTag\",\"connectorId\":1,"
            + "\"chargingProfile\":{\"chargingProfileId\":0,\"transactionId\":11,"
            + "\"stackLevel\":0,\"chargingProfilePurpose\":\"TxProfile\","
            + "\"chargingProfileKind\":\"Recurring\",\"chargingSchedule\":"
            + "{\"chargingRateUnit\":\"A\","
            + "\"chargingSchedulePeriod\":[{\"startPeriod\":0,\"limit\":0.0}]}}}");
    assert errors.isEmpty();
  }
}
