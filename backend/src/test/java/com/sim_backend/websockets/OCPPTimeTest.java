package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;
import com.sim_backend.websockets.messages.HeartBeat;
import com.sim_backend.websockets.messages.HeartBeatResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OCPPTimeTest {
  OCPPWebSocketClient client;
  MessageQueue queue;
  OnOCPPMessage onOCPPMessageMock;

  @BeforeEach
  void setUp() throws URISyntaxException {
    onOCPPMessageMock = mock(OnOCPPMessage.class);
    client = spy(new OCPPWebSocketClient(new URI("")));
  }

  private ZonedDateTime getTestTime() {
    return ZonedDateTime.of(2024, 2, 20, 8, 20, 0, 0, ZoneId.of("UTC"));
  }

  @Test
  public void testOCPPTime() throws InterruptedException, OCPPMessageFailure {
    try (OCPPTime time = new OCPPTime(client)) {
      HeartBeatResponse response = new HeartBeatResponse(ZonedDateTime.now().minusSeconds(24));

      HeartBeat beat = new HeartBeat();
      client.addPreviousMessage(beat);
      response.setMessageID(beat.getMessageID());
      client.onMessage(response.toJsonString());

      Duration duration = Duration.between(time.getSynchronizedTime(), ZonedDateTime.now());
      assert duration.getSeconds() == 24;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    verify(client, times(1))
        .deleteOnReceiveMessage(any(Class.class), any(OnOCPPMessageListener.class));
  }

  @Test
  public void testOCPPTime2() throws InterruptedException, OCPPMessageFailure {
    IllegalArgumentException illegalArgumentException =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              try (OCPPTime time = new OCPPTime(null)) {
                client.popAllMessages();
              }
            });
  }

  @Test
  public void testOCPPTime3() throws InterruptedException, OCPPMessageFailure {
    try (OCPPTime time = new OCPPTime(client)) {
      HeartBeatResponse response = new HeartBeatResponse(ZonedDateTime.now().minusSeconds(45));

      HeartBeat beat = new HeartBeat();
      client.addPreviousMessage(beat);
      response.setMessageID(beat.getMessageID());
      client.onMessage(response.toJsonString());

      Duration duration = Duration.between(time.getSynchronizedTime(), ZonedDateTime.now());
      assert duration.getSeconds() == 45;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    verify(client, times(1))
        .deleteOnReceiveMessage(any(Class.class), any(OnOCPPMessageListener.class));
  }
}
