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
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.HeartbeatResponse;
import com.sim_backend.websockets.types.OCPPMessageError;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OCPPWebSocketClientTest {

  OCPPWebSocketClient client;
  MessageQueue queue;
  OnOCPPMessage onOCPPMessageMock;

  @BeforeEach
  void setUp() throws URISyntaxException {
    onOCPPMessageMock = mock(OnOCPPMessage.class);
    client = spy(new OCPPWebSocketClient(new URI("")));
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
    HeartbeatResponse beat2 = new HeartbeatResponse();

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
    HeartbeatResponse response = new HeartbeatResponse();

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
    HeartbeatResponse response = new HeartbeatResponse();

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
  public void testAllThrowsException() throws OCPPMessageFailure {

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
  public void testThrowsException() throws OCPPMessageFailure {

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
                        HeartbeatResponse response = new HeartbeatResponse();
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
    HeartbeatResponse beatResponse = new HeartbeatResponse();
    beatResponse.setMessageID(beat.getMessageID());

    Heartbeat beat2 = new Heartbeat();

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
    HeartbeatResponse beatResponse = new HeartbeatResponse();
    beatResponse.setMessageID(beat.getMessageID());

    Heartbeat beat2 = new Heartbeat();

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
  void testOfflineFunctionality() throws InterruptedException, OCPPMessageFailure {
    client.goOffline();

    Heartbeat beat = new Heartbeat();
    HeartbeatResponse beatResponse = new HeartbeatResponse();
    beatResponse.setMessageID(beat.getMessageID());
    client.pushMessage(new Heartbeat());
    client.addPreviousMessage(beat);
    client.onMessage(beatResponse.toJsonString());
    client.popAllMessages();

    verify(client, times(0)).send(anyString());

    OnOCPPMessageListener a = mock(OnOCPPMessageListener.class);
    client.onReceiveMessage(HeartbeatResponse.class, a);

    client.goOnline();
    verify(a, times(1)).onMessageReceived(any());
  }
}
