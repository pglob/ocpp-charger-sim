package com.sim_backend.websockets.messages;

import com.google.gson.annotations.SerializedName;
import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.enums.MeterValuesSampledData;
import com.sim_backend.websockets.enums.ReadingContext;
import com.sim_backend.websockets.enums.UnitOfMeasure;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** A OCPP 1.6 MeterValues Request Message. */
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@OCPPMessageInfo(messageCallID = OCPPMessage.CALL_ID_REQUEST, messageName = "MeterValues")
public final class MeterValues extends OCPPMessageRequest implements Cloneable {

  /** The connector to which these meter samples are related. */
  @Min(value = 0, message = "MeterValues connectorId must be a non-negative integer")
  @SerializedName("connectorId")
  private int connectorId;

  /** The transaction to which these meter samples are related (optional). */
  @Min(value = 0, message = "MeterValues transactionId must be a non-negative integer")
  @SerializedName("transactionId")
  private Integer transactionId;

  /** The samples meter values with timestamps */
  @Size(min = 1, message = "meterValue must contain at least one element")
  @SerializedName("meterValue")
  private final List<MeterValue> meterValue;

  @Override
  protected MeterValues clone() {
    return (MeterValues) super.clone();
  }

  /** Collection of one or more sampled values. */
  @Getter
  @AllArgsConstructor
  public static class MeterValue {
    /** Timestamp of the readings. */
    @NotNull(message = "MeterValue timestamp is required")
    @SerializedName("timestamp")
    private ZonedDateTime timestamp;

    /** The measured values. */
    @Size(min = 1, message = "sampledValue must contain at least one element")
    @SerializedName("sampledValue")
    private final List<SampledValue> sampledValue;
  }

  /** A single sampled meter value. */
  @Getter
  @AllArgsConstructor
  public static class SampledValue {
    /** Actual value of the reading. Can be fractional (with decimal). */
    @NotNull(message = "SampledValue value is required and cannot be blank")
    @SerializedName("value")
    private String value;

    /** The reason for the reading. */
    @SerializedName("context")
    private ReadingContext context;

    /** The type of reading. */
    @SerializedName("measurand")
    private MeterValuesSampledData measurand;

    /** The unit of the reading. */
    @SerializedName("unit")
    private UnitOfMeasure unit;
  }
}
