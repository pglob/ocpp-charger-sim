package com.sim_backend.websockets.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.ErrorCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/** A CallError message for OCPP. */
@EqualsAndHashCode(callSuper = true)
@Getter
@Slf4j
public class OCPPMessageError extends OCPPMessage implements Cloneable {

  /** The message id index in a received JsonArray. */
  private static final int MESSAGE_ID_INDEX = 1;

  /** The error code index in a received JsonArray. */
  private static final int CODE_INDEX = 2;

  /** The description index in a received JsonArray. */
  private static final int DESCRIPTION_INDEX = 3;

  /** The error details index in a received JsonArray. */
  private static final int DETAIL_INDEX = 4;

  /** The given error code. */
  private final transient ErrorCode errorCode;

  /** The given error description. */
  private final transient String errorDescription;

  /** Any error details. */
  private final transient JsonObject errorDetails;

  /** The message we received our error from */
  @Setter private transient OCPPMessage erroredMessage = null;

  /**
   * Creates an OCPP error messages.
   *
   * @param errCode The given error code.
   * @param errDescription The given description.
   * @param details given json object (specification says it's undefined how it's laid out).
   */
  public OCPPMessageError(
      final ErrorCode errCode, final String errDescription, final JsonObject details) {
    super();
    this.errorCode = errCode;
    this.errorDescription = errDescription;
    this.errorDetails = details;
  }

  /**
   * Creates an OCPP error object given a JsonArray.
   *
   * @param array The given json array.
   */
  public OCPPMessageError(final JsonArray array) {
    super();
    this.messageID = array.get(MESSAGE_ID_INDEX).getAsString();
    this.errorCode = ErrorCode.valueOf(array.get(CODE_INDEX).getAsString());
    this.errorDescription = array.get(DESCRIPTION_INDEX).getAsString();
    this.errorDetails = array.get(DETAIL_INDEX).getAsJsonObject();
  }

  /**
   * Define the structure for a request message.
   *
   * @return The Generated JsonArray.
   */
  @Override
  public JsonArray generateMessage() {
    JsonArray array = new JsonArray();
    array.add(OCPPMessage.CALL_ID_ERROR);
    array.add(this.getMessageID());
    array.add(this.errorCode.toString());
    array.add(this.errorDescription);
    array.add(GsonUtilities.getGson().toJsonTree(this.errorDetails));

    return array;
  }

  @Override
  protected OCPPMessageError clone() {
    return (OCPPMessageError) super.clone();
  }
}
