package com.sim_backend.websockets.exceptions;

/** Thrown when we receive a message ID we did not send. */
public class OCPPCannotProcessResponse extends RuntimeException {

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

  /** Get The unable to be matched messageID. */
  public String getBadMessageId() {
    return badMessageId;
  }

  /** Get the full message we received. */
  public String getReceivedMessage() {
    return receivedMessage;
  }
}
