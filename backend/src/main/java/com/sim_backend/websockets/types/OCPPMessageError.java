package com.sim_backend.websockets.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sim_backend.websockets.GsonUtilities;

public class OCPPMessageError extends OCPPMessage {
  /** The error code index in a received JsonArray. */
  public static final int CODE_INDEX = 2;

  /** The description index in a received JsonArray. */
  public static final int DESCRIPTION_INDEX = 3;

  /** The error details index in a received JsonArray. */
  public static final int DETAIL_INDEX = 4;

  /** The given error code. */
  private final transient String errorCode;

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
  public OCPPMessageError(
      final String errCode, final String errDescription, final JsonObject details) {
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
    this.errorCode = array.get(CODE_INDEX).getAsString();
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
    array.add(this.errorCode);
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
  public String getErrorCode() {
    return errorCode;
  }
}
