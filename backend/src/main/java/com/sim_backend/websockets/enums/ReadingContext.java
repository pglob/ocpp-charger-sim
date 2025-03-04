package com.sim_backend.websockets.enums;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/** Enum representing the context of a sampled value. */
@Getter
public enum ReadingContext {

  /* Value taken at start of interruption. */
  @SerializedName("Interruption.Begin")
  INTERUPTION_BEGIN("Interruption.Begin"),

  /* Value taken when resuming after interruption. */
  @SerializedName("Interruption.End")
  INTERRUPTION_END("Interruption.End"),

  /* Value for any other situations. */
  @SerializedName("Other")
  OTHER("Other"),

  /* Value taken at clock aligned interval. */
  @SerializedName("Sample.Clock")
  SAMPLE_CLOCK("Sample.Clock"),

  /* Value taken as periodic sample relative to start time of transaction. */
  @SerializedName("Sample.Periodic")
  SAMPLE_PERIODIC("Sample.Periodic"),

  /* Value taken at start of transaction. */
  @SerializedName("Transaction.Begin")
  TRANSACTION_BEGIN("Transaction.Begin"),

  /* Value taken at end of transaction. */
  @SerializedName("Transaction.End")
  TRANSACTION_END("Transaction.End"),

  /* Value taken in response to a TriggerMessage.req */
  @SerializedName("Trigger")
  TRIGGER("Trigger");

  private final String value;

  ReadingContext(String value) {
    this.value = value;
  }

  public static ReadingContext fromString(String value) {
    for (ReadingContext data : ReadingContext.values()) {
      if (data.getValue().equals(value)) {
        return data;
      }
    }
    throw new IllegalArgumentException("Unknown ReadingContext: " + value);
  }
}
