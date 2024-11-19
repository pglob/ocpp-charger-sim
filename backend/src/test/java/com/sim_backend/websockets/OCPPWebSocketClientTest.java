package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.google.gson.JsonParseException;
import com.sim_backend.websockets.messages.HeartBeatMessage;
import com.sim_backend.websockets.messages.HeartBeatResponseMessage;
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
    HeartBeatMessage heartbeat = new HeartBeatMessage();
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

    HeartBeatMessage beat = new HeartBeatMessage();
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

    HeartBeatMessage beat = new HeartBeatMessage();
    HeartBeatMessage beat2 = new HeartBeatMessage();

    client.pushMessage(beat);
    assert client.size() == 1;
    client.pushMessage(beat2);
    assert client.size() == 2;

    client.popAllMessages();
    assert client.isEmpty();

    verify(client, times(2)).send(anyString());
  }

  @Test
  public void testOnReceiveMessage() throws OCPPMessageFailure, InterruptedException {
    HeartBeatResponseMessage response = new HeartBeatResponseMessage();
    doAnswer(
            invocation -> {
              client.onMessage(GsonUtilities.toString(response.generateMessage()));
              return null;
            })
        .when(client)
        .send(anyString());

    HeartBeatMessage beat = new HeartBeatMessage();

    client.pushMessage(beat);
    client.setOnReceiveMessage(
        message -> {
          assert message.getMessage() instanceof HeartBeatResponseMessage;
          HeartBeatResponseMessage receivedResponse =
              (HeartBeatResponseMessage) message.getMessage();
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

    HeartBeatMessage beat = new HeartBeatMessage();

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

    HeartBeatMessage beat = new HeartBeatMessage();

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

    HeartBeatMessage beat = new HeartBeatMessage();

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

    HeartBeatMessage beat = new HeartBeatMessage();

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

    HeartBeatMessage beat = new HeartBeatMessage();

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
    HeartBeatMessage beat = new HeartBeatMessage();

    client.pushMessage(beat);
    assert client.size() == 1;

    client.setOnReceiveMessage(
        message -> {
          assert message.getMessage() instanceof HeartBeatResponseMessage;
        });

    doAnswer(
            invocation -> {
              doAnswer(
                      invocation2 -> {
                        HeartBeatResponseMessage response = new HeartBeatResponseMessage();
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
