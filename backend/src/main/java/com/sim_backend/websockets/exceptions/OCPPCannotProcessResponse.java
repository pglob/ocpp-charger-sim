package com.sim_backend.websockets.exceptions;

import lombok.Getter;

/** Thrown when we receive a message ID we did not send. */
@Getter
public class OCPPCannotProcessResponse extends Exception {

  /** The Received message we could not process due to us not having a matching message ID. */
  private final String receivedMessage;

  /** The unable to be matched messageID. */
  private final String badMessageId;

  /**
   * Thrown when we receive a message with an ID we did not send.
   *
   * @param receivedMsg The received message.
   * @param badMsgId The message ID we cannot match.
   */
  public OCPPCannotProcessResponse(final String receivedMsg, final String badMsgId) {
    this.receivedMessage = receivedMsg;
    this.badMessageId = badMsgId;
  }

  /**
   * Get a String representation of an exception thrown when we receive a message with a message ID
   * we cannot process.
   *
   * @return The exception as a string.
   */
  @Override
  public String getMessage() {
    return String.format(
        "Received Message with ID \"%s\" where we do not have a matching sent message: %s",
        badMessageId, receivedMessage);
  }
}
