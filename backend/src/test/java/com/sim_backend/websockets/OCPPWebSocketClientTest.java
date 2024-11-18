package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.google.gson.JsonParseException;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.exceptions.OCPPBadCallID;
import com.sim_backend.websockets.exceptions.OCPPCannotProcessResponse;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;
import com.sim_backend.websockets.exceptions.OCPPUnsupportedMessage;
import com.sim_backend.websockets.messages.HeartBeat;
import com.sim_backend.websockets.messages.HeartBeatResponse;
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
  public void testBadCallID() throws InterruptedException {
    String badResponseMsg = "[1, \"Cool\", \"HeartBeat\", {}]";
    HeartBeat beat = new HeartBeat();
    doAnswer(
            invocation -> {
              OCPPBadCallID badResponse =
                  assertThrows(
                      OCPPBadCallID.class,
                      () -> {
                        client.onMessage(badResponseMsg);
                      });
              assert badResponse.getFullMessage().equals(badResponseMsg);
              assert badResponse.getBadCallID() == 1;
              return null;
            })
        .when(client)
        .send(anyString());

    client.pushMessage(beat);
    client.setOnReceiveMessage(message -> {});

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
  }

  @Test
  public void testBadCallID2() throws InterruptedException {
    String badResponseMsg = "[4, \"Cool\", \"HeartBeat\", {}]";
    HeartBeat beat = new HeartBeat();
    doAnswer(
            invocation -> {
              OCPPBadCallID badResponse =
                  assertThrows(
                      OCPPBadCallID.class,
                      () -> {
                        client.onMessage(badResponseMsg);
                      });
              assert badResponse.getFullMessage().equals(badResponseMsg);
              assert badResponse.getBadCallID() == 4;
              return null;
            })
        .when(client)
        .send(anyString());

    client.pushMessage(beat);
    client.setOnReceiveMessage(message -> {});

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
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
                        client.onMessage(fullMessage);
                      });
              assert badResponse.getReceivedMessage().equals(fullMessage);
              assert badResponse.getBadMessageId().equals(beat.getMessageID());

              return null;
            })
        .when(client)
        .send(anyString());

    client.pushMessage(beat);
    client.setOnReceiveMessage(
        message -> {
          assert message.getMessage() instanceof HeartBeatResponse;
          HeartBeatResponse receivedResponse = (HeartBeatResponse) message.getMessage();
          assert receivedResponse.getCurrentTime().isEqual(response.getCurrentTime());
        });

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
  }

  @Test
  public void testOnReceiveMessage() throws OCPPMessageFailure, InterruptedException {
    HeartBeatResponse response = new HeartBeatResponse();

    HeartBeat beat = new HeartBeat();
    doAnswer(
            invocation -> {
              client.addMessageToPreviousMessage(beat);
              response.setMessageID(beat.getMessageID());
              client.onMessage(GsonUtilities.toString(response.generateMessage()));
              return null;
            })
        .when(client)
        .send(anyString());

    client.pushMessage(beat);
    client.setOnReceiveMessage(
        message -> {
          assert message.getMessage() instanceof HeartBeatResponse;
          HeartBeatResponse receivedResponse = (HeartBeatResponse) message.getMessage();
          assert receivedResponse.getCurrentTime().isEqual(response.getCurrentTime());
        });

    client.popAllMessages();

    // verify(onOCPPMessageMock, times(1)).getMessage();
  }

  @Test
  public void testReceiveBadMessageNoReceiver() throws OCPPMessageFailure, InterruptedException {
    doAnswer(
            invocation -> {
              client.onMessage("{}");
              return null;
            })
        .when(client)
        .send(anyString());

    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    client.popAllMessages();
    verify(client, times(1)).onMessage(anyString());
  }

  @Test
  public void testReceiveBadMessage() throws OCPPMessageFailure, InterruptedException {
    doAnswer(
            invocation -> {
              client.onMessage("{}");
              return null;
            })
        .when(client)
        .send(anyString());

    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    client.setOnReceiveMessage(message -> {});
    JsonParseException err = assertThrows(JsonParseException.class, () -> client.popAllMessages());
    assert err != null;
    assert err.getMessage().startsWith("Expected array");
  }

  @Test
  public void testOnReceiveBadMessageType() throws OCPPMessageFailure, InterruptedException {
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
    client.setOnReceiveMessage(message -> {});
    OCPPUnsupportedMessage err =
        assertThrows(OCPPUnsupportedMessage.class, () -> client.popAllMessages());
    assert err != null;
    assert err.getMessageName().equals("AbsoluteTrash");
    assert err.getMessage().equals(msgToSend);
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
  public void testRetryAfterFirstAttempt() throws OCPPMessageFailure, InterruptedException {
    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    assert client.size() == 1;

    client.setOnReceiveMessage(
        message -> {
          assert message.getMessage() instanceof HeartBeatResponse;
        });

    doAnswer(
            invocation -> {
              doAnswer(
                      invocation2 -> {
                        this.client.addMessageToPreviousMessage(beat);
                        HeartBeatResponse response = new HeartBeatResponse();
                        response.setMessageID(beat.getMessageID());
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
