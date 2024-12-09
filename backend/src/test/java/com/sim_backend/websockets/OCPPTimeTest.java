package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.HeartbeatResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OCPPTimeTest {
  public static final ZoneId UTC = ZoneId.of("UTC");

  OCPPWebSocketClient client;
  MessageQueue queue;
  OnOCPPMessage onOCPPMessageMock;
  OCPPTime ocppTime;

  @BeforeEach
  void setUp() throws URISyntaxException {
    onOCPPMessageMock = mock(OnOCPPMessage.class);
    client = spy(new OCPPWebSocketClient(new URI("")));
    ocppTime = new OCPPTime(client);
  }

  private ZonedDateTime getTestTime() {
    return ZonedDateTime.of(2024, 2, 20, 8, 20, 0, 0, ZoneId.of("UTC"));
  }

  @Test
  public void testOCPPTime() throws InterruptedException, OCPPMessageFailure {
    try (OCPPTime time = new OCPPTime(client)) {
      HeartbeatResponse response = new HeartbeatResponse(ZonedDateTime.now().minusSeconds(24));

      Heartbeat beat = new Heartbeat();
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
      HeartbeatResponse response = new HeartbeatResponse(ZonedDateTime.now().minusSeconds(45));

      Heartbeat beat = new Heartbeat();
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

  @Test
  void testTimeSynchronization() {
    // Arrange
    HeartbeatResponse heartbeatResponse = mock(HeartbeatResponse.class);
    ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC")); // Ensure UTC time
    when(heartbeatResponse.getCurrentTime()).thenReturn(currentTime);

    // Simulate receiving a heartbeat response
    ocppTime.listener.onMessageReceived(new OnOCPPMessage(heartbeatResponse, client));

    // Act
    ZonedDateTime synchronizedTime = ocppTime.getSynchronizedTime();

    // Assert
    Duration difference = Duration.between(currentTime, synchronizedTime);
    assertTrue(
        difference.abs().toMillis() < 100,
        "Synchronized time after first heartbeat should match heartbeat time.");
  }

  @Test
  void testOffsetUpdateOnNewHeartbeat() {

    ZonedDateTime currentTime1 = ZonedDateTime.now(UTC);
    ZonedDateTime currentTime2 = currentTime1.plusMinutes(2);

    HeartbeatResponse heartbeatResponse1 = mock(HeartbeatResponse.class);
    when(heartbeatResponse1.getCurrentTime()).thenReturn(currentTime1);
    ocppTime.listener.onMessageReceived(new OnOCPPMessage(heartbeatResponse1, client));

    ZonedDateTime synchronizedTime1 = ocppTime.getSynchronizedTime();
    Duration difference = Duration.between(currentTime1, synchronizedTime1);
    assertTrue(
        difference.abs().toMillis() < 100,
        "Synchronized time after first heartbeat should match heartbeat time.");

    HeartbeatResponse heartbeatResponse2 = mock(HeartbeatResponse.class);
    when(heartbeatResponse2.getCurrentTime()).thenReturn(currentTime2);
    ocppTime.listener.onMessageReceived(new OnOCPPMessage(heartbeatResponse2, client));

    ZonedDateTime synchronizedTime2 = ocppTime.getSynchronizedTime();
    difference = Duration.between(currentTime2, synchronizedTime2);
    assertTrue(
        difference.abs().toMillis() < 100,
        "Synchronized time after first heartbeat should match heartbeat time.");
  }

  @Test
  void testTimeSynchronizationWithOffset() {
    ZonedDateTime currentTime = ZonedDateTime.now(UTC); // Ensure UTC time
    ZonedDateTime heartbeatTime = currentTime.minusSeconds(30); // 30 seconds behind
    HeartbeatResponse heartbeatResponse = mock(HeartbeatResponse.class);
    when(heartbeatResponse.getCurrentTime()).thenReturn(heartbeatTime);

    ocppTime.listener.onMessageReceived(new OnOCPPMessage(heartbeatResponse, client));

    ZonedDateTime synchronizedTime = ocppTime.getSynchronizedTime(currentTime);

    Duration difference = Duration.between(synchronizedTime, currentTime.minusSeconds(30));
    long diff = difference.abs().toMillis();
    assertTrue(diff < 100, "Synchronized time should be adjusted by approximately 30 seconds.");
  }

  @Test
  void testCloseCleansUpListener() throws Exception {
    // Arrange
    OCPPTime spyOcppTime = spy(ocppTime);

    // Act
    spyOcppTime.close();

    // Assert
    verify(client, times(1)).deleteOnReceiveMessage(HeartbeatResponse.class, spyOcppTime.listener);
  }

  @Test
  void testInvalidClientThrowsException() {
    // Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> new OCPPTime(null),
        "Should throw IllegalArgumentException when client is null.");
  }
}
