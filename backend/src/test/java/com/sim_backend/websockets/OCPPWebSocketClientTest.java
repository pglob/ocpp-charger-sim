package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sim_backend.websockets.enums.ErrorCode;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.exceptions.*;
import com.sim_backend.websockets.messages.*;
import com.sim_backend.websockets.observers.StatusNotificationObserver;
import com.sim_backend.websockets.types.OCPPMessageError;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Pattern;
import javax.net.SocketFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OCPPWebSocketClientTest {

  public static class TestOCPPWebSocketClient extends OCPPWebSocketClient {
    public TestOCPPWebSocketClient(
        URI serverUri, StatusNotificationObserver statusNotificationObserver) {
      super(serverUri, statusNotificationObserver);
    }

    @Override
    public void startConnectionLostTimer() {}

    @Override
    public boolean connectBlocking() throws InterruptedException {
      return true;
    }

    @Override
    public boolean reconnectBlocking() throws InterruptedException {
      return true;
    }
  }

  TestOCPPWebSocketClient client;
  MessageQueue queue;
  OnOCPPMessage onOCPPMessageMock;
  StatusNotificationObserver statusNotificationObserver;

  @BeforeEach
  void setUp() throws URISyntaxException {
    onOCPPMessageMock = mock(OnOCPPMessage.class);
    client = spy(new TestOCPPWebSocketClient(new URI(""), statusNotificationObserver));
    queue = mock(MessageQueue.class);
  }

  @Test
  public void testPopOnEmptyQueue() throws OCPPMessageFailure, InterruptedException {
    assert client.popMessage() == null;
  }

  @Test
  public void testPushThenPop() throws OCPPMessageFailure, InterruptedException {
    Heartbeat heartbeat = new Heartbeat();
    doReturn(heartbeat).when(client).popMessage();
    client.pushMessage(heartbeat);

    assert client.popMessage() == heartbeat;
  }

  @Test
  public void testPopMessage() throws OCPPMessageFailure, InterruptedException {
    Pattern pattern = Pattern.compile("^\\[2,\\s*\".*?\",\\s*\"Heartbeat\",\\s*\\{}]$");
    doAnswer(
            invocation -> {
              assert pattern.matcher(invocation.getArgument(0, String.class)).find();
              return null;
            })
        .when(client)
        .send(anyString());

    Heartbeat beat = new Heartbeat();
    client.pushMessage(beat);
    client.popMessage();
    verify(client, times(1)).send(anyString());
  }

  @Test
  void testPopAllMessages() throws OCPPMessageFailure, InterruptedException {
    doAnswer(invocation -> null).when(client).send(anyString());

    Heartbeat beat = new Heartbeat();
    HeartbeatResponse beat2 = new HeartbeatResponse(new Heartbeat());

    client.pushMessage(beat);
    assert client.size() == 1;
    client.pushMessage(beat2);
    assert client.size() == 2;

    client.popAllMessages();
    assert client.isEmpty();

    verify(client, times(2)).send(anyString());
  }

  @Test
  public void testBadCallID() throws InterruptedException, OCPPMessageFailure {
    String badResponseMsg = "[1, \"Cool\", \"Heartbeat\", {}]";
    Heartbeat beat = new Heartbeat();
    doAnswer(
            invocation -> {
              OCPPBadCallID badResponse =
                  assertThrows(
                      OCPPBadCallID.class,
                      () -> {
                        client.handleMessage(badResponseMsg);
                      });
              assert badResponse.getFullMessage().equals(badResponseMsg);
              assert badResponse.getBadCallId() == 1;
              return null;
            })
        .when(client)
        .send(anyString());

    client.pushMessage(beat);
    client.onReceiveMessage(HeartbeatResponse.class, message -> {});

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
  }

  @Test
  public void testBadCallID2() throws InterruptedException, OCPPMessageFailure {
    String badResponseMsg = "[5, \"Cool\", \"Heartbeat\", {}]";
    Heartbeat beat = new Heartbeat();
    doAnswer(
            invocation -> {
              OCPPBadCallID badResponse =
                  assertThrows(
                      OCPPBadCallID.class,
                      () -> {
                        client.handleMessage(badResponseMsg);
                      });
              assert badResponse.getFullMessage().equals(badResponseMsg);
              assert badResponse.getBadCallId() == 5;
              return null;
            })
        .when(client)
        .send(anyString());

    client.pushMessage(beat);
    client.onReceiveMessage(HeartbeatResponse.class, message -> {});

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
  }

  @Test
  public void testOnReceiveError() {
    OnOCPPMessageListener listener = mock(OnOCPPMessageListener.class);
    OCPPMessageError messageError =
        new OCPPMessageError(ErrorCode.FormatViolation, "Not Found", new JsonObject());

    String fullMessage = messageError.toJsonString();
    Heartbeat beat = new Heartbeat();
    beat.setMessageID(messageError.getMessageID());
    client.addPreviousMessage(beat);

    client.onReceiveMessage(
        OCPPMessageError.class,
        message -> {
          assert message.getMessage() instanceof OCPPMessageError;
          OCPPMessageError receivedError = (OCPPMessageError) message.getMessage();
          assert receivedError.getErrorCode() == ErrorCode.FormatViolation;
          assert receivedError.getErrorDescription().equals("Not Found");
          assert receivedError.getErrorDetails() != null;
        });
    client.onMessage(fullMessage);

    // verify(listener, times(1)).onMessageReceieved(any(OnOCPPMessage.class));
  }

  @Test
  public void testOnReceiveMulti() {
    OnOCPPMessageListener listener = mock(OnOCPPMessageListener.class);
    OCPPMessageError messageError =
        new OCPPMessageError(ErrorCode.FormatViolation, "Not Found", new JsonObject());

    String fullMessage = messageError.toJsonString();
    Heartbeat beat = new Heartbeat();
    beat.setMessageID(messageError.getMessageID());
    client.addPreviousMessage(beat);

    client.onReceiveMessage(
        OCPPMessageError.class,
        message -> {
          assert message.getMessage() instanceof OCPPMessageError;
          OCPPMessageError receivedError = (OCPPMessageError) message.getMessage();
          assert receivedError.getErrorCode() == ErrorCode.FormatViolation;
          assert receivedError.getErrorDescription().equals("Not Found");
          assert receivedError.getErrorDetails() != null;
        });
    client.onReceiveMessage(
        Heartbeat.class,
        message -> {
          assert message.getMessage() instanceof Heartbeat;
        });
    client.onMessage(fullMessage);
    client.onMessage(new Heartbeat().toJsonString());

    // verify(listener, times(1)).onMessageReceieved(any(OnOCPPMessage.class));
  }

  @Test
  public void testOnReceiveMessageNoMatchingMsg() throws OCPPMessageFailure, InterruptedException {
    HeartbeatResponse response = new HeartbeatResponse(new Heartbeat());

    OCPPMessageError error = new OCPPMessageError(ErrorCode.FormatViolation, "", new JsonObject());

    Heartbeat beat = new Heartbeat();
    doAnswer(
            invocation -> {
              response.setMessageID(beat.getMessageID());
              String fullMessage = response.toJsonString();
              OCPPCannotProcessMessage badResponse =
                  assertThrows(
                      OCPPCannotProcessMessage.class,
                      () -> {
                        client.handleMessage(fullMessage);
                      });
              assert badResponse.getReceivedMessage().equals(fullMessage);
              assert badResponse.getBadMessageId().equals(beat.getMessageID());

              return null;
            })
        .when(client)
        .send(anyString());

    client.pushMessage(beat);
    client.onReceiveMessage(HeartbeatResponse.class, message -> {});

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
  }

  @Test
  public void testOnReceiveErrorMessageNoMatchingMsg()
      throws OCPPMessageFailure, InterruptedException {
    OCPPMessageError error = new OCPPMessageError(ErrorCode.FormatViolation, "", new JsonObject());

    doAnswer(
            invocation -> {
              String fullMessage = error.toJsonString();
              assertThrows(
                  OCPPCannotProcessMessage.class,
                  () -> {
                    client.handleMessage(fullMessage);
                  });

              return null;
            })
        .when(client)
        .send(anyString());

    client.pushMessage(error);
    client.onReceiveMessage(OCPPMessageError.class, message -> {});
    client.popAllMessages();
  }

  @Test
  public void testOnReceiveMessage() throws OCPPMessageFailure, InterruptedException {
    HeartbeatResponse response = new HeartbeatResponse(new Heartbeat());

    Heartbeat beat = new Heartbeat();
    doAnswer(
            invocation -> {
              client.addPreviousMessage(beat);
              response.setMessageID(beat.getMessageID());
              client.onMessage(response.toJsonString());
              return null;
            })
        .when(client)
        .send(anyString());

    client.pushMessage(beat);
    client.onReceiveMessage(
        HeartbeatResponse.class,
        message -> {
          assert message.getMessage() instanceof HeartbeatResponse;
          HeartbeatResponse receivedResponse = (HeartbeatResponse) message.getMessage();
          assert receivedResponse.getCurrentTime().isEqual(response.getCurrentTime());
        });

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
  }

  @Test
  public void testReceiveBadMessage() throws OCPPMessageFailure, InterruptedException {
    doAnswer(
            invocation -> {
              client.handleMessage("{}");
              return null;
            })
        .when(client)
        .send(anyString());

    Heartbeat beat = new Heartbeat();

    client.pushMessage(beat);
    client.onReceiveMessage(HeartbeatResponse.class, message -> {});
    JsonParseException err = assertThrows(JsonParseException.class, () -> client.popAllMessages());
    assert err != null;
    assert err.getMessage().startsWith("Expected array");
  }

  @Test
  public void testOnReceiveBadMessageType() throws OCPPMessageFailure, InterruptedException {
    String msgToSend = "[2, \"Woah\", \"AbsoluteTrash\", {}]";
    doAnswer(
            invocation -> {
              client.handleMessage(msgToSend);
              return null;
            })
        .when(client)
        .send(anyString());

    Heartbeat beat = new Heartbeat();

    client.pushMessage(beat);
    client.onReceiveMessage(HeartbeatResponse.class, message -> {});
    OCPPUnsupportedMessage err =
        assertThrows(OCPPUnsupportedMessage.class, () -> client.popAllMessages());
    assert err != null;
    assert err.getMessageName().equals("AbsoluteTrash");
    assert err.getFullMessage().equals(msgToSend);
  }

  @Test
  public void testAllThrowsException() throws OCPPMessageFailure, InterruptedException {
    doAnswer(invocation -> false).when(client).reconnectBlocking();
    Heartbeat beat = new Heartbeat();

    client.pushMessage(beat);
    assert client.size() == 1;

    OCPPMessageFailure exception =
        assertThrows(
            OCPPMessageFailure.class,
            () -> {
              client.popAllMessages();
            });

    assert exception != null;
    assert exception.getFailedMessage() == beat;
    assert exception.getFailedMessage().incrementTries() == MessageQueue.MAX_REATTEMPTS + 1;
    assert exception.getInnerException() != null;
  }

  @Test
  public void testThrowsException() throws OCPPMessageFailure, InterruptedException {
    doAnswer(invocation -> false).when(client).reconnectBlocking();
    Heartbeat beat = new Heartbeat();

    client.pushMessage(beat);
    assert client.size() == 1;

    OCPPMessageFailure exception =
        assertThrows(
            OCPPMessageFailure.class,
            () -> {
              client.popMessage();
            });
    assert exception != null;
    assert exception.getFailedMessage() == beat;
    assert exception.getFailedMessage().incrementTries() == MessageQueue.MAX_REATTEMPTS + 1;
    assert exception.getInnerException() != null;
  }

  @Test
  public void testRetryAfterFirstAttempt() throws Exception {
    doAnswer(invocation -> false).when(client).reconnectBlocking();
    Heartbeat beat = new Heartbeat();

    client.pushMessage(beat);
    assert client.size() == 1;

    client.onReceiveMessage(
        HeartbeatResponse.class,
        message -> {
          assert message.getMessage() instanceof HeartbeatResponse;
        });

    doAnswer(
            invocation -> {
              doAnswer(
                      invocation2 -> {
                        this.client.addPreviousMessage(beat);
                        HeartbeatResponse response = new HeartbeatResponse(new Heartbeat());
                        response.setMessageID(beat.getMessageID());
                        client.handleMessage(response.toJsonString());
                        return null;
                      })
                  .when(client)
                  .send(anyString());
              return true;
            })
        .when(client)
        .reconnectBlocking();

    client.popMessage();
    verify(client, times(2)).send(anyString());
    verify(client, times(1)).handleMessage(anyString());
  }

  @Test
  public void testBadClass() {
    assertThrows(
        OCPPBadClass.class,
        () -> {
          client.onReceiveMessage(MessageQueue.class, message -> {});
        });
  }

  @Test
  public void testClearOnReceivedMessage() {
    assertThrows(
        OCPPBadClass.class,
        () -> {
          client.clearOnReceiveMessage(String.class);
        });

    client.onReceiveMessage(Heartbeat.class, message -> {});
    client.onReceiveMessage(Heartbeat.class, message -> {});
    client.onReceiveMessage(Heartbeat.class, message -> {});
    assertEquals(3, client.onReceiveMessage.get(Heartbeat.class).size());

    client.clearOnReceiveMessage(Heartbeat.class);

    assertNull(client.onReceiveMessage.get(Heartbeat.class));
  }

  @Test
  public void testDeleteOnReceivedMessage() {
    assertThrows(
        OCPPBadClass.class,
        () -> {
          client.deleteOnReceiveMessage(
              String.class,
              new OnOCPPMessageListener() {
                @Override
                public void onMessageReceived(OnOCPPMessage message) {}
              });
        });
    OnOCPPMessageListener listener =
        new OnOCPPMessageListener() {
          @Override
          public void onMessageReceived(OnOCPPMessage message) {}
        };

    client.deleteOnReceiveMessage(Heartbeat.class, listener);
    assertNull(client.onReceiveMessage.get(Heartbeat.class));

    client.onReceiveMessage(Heartbeat.class, listener);

    assertNotEquals(-1, client.onReceiveMessage.get(Heartbeat.class).indexOf(listener));

    client.deleteOnReceiveMessage(Heartbeat.class, listener);
    assertNull(client.onReceiveMessage.get(Heartbeat.class));

    client.onReceiveMessage(Heartbeat.class, message -> {});
    client.onReceiveMessage(Heartbeat.class, message -> {});
    client.onReceiveMessage(Heartbeat.class, listener);

    assertNotEquals(-1, client.onReceiveMessage.get(Heartbeat.class).indexOf(listener));

    client.deleteOnReceiveMessage(Heartbeat.class, listener);
    assertEquals(-1, client.onReceiveMessage.get(Heartbeat.class).indexOf(listener));
  }

  @Test
  void testRequestSynchronicity() throws Exception {
    doAnswer(invocation -> null).when(client).send(anyString());

    Heartbeat beat = new Heartbeat();
    HeartbeatResponse beatResponse = new HeartbeatResponse(new Heartbeat());
    beatResponse.setMessageID(beat.getMessageID());

    StartTransaction beat2 = new StartTransaction(2, "", 1, "");

    client.pushMessage(beat);
    assert client.size() == 1;
    client.pushMessage(beat2);
    assert client.size() == 2;

    client.popAllMessages();
    assertFalse(client.isEmpty());
    verify(client, times(1)).send(anyString());
    assertTrue(client.isBusy());

    client.handleMessage(beatResponse.toJsonString());
    client.popAllMessages();
    assertTrue(client.isEmpty());

    verify(client, times(2)).send(anyString());
  }

  @Test
  void testRequestSynchronicity2() throws InterruptedException, OCPPMessageFailure {
    doAnswer(invocation -> null).when(client).send(anyString());

    Heartbeat beat = new Heartbeat();
    HeartbeatResponse beatResponse = new HeartbeatResponse(new Heartbeat());
    beatResponse.setMessageID(beat.getMessageID());

    StartTransaction beat2 = new StartTransaction(2, "", 1, "");

    client.pushMessage(beat);
    assert client.size() == 1;
    client.pushMessage(beat2);
    assert client.size() == 2;

    client.popAllMessages();
    assertFalse(client.isEmpty());
    verify(client, times(1)).send(anyString());
    assertTrue(client.isBusy());

    client.clearPreviousMessage(beat);

    assertFalse(client.isBusy());

    client.popAllMessages();
    verify(client, times(2)).send(anyString());
    assertTrue(client.isEmpty());
  }

  @Test
  void testDuplicateAdd() {
    doAnswer(invocation -> null).when(client).send(anyString());
    Heartbeat beat = new Heartbeat();
    assertTrue(client.pushMessage(beat));
    assertFalse(client.isEmpty());

    Heartbeat beat2 = new Heartbeat();
    assertFalse(client.pushMessage(beat2));
  }

  @Test
  void testOfflineFunctionality() throws InterruptedException, OCPPMessageFailure {
    doAnswer(invocation -> null).when(client).send(anyString());

    client.goOffline();

    Heartbeat beat = new Heartbeat();
    HeartbeatResponse beatResponse = new HeartbeatResponse(new Heartbeat());
    beatResponse.setMessageID(beat.getMessageID());
    client.pushMessage(new Heartbeat());
    client.addPreviousMessage(beat);
    client.onMessage(beatResponse.toJsonString());
    client.popAllMessages();

    verify(client, times(0)).send(anyString());
  }

  @Test
  public void testPushPriorityMessageSuccess() {
    // Clear the internal queue for a clean start
    client.queue.getQueue().clear();
    client.queue.getQueueSet().clear();

    // Start the queue with a single message
    client.pushMessage(new BootNotification());

    int initialTxMessages = client.getSentMessages().size();
    Heartbeat heartbeat = new Heartbeat();

    // Push a priority message; should return true
    boolean result = client.pushPriorityMessage(heartbeat);
    assertTrue(result, "Push priority message should return true when message is not in the queue");

    // Verify the message is now at the front of the queue
    assertFalse(client.isEmpty(), "Queue should not be empty after push");
    assertEquals(
        heartbeat,
        client.queue.getQueue().peekFirst(),
        "Priority message should be at the front of the queue");
    assertTrue(
        client.queue.getQueueSet().contains(heartbeat),
        "Queue set should contain the pushed message");

    // Verify that the message was recorded in the transmitted messages list
    assertEquals(
        initialTxMessages + 1,
        client.getSentMessages().size(),
        "A transmitted message should be recorded");
  }

  @Test
  public void testPushPriorityMessageDuplicate() {
    // Clear the internal queue for a clean start
    client.queue.getQueue().clear();
    client.queue.getQueueSet().clear();

    Heartbeat heartbeat = new Heartbeat();

    // First push should succeed
    boolean firstPush = client.pushPriorityMessage(heartbeat);
    assertTrue(firstPush, "First push priority message should succeed");

    // Attempting to push the same message again should fail
    boolean secondPush = client.pushPriorityMessage(heartbeat);
    assertFalse(secondPush, "Duplicate push priority message should return false");

    // The queue should only contain one instance of the message
    assertEquals(
        1,
        client.queue.getQueue().size(),
        "Queue should contain only one message after duplicate push attempt");
  }

  @Test
  public void testCheckTimeouts() throws Exception {
    // Create a Heartbeat message that will be timed out
    Heartbeat heartbeat = new Heartbeat();
    String testMsgId = "timeout-test";
    heartbeat.setMessageID(testMsgId);

    // Get the MessageQueue from our client
    TestOCPPWebSocketClient client =
        new TestOCPPWebSocketClient(new java.net.URI("ws://dummy"), statusNotificationObserver);

    // Access previousMessages
    Field previousMessagesField = MessageQueue.class.getDeclaredField("previousMessages");
    previousMessagesField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, MessageQueue.TimedMessage> previousMessages =
        (Map<String, MessageQueue.TimedMessage>) previousMessagesField.get(client.getQueue());

    // Insert the message with a timestamp older than RESPONSE_TIME_OUT seconds
    MessageQueue.TimedMessage timedMessage =
        new MessageQueue.TimedMessage(
            heartbeat, Instant.now().minus(Duration.ofSeconds(MessageQueue.RESPONSE_TIME_OUT + 5)));
    previousMessages.put(heartbeat.getMessageID(), timedMessage);

    // Register a listener for the complementary message type
    OnOCPPMessageListener listener = mock(OnOCPPMessageListener.class);
    client.onReceiveMessage(HeartbeatResponse.class, listener);

    // Call checkTimeouts, which should trigger onTimeout() for the listener
    client.getQueue().checkTimeouts(client);

    // Verify that the onTimeout method was called once.
    verify(listener, times(1)).onTimeout();

    // Verify that the timed-out message was removed from previousMessages
    assertFalse(
        previousMessages.containsKey(testMsgId),
        "Timed-out message should be removed from previousMessages");
  }

  @Test
  void testInvalidJSONCallError() throws Exception {
    doAnswer(invocation -> null).when(client).send(anyString());
    client.handleMessage("[1,");
    OCPPMessageError error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.FormatViolation, error.getErrorCode());
    assertEquals(
        "java.io.EOFException: End of input at line 1 column 4 path $[1]",
        error.getErrorDescription());

    client.handleMessage("");
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.FormatViolation, error.getErrorCode());
    assertEquals("Provided empty string", error.getErrorDescription());

    assertThrows(
        OCPPBadCallID.class,
        () -> {
          client.handleMessage("[5,\"w\", \"Heartbeat\", {}]");
        });
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.PropertyConstraintViolation, error.getErrorCode());
    assertEquals("Provided bad Call ID", error.getErrorDescription());

    assertThrows(
        OCPPUnsupportedMessage.class, () -> client.handleMessage("[2,\"w\", \"Water\", {}]"));
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.NotSupported, error.getErrorCode());
    assertEquals("Unsupported action", error.getErrorDescription());

    assertThrows(
        OCPPBadMessage.class, () -> client.handleMessage("[2,\"w\", \"Heartbeat\", {}, {}]"));
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.OccurenceConstraintViolation, error.getErrorCode());
    assertEquals("Request provided wrong number of array elements", error.getErrorDescription());

    assertThrows(
        OCPPBadMessage.class, () -> client.handleMessage("[4,\"w\", 2, \"Heartbeat\", {}, {}]"));
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.OccurenceConstraintViolation, error.getErrorCode());
    assertEquals("Error provided wrong number of array elements", error.getErrorDescription());

    assertThrows(
        OCPPBadMessage.class, () -> client.handleMessage("[3,\"w\", \"Heartbeat\", {}, {}]"));
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.OccurenceConstraintViolation, error.getErrorCode());
    assertEquals("Response provided wrong number of array elements", error.getErrorDescription());

    assertThrows(OCPPCannotProcessMessage.class, () -> client.handleMessage("[3,\"w\", {}]"));
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.ProtocolError, error.getErrorCode());
    assertEquals("Received Response with an unknown ID", error.getErrorDescription());

    assertThrows(
        OCPPCannotProcessMessage.class, () -> client.handleMessage("[4,\"w\", 2, \"w\", {}]"));
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.ProtocolError, error.getErrorCode());
    assertEquals("Received Error with an unknown ID", error.getErrorDescription());

    Heartbeat beat = new Heartbeat();
    beat.setMessageID("heartbeat");
    client.addPreviousMessage(beat);
    client.handleMessage("[4,\"heartbeat\", 29999, \"w\", {}]");
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.PropertyConstraintViolation, error.getErrorCode());
    assertEquals("Received Unknown Error Code", error.getErrorDescription());

    client.addPreviousMessage(beat);
    client.handleMessage("[4,\"heartbeat\", -1, \"w\", {}]");
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.PropertyConstraintViolation, error.getErrorCode());
    assertEquals("Received Unknown Error Code", error.getErrorDescription());

    client.addPreviousMessage(beat);
    client.handleMessage("[4,\"heartbeat\", 2, \"w\", a]");
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.PropertyConstraintViolation, error.getErrorCode());
    assertEquals("Error details was not a json object", error.getErrorDescription());

    client.addPreviousMessage(beat);
    client.handleMessage("[3,\"heartbeat\", 2]");
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.PropertyConstraintViolation, error.getErrorCode());
    assertEquals("Response details was not a json object", error.getErrorDescription());

    client.handleMessage("[2,\"heartbeat\", \"Heartbeat\", 2]");
    error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(ErrorCode.PropertyConstraintViolation, error.getErrorCode());
    assertEquals("Request details was not a json object", error.getErrorDescription());
  }

  @Test
  void testWssSchemeSetsSniSSLSocketFactory()
      throws URISyntaxException, NoSuchFieldException, IllegalAccessException {
    // Given a "wss" URI
    URI wssUri = new URI("wss://example.com:12345");

    TestOCPPWebSocketClient client =
        new TestOCPPWebSocketClient(wssUri, statusNotificationObserver);

    // Verify the WebSocketClient's 'socketFactory' is SniSSLSocketFactory
    Field socketFactoryField = getSocketFactoryField();
    SocketFactory actualFactory = (SocketFactory) socketFactoryField.get(client);
    assertNotNull(actualFactory, "Expected socketFactory to be initialized for wss");
    assertTrue(
        actualFactory instanceof SniSSLSocketFactory,
        "Expected a SniSSLSocketFactory when using a wss:// URI");
  }

  @Test
  void testNonWssSchemeDoesNotSetSniSSLSocketFactory()
      throws URISyntaxException, NoSuchFieldException, IllegalAccessException {
    // Given a "ws" URI
    URI wsUri = new URI("ws://example.com:12345");

    TestOCPPWebSocketClient client = new TestOCPPWebSocketClient(wsUri, statusNotificationObserver);

    // Verify the WebSocketClient's 'socketFactory' is not SniSSLSocketFactory
    Field socketFactoryField = getSocketFactoryField();
    SocketFactory actualFactory = (SocketFactory) socketFactoryField.get(client);
    assertNull(actualFactory, "Expected socketFactory to be uninitialized for ws");
  }

  /** Helper to get the `socketFactory` field from the parent WebSocketClient class. */
  private Field getSocketFactoryField() throws NoSuchFieldException {
    Field field = org.java_websocket.client.WebSocketClient.class.getDeclaredField("socketFactory");
    field.setAccessible(true);
    return field;
  }
}
