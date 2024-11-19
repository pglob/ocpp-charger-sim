package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sim_backend.websockets.enums.ErrorCode;
import com.sim_backend.websockets.events.OnOcppMessage;
import com.sim_backend.websockets.events.OnOcppMessageListener;
import com.sim_backend.websockets.exceptions.OcppCannotProcessResponse;
import com.sim_backend.websockets.exceptions.OcppMessageFailure;
import com.sim_backend.websockets.exceptions.OcppUnsupportedMessage;
import com.sim_backend.websockets.messages.HeartBeat;
import com.sim_backend.websockets.messages.HeartBeatResponse;
import com.sim_backend.websockets.types.OcppMessageError;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OcppBadCallIdTest {

  OcppBadCallId client;
  MessageQueue queue;
  OnOcppMessage onOcppMessageMock;

  @BeforeEach
  void setUp() throws URISyntaxException {
    onOcppMessageMock = mock(OnOcppMessage.class);
    client = spy(new OcppBadCallId(new URI("")));
    queue = mock(MessageQueue.class);
  }

  @Test
  public void testPopOnEmptyQueue() throws OcppMessageFailure, InterruptedException {
    assert client.popMessage() == null;
  }

  @Test
  public void testPushThenPop() throws OcppMessageFailure, InterruptedException {
    HeartBeat heartbeat = new HeartBeat();
    doReturn(heartbeat).when(client).popMessage();
    client.pushMessage(heartbeat);

    assert client.popMessage() == heartbeat;
  }

  @Test
  public void testPopMessage() throws OcppMessageFailure, InterruptedException {
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
  void testPopAllMessages() throws OcppMessageFailure, InterruptedException {
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
  public void testBadCallID() throws InterruptedException {
    String badResponseMsg = "[1, \"Cool\", \"HeartBeat\", {}]";
    HeartBeat beat = new HeartBeat();
    doAnswer(
            invocation -> {
              com.sim_backend.websockets.exceptions.OcppBadCallId badResponse =
                  assertThrows(
                      com.sim_backend.websockets.exceptions.OcppBadCallId.class,
                      () -> {
                        client.onMessage(badResponseMsg);
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
  public void testBadCallID2() throws InterruptedException {
    String badResponseMsg = "[5, \"Cool\", \"HeartBeat\", {}]";
    HeartBeat beat = new HeartBeat();
    doAnswer(
            invocation -> {
              com.sim_backend.websockets.exceptions.OcppBadCallId badResponse =
                  assertThrows(
                      com.sim_backend.websockets.exceptions.OcppBadCallId.class,
                      () -> {
                        client.onMessage(badResponseMsg);
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
    OnOcppMessageListener listener = mock(OnOcppMessageListener.class);
    OcppMessageError messageError =
        new OcppMessageError(ErrorCode.FormatViolation, "Not Found", new JsonObject());

    String fullMessage = GsonUtilities.toString(messageError.generateMessage());

    client.onReceiveMessage(
        OcppMessageError.class,
        message -> {
          assert message.getMessage() instanceof OcppMessageError;
          OcppMessageError receivedError = (OcppMessageError) message.getMessage();
          assert receivedError.getErrorCode() == ErrorCode.FormatViolation;
          assert receivedError.getErrorDescription().equals("Not Found");
          assert receivedError.getErrorDetails() != null;
        });
    client.onMessage(fullMessage);

    // verify(listener, times(1)).onMessageReceieved(any(OnOCPPMessage.class));
  }

  @Test
  public void testOnReceiveMulti() {
    OnOcppMessageListener listener = mock(OnOcppMessageListener.class);
    OcppMessageError messageError =
        new OcppMessageError(ErrorCode.FormatViolation, "Not Found", new JsonObject());

    String fullMessage = GsonUtilities.toString(messageError.generateMessage());

    client.onReceiveMessage(
        OcppMessageError.class,
        message -> {
          assert message.getMessage() instanceof OcppMessageError;
          OcppMessageError receivedError = (OcppMessageError) message.getMessage();
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
  public void testOnReceiveMessageNoMatchingMsg() throws OcppMessageFailure, InterruptedException {
    HeartBeatResponse response = new HeartBeatResponse();

    HeartBeat beat = new HeartBeat();
    doAnswer(
            invocation -> {
              response.setMessageId(beat.getMessageId());
              String fullMessage = GsonUtilities.toString(response.generateMessage());
              OcppCannotProcessResponse badResponse =
                  assertThrows(
                      OcppCannotProcessResponse.class,
                      () -> {
                        client.onMessage(fullMessage);
                      });
              assert badResponse.getReceivedMessage().equals(fullMessage);
              assert badResponse.getBadMessageId().equals(beat.getMessageId());

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
  public void testOnReceiveMessage() throws OcppMessageFailure, InterruptedException {
    HeartBeatResponse response = new HeartBeatResponse();

    HeartBeat beat = new HeartBeat();
    doAnswer(
            invocation -> {
              client.addPreviousMessage(beat);
              response.setMessageId(beat.getMessageId());
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
  public void testReceiveBadMessage() throws OcppMessageFailure, InterruptedException {
    doAnswer(
            invocation -> {
              client.onMessage("{}");
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
  public void testOnReceiveBadMessageType() throws OcppMessageFailure, InterruptedException {
    String msgToSend = "[2, \"Woah\", \"AbsoluteTrash\", {}]";
    doAnswer(
            invocation -> {
              client.onMessage(msgToSend);
              return null;
            })
        .when(client)
        .send(anyString());

    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    client.onReceiveMessage(HeartBeatResponse.class, message -> {});
    OcppUnsupportedMessage err =
        assertThrows(OcppUnsupportedMessage.class, () -> client.popAllMessages());
    assert err != null;
    assert err.getMessageName().equals("AbsoluteTrash");
    assert err.getMessage().equals(msgToSend);
  }

  @Test
  public void testAllThrowsException() throws OcppMessageFailure {

    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    assert client.size() == 1;

    OcppMessageFailure exception =
        assertThrows(
            OcppMessageFailure.class,
            () -> {
              client.popAllMessages();
            });

    assert exception != null;
    assert exception.getFailedMessage() == beat;
    assert exception.getFailedMessage().incrementTries() == MessageQueue.MAX_REATTEMPTS + 1;
    assert exception.getInnerException() != null;
  }

  @Test
  public void testThrowsException() throws OcppMessageFailure {

    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    assert client.size() == 1;

    OcppMessageFailure exception =
        assertThrows(
            OcppMessageFailure.class,
            () -> {
              client.popMessage();
            });
    assert exception != null;
    assert exception.getFailedMessage() == beat;
    assert exception.getFailedMessage().incrementTries() == MessageQueue.MAX_REATTEMPTS + 1;
    assert exception.getInnerException() != null;
  }

  @Test
  public void testRetryAfterFirstAttempt() throws OcppMessageFailure, InterruptedException {
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
                        response.setMessageId(beat.getMessageId());
                        client.onMessage(GsonUtilities.toString(response.generateMessage()));
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
    verify(client, times(1)).onMessage(anyString());
  }
}
