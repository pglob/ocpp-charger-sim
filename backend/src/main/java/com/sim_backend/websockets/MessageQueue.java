package com.sim_backend.websockets;

import com.sim_backend.websockets.annotations.OCPPMessageInfo;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;
import com.sim_backend.websockets.types.OCPPMessage;
import com.sim_backend.websockets.types.OCPPMessageRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
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
  public static final int RESPONSE_TIME_OUT = 30;

  /** The previous messages we have sent. */
  private final Map<String, TimedMessage> previousMessages = new ConcurrentHashMap<>();

  /** The OCPP Message Queue. */
  @Getter private final Deque<OCPPMessage> queue = new LinkedList<>();

  /** Unique hashes to check for uniqueness */
  @Getter private final Set<OCPPMessage> queueSet = new HashSet<>();

  /** Create an OCPPMessage Queue. */
  public MessageQueue() {}

  /**
   * Add a OCPPMessage to our send queue.
   *
   * @param message the message to be sent.
   */
  public boolean pushMessage(final OCPPMessage message) {
    if (queueSet.contains(message)) {
      return false;
    }

    queue.add(message);
    queueSet.add(message);
    return true;
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
    // Before sending a new message, check for any timed-out previous messages
    checkTimeouts(client);

    OCPPMessage message = queue.poll();
    if (message != null) {
      if (message instanceof OCPPMessageRequest && isBusy()) {
        queue.addLast(message);
        return null;
      }

      try {
        message.sendMessage(client);
        queueSet.remove(message);
      } catch (WebsocketNotConnectedException ex) {
        if (message.incrementTries() >= MAX_REATTEMPTS) {
          throw new OCPPMessageFailure(message, ex);
        } else {
          client.reconnectBlocking();
          queue.addFirst(message);
          return this.popMessage(client);
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
    int size = queue.size();
    for (int i = 0; i < size; i++) {
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

  /**
   * Check for any timed-out previous messages and call onTimeout() on their associated listeners.
   *
   * @param client The OCPPWebSocketClient instance.
   */
  public void checkTimeouts(OCPPWebSocketClient client) {
    Instant now = Instant.now();
    Iterator<Map.Entry<String, TimedMessage>> iterator = previousMessages.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<String, TimedMessage> entry = iterator.next();
      TimedMessage timedMessage = entry.getValue();
      Duration duration = Duration.between(timedMessage.timestamp, now);

      if (duration.getSeconds() > RESPONSE_TIME_OUT) {
        // Map the timed-out message to its complementary message.
        Class<?> complementClass;
        try {
          complementClass = getComplementMessageClass(timedMessage.message.getClass());
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
          continue;
        }

        // Notify all listeners registered for the complementary message type.
        if (client.onReceiveMessage.containsKey(complementClass)) {
          for (var listener : client.onReceiveMessage.get(complementClass)) {
            listener.onTimeout();
          }
        }
        // Remove the timed-out message.
        iterator.remove();
      }
    }
  }

  /**
   * Finds the complementary class for a given message. For example Heartbeat -> HeartbeatResponse
   * and HeartbeatResponse -> Heartbeat.
   *
   * @param messageClass The class to "invert"
   */
  private Class<?> getComplementMessageClass(Class<?> messageClass) throws ClassNotFoundException {
    OCPPMessageInfo info = messageClass.getAnnotation(OCPPMessageInfo.class);
    if (info == null) {
      throw new ClassNotFoundException(
          messageClass.toString() + " annotation not defined correctly");
    }

    // For a request, add "Response" to the message name
    if (info.messageCallID() == OCPPMessage.CALL_ID_REQUEST) {
      String responseName = info.messageName() + "Response";
      Class<?> complement = OCPPMessage.getMessageByName(responseName);
      if (complement != null) {
        return complement;
      }
    }
    // For a response, remove "Response" from the message name
    else if (info.messageCallID() == OCPPMessage.CALL_ID_RESPONSE) {
      String reqName = info.messageName();
      reqName = reqName.substring(0, reqName.length() - "Response".length());
      Class<?> complement = OCPPMessage.getMessageByName(reqName);
      if (complement != null) {
        return complement;
      }
    }

    // This class is not setup correctly
    throw new ClassNotFoundException(messageClass.toString() + " is missing its complement");
  }
}
