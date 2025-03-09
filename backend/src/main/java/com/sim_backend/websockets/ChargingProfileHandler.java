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

  /** A record representing a charging profile along with its stack level for ordering purposes. */
  public record ChargingTuple(ChargingProfile profile, int stackLevel)
      implements Comparable<ChargingTuple> {
    @Override
    public int compareTo(@NotNull ChargingTuple o) {
      return Integer.compare(o.stackLevel, this.stackLevel);
    }
  }

  /** A record representing a charging limit and its corresponding stack level. */
  public record StackLimit(double limit, int stackLevel) implements Comparable<StackLimit> {
    @Override
    public int compareTo(@NotNull StackLimit o) {
      return Integer.compare(o.stackLevel, this.stackLevel);
    }
  }

  // Lists to store charging profiles based on their purpose
  List<ChargingTuple> chargingTuplesMax = new ArrayList<>();
  List<ChargingTuple> chargingTuplesTxDefault = new ArrayList<>();
  List<ChargingTuple> chargingTuplesTx = new ArrayList<>();

  public ChargingProfileHandler(TransactionHandler transactionHandler, OCPPWebSocketClient client) {
    this.transactionHandler = transactionHandler;
    this.client = client;
  }

  /**
   * Adds a new charging profile to the appropriate list based on its purpose. Ensures profiles with
   * the same stack level and purpose do not duplicate.
   */
  public void addChargingProfile(ChargingProfile chargingProfile) {
    ChargingProfilePurpose purpose = chargingProfile.getChargingProfilePurpose();
    List<ChargingTuple> chargingTuples;

    if (purpose.equals(ChargingProfilePurpose.CHARGE_POINT_MAX_PROFILE)) {
      chargingTuples = chargingTuplesMax;
    } else if (purpose.equals(ChargingProfilePurpose.TX_DEFAULT_PROFILE)) {
      chargingTuples = chargingTuplesTxDefault;
    } else {
      chargingTuples = chargingTuplesTx;
    }

    removeDuplicate(chargingProfile, chargingTuples);
    chargingTuples.add(new ChargingTuple(chargingProfile, chargingProfile.getStackLevel()));
    Collections.sort(chargingTuples);
  }

  /** Removes duplicate charging profiles with the same stack level and purpose. */
  public void removeDuplicate(ChargingProfile chargingProfile, List<ChargingTuple> chargingTuples) {
    for (int i = 0; i < chargingTuples.size(); i++) {
      if (chargingTuples
              .get(i)
              .profile
              .getChargingProfilePurpose()
              .equals(chargingProfile.getChargingProfilePurpose())
          && chargingTuples.get(i).stackLevel == chargingProfile.getStackLevel()) {
        chargingTuples.remove(i);
        i--; // Adjust index after removal
      }
    }
  }

  /** Determines the current charging limit based on active profiles and schedules. */
  private StackLimit getCurrentLimit(
      long initialTimestamp, int voltage, List<ChargingTuple> chargingTuples) {
    ZonedDateTime time = client.getScheduler().getTime().getSynchronizedTime();
    double limit = -1;

    for (int i = 0; i < chargingTuples.size(); i++) {
      ChargingSchedule schedule = chargingTuples.get(i).profile.getChargingSchedule();

      // Remove expired profiles
      if (chargingTuples.get(i).profile != null
          && chargingTuples.get(i).profile.getValidTo() != null) {
        if (time.isAfter(chargingTuples.get(i).profile.getValidTo())) {
          chargingTuples.remove(i);
          i--;
          continue;
        }
      }

      // Skip profiles not yet valid
      if (chargingTuples.get(i).profile != null
          && chargingTuples.get(i).profile.getValidFrom() != null) {
        if (time.isBefore(chargingTuples.get(i).profile.getValidFrom())) {
          continue;
        }
      }

      if (schedule.getStartSchedule() != null && time.isBefore(schedule.getStartSchedule())) {
        continue;
      }

      // Ensure the profile belongs to the active transaction
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

      // Find the current applicable limit
      for (int j = 0; j < schedule.getChargingSchedulePeriod().size(); j++) {
        long startPeriod =
            schedule.getChargingSchedulePeriod().get(j).getStartPeriod() * 1000L + startReference;
        if (startReference + startPeriod < convertedTime) {
          limit = schedule.getChargingSchedulePeriod().get(j).getLimit();
        }

        if (startReference + startPeriod > convertedTime) {
          break;
        }
      }

      if (limit != -1 && schedule.getChargingRateUnit() == ChargingRateUnit.WATTS) {
        return new StackLimit(limit / voltage, chargingTuples.get(i).stackLevel);
      }
      else if(limit != -1 && schedule.getChargingRateUnit() == ChargingRateUnit.AMPS){
        return new StackLimit(limit, chargingTuples.get(i).stackLevel);
      }
    }

    return new StackLimit(limit, -1);
  }

  /** Computes the lowest applicable charging limit across different profile types. */
  public double getCurrentLimit(long initialTimestamp, int voltage) {
    StackLimit max = getCurrentLimit(initialTimestamp, voltage, chargingTuplesMax);
    StackLimit txDefault = getCurrentLimit(initialTimestamp, voltage, chargingTuplesTxDefault);
    StackLimit txProfile = getCurrentLimit(initialTimestamp, voltage, chargingTuplesTx);

    List<StackLimit> tuples = new ArrayList<>(List.of(max, txDefault, txProfile));
    Collections.sort(tuples);
    List<StackLimit> highestStackTuples = new ArrayList<>();

    highestStackTuples.add(tuples.getFirst());

    for (int i = 1; i < tuples.size(); i++) {
      if (tuples.get(i).stackLevel == highestStackTuples.getFirst().stackLevel) {
        highestStackTuples.add(tuples.get(i));
      }
    }

    double minimumLimit = Double.MAX_VALUE;
    for (StackLimit highestStackTuple : highestStackTuples) {
      if (highestStackTuple.limit != -1 && highestStackTuple.limit < minimumLimit) {
        minimumLimit = highestStackTuple.limit;
      }
    }

    return minimumLimit;
  }
}
