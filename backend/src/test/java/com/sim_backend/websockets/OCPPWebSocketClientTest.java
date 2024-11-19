package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sim_backend.websockets.enums.ErrorCode;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.exceptions.*;
import com.sim_backend.websockets.messages.HeartBeat;
import com.sim_backend.websockets.messages.HeartBeatResponse;
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
    HeartBeat heartbeat = new HeartBeat();
    doReturn(heartbeat).when(client).popMessage();
    client.pushMessage(heartbeat);

    assert client.popMessage() == heartbeat;
  }

  @Test
  public void testPopMessage() throws OCPPMessageFailure, InterruptedException {
    Pattern pattern = Pattern.compile("^\\[2,\\s*\".*?\",\\s*\"HeartBeat\",\\s*\\{}]$");
    doAnswer(
            invocation -> {
              assert pattern.matcher(invocation.getArgument(0, String.class)).find();
              return null;
            })
        .when(client)
        .send(anyString());

    HeartBeat beat = new HeartBeat();
    client.pushMessage(beat);
    client.popMessage();
    verify(client, times(1)).send(anyString());
  }

  @Test
  void testPopAllMessages() throws OCPPMessageFailure, InterruptedException {
    Pattern pattern = Pattern.compile("^\\[2,\\s*\".*?\",\\s*\"HeartBeat\",\\s*\\{}]$");
    doAnswer(
            invocation -> {
              assert pattern.matcher(invocation.getArgument(0, String.class)).find();
              return null;
            })
        .when(client)
        .send(anyString());

    HeartBeat beat = new HeartBeat();
    HeartBeat beat2 = new HeartBeat();

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
    String badResponseMsg = "[1, \"Cool\", \"HeartBeat\", {}]";
    HeartBeat beat = new HeartBeat();
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
    client.onReceiveMessage(HeartBeatResponse.class, message -> {});

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
  }

  @Test
  public void testBadCallID2() throws InterruptedException, OCPPMessageFailure {
    String badResponseMsg = "[5, \"Cool\", \"HeartBeat\", {}]";
    HeartBeat beat = new HeartBeat();
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
    client.onReceiveMessage(HeartBeatResponse.class, message -> {});

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
  }

  @Test
  public void testOnReceiveError() {
    OnOCPPMessageListener listener = mock(OnOCPPMessageListener.class);
    OCPPMessageError messageError =
        new OCPPMessageError(ErrorCode.FormatViolation, "Not Found", new JsonObject());

    String fullMessage = GsonUtilities.toString(messageError.generateMessage());

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

    String fullMessage = GsonUtilities.toString(messageError.generateMessage());

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
        HeartBeat.class,
        message -> {
          assert message.getMessage() instanceof HeartBeat;
        });
    client.onMessage(fullMessage);
    client.onMessage(GsonUtilities.toString(new HeartBeat().generateMessage()));

    // verify(listener, times(1)).onMessageReceieved(any(OnOCPPMessage.class));
  }

  @Test
  public void testOnReceiveMessageNoMatchingMsg() throws OCPPMessageFailure, InterruptedException {
    HeartBeatResponse response = new HeartBeatResponse();

    HeartBeat beat = new HeartBeat();
    doAnswer(
            invocation -> {
              response.setMessageID(beat.getMessageID());
              String fullMessage = GsonUtilities.toString(response.generateMessage());
              OCPPCannotProcessResponse badResponse =
                  assertThrows(
                      OCPPCannotProcessResponse.class,
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
    client.onReceiveMessage(HeartBeatResponse.class, message -> {});

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
  }

  @Test
  public void testOnReceiveMessage() throws OCPPMessageFailure, InterruptedException {
    HeartBeatResponse response = new HeartBeatResponse();

    HeartBeat beat = new HeartBeat();
    doAnswer(
            invocation -> {
              client.addPreviousMessage(beat);
              response.setMessageID(beat.getMessageID());
              client.onMessage(GsonUtilities.toString(response.generateMessage()));
              return null;
            })
        .when(client)
        .send(anyString());

    client.pushMessage(beat);
    client.onReceiveMessage(
        HeartBeatResponse.class,
        message -> {
          assert message.getMessage() instanceof HeartBeatResponse;
          HeartBeatResponse receivedResponse = (HeartBeatResponse) message.getMessage();
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

    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    client.onReceiveMessage(HeartBeatResponse.class, message -> {});
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

    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    client.onReceiveMessage(HeartBeatResponse.class, message -> {});
    OCPPUnsupportedMessage err =
        assertThrows(OCPPUnsupportedMessage.class, () -> client.popAllMessages());
    assert err != null;
    assert err.getMessageName().equals("AbsoluteTrash");
    assert err.getFullMessage().equals(msgToSend);
  }

  @Test
  public void testAllThrowsException() throws OCPPMessageFailure {

    HeartBeat beat = new HeartBeat();

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

    HeartBeat beat = new HeartBeat();

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
    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    assert client.size() == 1;

    client.onReceiveMessage(
        HeartBeatResponse.class,
        message -> {
          assert message.getMessage() instanceof HeartBeatResponse;
        });

    doAnswer(
            invocation -> {
              doAnswer(
                      invocation2 -> {
                        this.client.addPreviousMessage(beat);
                        HeartBeatResponse response = new HeartBeatResponse();
                        response.setMessageID(beat.getMessageID());
                        client.handleMessage(GsonUtilities.toString(response.generateMessage()));
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
}
