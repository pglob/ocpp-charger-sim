package com.sim_backend.websockets;

import com.sim_backend.websockets.exceptions.OCPPMessageFailure;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

/** A class for handling an OCPP Message Queue. */
public class MessageQueue {
  @Getter
  @AllArgsConstructor
  static class TimedMessage {
    private final OCPPMessage message;
    private final Instant timestamp;

    public TimedMessage(OCPPMessage message) {
      this.message = message;
      this.timestamp = Instant.now();
    }
  }

  /** The number of reattempts to resend a message. */
  public static final int MAX_REATTEMPTS = 5;

  /** The time we consider a Request Message not valid anymore */
  public static final int RESPONSE_TIME_OUT = 15;

  /** The previous messages we have sent. */
  private final Map<String, TimedMessage> previousMessages = new ConcurrentHashMap<>();

  /** The OCPP Message Queue. */
  private final Deque<OCPPMessage> queue = new LinkedList<>();

  /** Create an OCPPMessage Queue. */
  public MessageQueue() {}

  /**
   * Add a OCPPMessage to our send queue.
   *
   * @param message the message to be sent.
   */
  public void pushMessage(final OCPPMessage message) {
    queue.add(message);
  }

  /**
   * Return the size of the send queue.
   *
   * @return size in int.
   */
  public int size() {
    return queue.size();
  }

  /**
   * Test if the send queue is empty.
   *
   * @return true if empty.
   */
  public boolean isEmpty() {
    return queue.isEmpty();
  }

  /**
   * Pop and send the message on top of the send queue.
   *
   * @param client The WebsocketClient to send it through.
   * @return The Send OCPP Message.
   */
  public OCPPMessage popMessage(final OCPPWebSocketClient client)
      throws OCPPMessageFailure, InterruptedException {
    if (isBusy()) {
      return null;
    }

    OCPPMessage message = queue.poll();
    if (message != null) {
      try {
        message.sendMessage(client);
      } catch (WebsocketNotConnectedException ex) {
        if (message.incrementTries() >= MAX_REATTEMPTS) {
          throw new OCPPMessageFailure(message, ex);
        } else {
          client.reconnectBlocking();
          queue.addFirst(message);
          this.popMessage(client);
        }
      }
    }
    return message;
  }

  /**
   * Pop the entire send queue.
   *
   * @param client The WebsocketClient to send it through.
   */
  public void popAllMessages(final OCPPWebSocketClient client)
      throws OCPPMessageFailure, InterruptedException {
    if (isBusy()) {
      return;
    }
    while (!queue.isEmpty()) {
      popMessage(client);
    }
  }

  /**
   * Check if our queue is busy and will not send out a call.
   *
   * @return if we are busy.
   */
  public boolean isBusy() {
    boolean isReady = true;
    for (Map.Entry<String, TimedMessage> message : previousMessages.entrySet()) {
      Duration duration = Duration.between(message.getValue().timestamp, Instant.now());
      if (duration.getSeconds() <= RESPONSE_TIME_OUT) {
        isReady = false;
      }
    }
    return !isReady;
  }

  /**
   * Add an OCPPMessage to the previous messages.
   *
   * @param msg The message we wish to add.
   */
  public void addPreviousMessage(final OCPPMessage msg) {
    if (msg instanceof OCPPMessageRequest) {
      TimedMessage timedMessage = new TimedMessage(msg);
      this.previousMessages.put(msg.getMessageID(), timedMessage);
    }
  }

  /**
   * Retrieve a previously sent CALL OCPPMessage via it's message ID
   *
   * @param messageID the message ID we are searching for
   * @return The found message or null
   */
  public OCPPMessage getPreviousMessage(final String messageID) {
    TimedMessage timedMessage = previousMessages.get(messageID);

    if (timedMessage != null) {
      return timedMessage.message;
    }

    return null;
  }

  /** Clear a previously sent message from the previous message */
  public void clearPreviousMessage(final OCPPMessage msg) {
    if (msg instanceof OCPPMessageRequest) {
      this.previousMessages.remove(msg.getMessageID());
    }
  }
}
