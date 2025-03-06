package com.sim_backend.websockets;

import com.sim_backend.transactions.TransactionHandler;
import com.sim_backend.websockets.enums.ChargingProfilePurpose;
import com.sim_backend.websockets.enums.ChargingRateUnit;
import com.sim_backend.websockets.types.ChargingProfile;
import com.sim_backend.websockets.types.ChargingSchedule;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChargingProfileHandler {
  TransactionHandler transactionHandler;
  OCPPWebSocketClient client;

  public record ChargingTuple(ChargingProfile profile, int stackLevel)
      implements Comparable<ChargingTuple> {
    @Override
    public int compareTo(@NotNull ChargingTuple o) {
      if (stackLevel < o.stackLevel) return -1;
      return 1;
    }
  }

  public record StackLimit(double limit, int stackLevel) implements Comparable<StackLimit> {
    @Override
    public int compareTo(@NotNull StackLimit o) {
      if (stackLevel < o.stackLevel) return -1;
      return 1;
    }
  }

  List<ChargingTuple> chargingTuplesMax = new ArrayList<>();
  List<ChargingTuple> chargingTuplesTxDefault = new ArrayList<>();
  List<ChargingTuple> chargingTuplesTx = new ArrayList<>();

  public ChargingProfileHandler(TransactionHandler transactionHandler, OCPPWebSocketClient client) {
    this.transactionHandler = transactionHandler;
    this.client = client;
  }

  public void addChargingProfile(ChargingProfile chargingProfile) {

    ChargingProfilePurpose purpose = chargingProfile.getChargingProfilePurpose();
    List<ChargingTuple> chargingTuples = new ArrayList<>();

    if (purpose.equals(ChargingProfilePurpose.CHARGE_POINT_MAX_PROFILE)) {
      chargingTuples = chargingTuplesMax;
    } else if (purpose.equals(ChargingProfilePurpose.TX_DEFAULT_PROFILE)) {
      chargingTuples = chargingTuplesTxDefault;
    } else {
      chargingTuples = chargingTuplesTx;
    }

    ChargingTuple chargingTuple =
        new ChargingTuple(chargingProfile, chargingProfile.getStackLevel());
    removeDuplicate(chargingProfile, chargingTuples);

    chargingTuples.add(new ChargingTuple(chargingProfile, chargingProfile.getStackLevel()));
    Collections.sort(chargingTuples);
  }

  public void removeDuplicate(ChargingProfile chargingProfile, List<ChargingTuple> chargingTuples) {
    for (int i = 0; i < chargingTuples.size(); i++) {
      if (chargingTuples
              .get(i)
              .profile
              .getChargingProfilePurpose()
              .equals(chargingProfile.getChargingProfilePurpose())
          && chargingTuples.get(i).stackLevel == chargingProfile.getStackLevel()) {
        chargingTuples.remove(i);
        i--;
      }
    }
  }

  private StackLimit getCurrentLimit(
      long initialTimestamp, int voltage, List<ChargingTuple> chargingTuples) {
    ZonedDateTime time = client.getScheduler().getTime().getSynchronizedTime();
    double limit = -1;

    for (int i = 0; i < chargingTuples.size(); i++) {
      ChargingSchedule schedule = chargingTuples.get(i).profile.getChargingSchedule();

      if (time.isAfter(chargingTuples.get(i).profile.getValidTo())) {
        chargingTuples.remove(i);
        i--;
        continue;
      }

      if (time.isBefore(chargingTuples.get(i).profile.getValidFrom())) {
        continue;
      }

      if (schedule.getStartSchedule() != null && time.isBefore(schedule.getStartSchedule())) {
        continue;
      }

      if (chargingTuples
          .get(i)
          .profile
          .getChargingProfilePurpose()
          .equals(ChargingProfilePurpose.TX_PROFILE)) {
        if (chargingTuples.get(i).profile.getTransactionId()
            != transactionHandler.getTransactionId().get()) {
          continue;
        }
      }

      long startReference =
          schedule.getStartSchedule() != null
              ? schedule.getStartSchedule().toInstant().toEpochMilli()
              : initialTimestamp;
      long convertedTime = time.toInstant().toEpochMilli();
      if (schedule.getDuration() != null
          && startReference + schedule.getDuration() < convertedTime) {
        continue;
      }

      for (int j = 0; j < schedule.getChargingSchedulePeriod().size(); j++) {
        long startPeriod =
            schedule.getChargingSchedulePeriod().get(j).getStartPeriod() * 1000L + startReference;
        if (startReference + startPeriod < convertedTime) {
          limit =
              chargingTuples
                  .get(i)
                  .profile
                  .getChargingSchedule()
                  .getChargingSchedulePeriod()
                  .get(j)
                  .getLimit();
        }

        if (startReference + startPeriod > convertedTime) {
          break;
        }
      }

      if (limit != -1
          && chargingTuples.get(i).profile.getChargingSchedule().getChargingRateUnit()
              == ChargingRateUnit.WATTS) {
        return new StackLimit(limit / voltage, chargingTuples.get(i).profile.getStackLevel());
      }
    }

    return new StackLimit(limit, -1);
  }

  public double getCurrentLimit(long initialTimestamp, int voltage) {
    StackLimit max = getCurrentLimit(initialTimestamp, voltage, chargingTuplesMax);
    StackLimit txDefault = getCurrentLimit(initialTimestamp, voltage, chargingTuplesTxDefault);
    StackLimit txProfile = getCurrentLimit(initialTimestamp, voltage, chargingTuplesTx);

    List<StackLimit> tuples = new ArrayList<>();
    tuples.add(max);
    tuples.add(txDefault);
    tuples.add(txProfile);
    Collections.sort(tuples);
    List<StackLimit> highestStackTuples = new ArrayList<>();

    highestStackTuples.add(tuples.getFirst());

    for (int i = 1; i < tuples.size(); i++) {
      if (tuples.get(i).equals(highestStackTuples.getFirst())) {
        highestStackTuples.add(tuples.get(i));
      }
    }

    double minimumLimit = -1;
    for (int i = 0; i < highestStackTuples.size(); i++) {
      if (highestStackTuples.get(i).limit != -1 && highestStackTuples.get(i).limit < minimumLimit) {
        minimumLimit = highestStackTuples.get(i).limit;
      }
    }

    return minimumLimit;
  }
}
