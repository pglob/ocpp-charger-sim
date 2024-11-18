package com.sim_backend.websockets.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sim_backend.websockets.GsonUtilities;
import com.sim_backend.websockets.enums.ErrorCode;

/** A CallError message for OCPP. */
public class OcppMessageError extends OcppMessage {

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

  /**
   * Creates an OCPP error messages.
   *
   * @param errCode The given error code.
   * @param errDescription The given description.
   * @param details given json object (specification says it's undefined how it's laid out).
   */
  public OcppMessageError(
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
  public OcppMessageError(final JsonArray array) {
    super();
    this.messageId = array.get(MESSAGE_ID_INDEX).getAsString();
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
    array.add(OcppMessage.CALL_ID_ERROR);
    array.add(this.getMessageId());
    array.add(this.errorCode.toString());
    array.add(this.errorDescription);
    array.add(GsonUtilities.getGson().toJsonTree(this.errorDetails));

    return array;
  }

  /**
   * Get the error details JsonObject.
   *
   * @return The given Json Object.
   */
  public JsonObject getErrorDetails() {
    return errorDetails;
  }

  /**
   * Get the error description.
   *
   * @return The given error description.
   */
  public String getErrorDescription() {
    return errorDescription;
  }

  /**
   * Get the error code.
   *
   * @return The given error code.
   */
  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
