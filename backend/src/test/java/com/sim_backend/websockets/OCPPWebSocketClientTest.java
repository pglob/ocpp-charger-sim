package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.google.gson.JsonParseException;
import com.sim_backend.exceptions.OCPPMessageFailureException;
import com.sim_backend.exceptions.OCPPUnsupportedMessageException;
import com.sim_backend.utils.GsonUtilities;
import com.sim_backend.websockets.client.OCPPWebSocketClient;
import com.sim_backend.websockets.messages.HeartBeat;
import com.sim_backend.websockets.messages.HeartBeatResponse;
import java.net.URI;
import java.net.URISyntaxException;
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
  public void testPopOnEmptyQueue() throws OCPPMessageFailureException, InterruptedException {
    assert client.popMessage() == null;
  }

  @Test
  public void testPushThenPop() throws OCPPMessageFailureException, InterruptedException {
    HeartBeat heartbeat = new HeartBeat();
    doReturn(heartbeat).when(client).popMessage();
    client.pushMessage(heartbeat);

    assert client.popMessage() == heartbeat;
  }

  @Test
  public void testPopMessage() throws OCPPMessageFailureException, InterruptedException {
    doAnswer(
            invocation -> {
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
  void testPopAllMessages() throws OCPPMessageFailureException, InterruptedException {
    doAnswer(
            invocation -> {
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
  public void testOnReceiveMessage() throws OCPPMessageFailureException, InterruptedException {
    HeartBeatResponse response = new HeartBeatResponse();
    doAnswer(
            invocation -> {
              client.onMessage(GsonUtilities.toString(response.generateMessage()));
              System.out.println(GsonUtilities.toString(response.generateMessage()));
              return null;
            })
        .when(client)
        .send(anyString());

    HeartBeat beat = new HeartBeat();

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
  public void testReceiveBadMessageNoReceiver()
      throws OCPPMessageFailureException, InterruptedException {
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
  public void testReceiveBadMessage() throws OCPPMessageFailureException, InterruptedException {
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
  }

  @Test
  public void testOnReceiveBadMessageType()
      throws OCPPMessageFailureException, InterruptedException {
    String msgToSend =
        "{\"messageCallId\" : \"2\", \"messageType\" : \"AbsoluteTrash\", \"messageName\" : \"Woah\", \"body\" : {}}";
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
    OCPPUnsupportedMessageException err =
        assertThrows(OCPPUnsupportedMessageException.class, () -> client.popAllMessages());
    assert err != null;
    assert err.getMessageType().equals("AbsoluteTrash");
    assert err.getMessage().equals(msgToSend);
  }

  @Test
  public void testAllThrowsException() throws OCPPMessageFailureException {

    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    assert client.size() == 1;

    OCPPMessageFailureException exception =
        assertThrows(
            OCPPMessageFailureException.class,
            () -> {
              client.popAllMessages();
            });

    assert exception != null;
    assert exception.getFailedMessage() == beat;
    assert exception.getFailedMessage().incrementTries() == MessageQueue.MAX_REATTEMPTS + 1;
    assert exception.getInnerException() != null;
  }

  @Test
  public void testThrowsException() throws OCPPMessageFailureException {

    HeartBeat beat = new HeartBeat();

    client.pushMessage(beat);
    assert client.size() == 1;

    OCPPMessageFailureException exception =
        assertThrows(
            OCPPMessageFailureException.class,
            () -> {
              client.popMessage();
            });
    assert exception != null;
    assert exception.getFailedMessage() == beat;
    assert exception.getFailedMessage().incrementTries() == MessageQueue.MAX_REATTEMPTS + 1;
    assert exception.getInnerException() != null;
  }

  @Test
  public void testRetryAfterFirstAttempt()
      throws OCPPMessageFailureException, InterruptedException {
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
                        HeartBeatResponse response = new HeartBeatResponse();
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
