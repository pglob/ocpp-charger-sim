package com.sim_backend.electrical;

import static org.junit.jupiter.api.Assertions.*;

import com.sim_backend.charger.Charger;
import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.OCPPWebSocketClient;
import com.sim_backend.websockets.enums.ChargingProfileKind;
import com.sim_backend.websockets.enums.ChargingProfilePurpose;
import com.sim_backend.websockets.enums.ChargingRateUnit;
import com.sim_backend.websockets.observers.StatusNotificationObserver;
import com.sim_backend.websockets.types.ChargingProfile;
import com.sim_backend.websockets.types.ChargingSchedule;
import com.sim_backend.websockets.types.ChargingSchedulePeriod;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChargingProfileHandlerTest {

  private TransactionHandler transactionHandler;
  private OCPPWebSocketClient client;
  private ChargingProfileHandler chargingProfileHandler;

  @BeforeEach
  void setUp() {
    transactionHandler = new TransactionHandler(new Charger());
    try {
      client = new OCPPWebSocketClient(new URI(""), new StatusNotificationObserver());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    chargingProfileHandler = new ChargingProfileHandler(transactionHandler, client);
  }

  private ChargingProfile createChargingProfile(
      int id, int stackLevel, ChargingProfilePurpose purpose) {
    ChargingSchedulePeriod schedulePeriod = new ChargingSchedulePeriod(100, 40, null);
    ChargingSchedule chargingSchedule =
        new ChargingSchedule(null, null, ChargingRateUnit.AMPS, List.of(schedulePeriod), null);

    return new ChargingProfile(
        id,
        null,
        stackLevel,
        purpose,
        ChargingProfileKind.ABSOLUTE,
        null,
        null,
        null,
        chargingSchedule);
  }

  @Test
  void testAddChargingProfile() {
    ChargingProfile chargingProfile =
        createChargingProfile(1, 0, ChargingProfilePurpose.TX_PROFILE);
    chargingProfileHandler.addChargingProfile(chargingProfile);

    assertEquals(1, chargingProfileHandler.getChargingTuplesTx().size());
    assertEquals(chargingProfile, chargingProfileHandler.getChargingTuplesTx().get(0).profile());
    assertEquals(
        40,
        chargingProfile.getChargingSchedule().getChargingSchedulePeriod().getFirst().getLimit());
  }

  @Test
  void testAddDuplicateChargingProfile() {
    ChargingProfile chargingProfile1 =
        createChargingProfile(1, 0, ChargingProfilePurpose.TX_PROFILE);
    ChargingProfile chargingProfile2 =
        createChargingProfile(2, 0, ChargingProfilePurpose.TX_PROFILE);

    chargingProfileHandler.addChargingProfile(chargingProfile1);
    chargingProfileHandler.addChargingProfile(chargingProfile2);

    assertEquals(
        40,
        chargingProfile1.getChargingSchedule().getChargingSchedulePeriod().getFirst().getLimit());

    assertEquals(
        40,
        chargingProfile2.getChargingSchedule().getChargingSchedulePeriod().getFirst().getLimit());

    assertEquals(1, chargingProfileHandler.getChargingTuplesTx().size());
    assertEquals(chargingProfile2, chargingProfileHandler.getChargingTuplesTx().get(0).profile());
  }

  @Test
  void testAddDifferentStackLevels() {
    ChargingProfile chargingProfile1 =
        createChargingProfile(1, 0, ChargingProfilePurpose.TX_PROFILE);
    ChargingProfile chargingProfile2 =
        createChargingProfile(2, 1, ChargingProfilePurpose.TX_PROFILE);

    chargingProfileHandler.addChargingProfile(chargingProfile1);
    chargingProfileHandler.addChargingProfile(chargingProfile2);

    assertEquals(
        40,
        chargingProfile1.getChargingSchedule().getChargingSchedulePeriod().getFirst().getLimit());

    assertEquals(
        40,
        chargingProfile2.getChargingSchedule().getChargingSchedulePeriod().getFirst().getLimit());

    assertEquals(2, chargingProfileHandler.getChargingTuplesTx().size());
  }

  @Test
  void testAddProfilesWithDifferentPurposes() {
    ChargingProfile chargingProfile1 =
        createChargingProfile(1, 0, ChargingProfilePurpose.TX_PROFILE);
    ChargingProfile chargingProfile2 =
        createChargingProfile(2, 0, ChargingProfilePurpose.CHARGE_POINT_MAX_PROFILE);

    chargingProfileHandler.addChargingProfile(chargingProfile1);
    chargingProfileHandler.addChargingProfile(chargingProfile2);

    assertEquals(
        40,
        chargingProfile1.getChargingSchedule().getChargingSchedulePeriod().getFirst().getLimit());

    assertEquals(
        40,
        chargingProfile2.getChargingSchedule().getChargingSchedulePeriod().getFirst().getLimit());

    assertEquals(1, chargingProfileHandler.getChargingTuplesTx().size());
    assertEquals(1, chargingProfileHandler.getChargingTuplesMax().size());
  }

  @Test
  void testGetCurrentLimitWhenNoProfilesExist() {
    double limit = chargingProfileHandler.getCurrentLimit(System.currentTimeMillis(), 230);
    assertEquals(Double.MAX_VALUE, limit);
  }
}
