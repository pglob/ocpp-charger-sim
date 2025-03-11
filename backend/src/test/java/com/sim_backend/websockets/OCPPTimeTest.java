package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sim_backend.websockets.OCPPWebSocketClientTest.TestOCPPWebSocketClient;
import com.sim_backend.websockets.enums.ErrorCode;
import com.sim_backend.websockets.events.OnOCPPMessage;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.exceptions.OCPPMessageFailure;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.HeartbeatResponse;
import com.sim_backend.websockets.observers.StatusNotificationObserver;
import com.sim_backend.websockets.types.OCPPMessageError;
import com.sim_backend.websockets.types.OCPPRepeatingTimedTask;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OCPPTimeTest {
  public static final ZoneId UTC = ZoneId.of("UTC");

  TestOCPPWebSocketClient client;
  MessageQueue queue;
  OnOCPPMessage onOCPPMessageMock;
  StatusNotificationObserver statusNotificationObserver;
  OCPPTime ocppTime;

  @BeforeEach
  void setUp() throws URISyntaxException {
    onOCPPMessageMock = mock(OnOCPPMessage.class);
    client = spy(new TestOCPPWebSocketClient(new URI(""), statusNotificationObserver));
    ocppTime = client.getScheduler().getTime();
  }

  private ZonedDateTime getTestTime() {
    return ZonedDateTime.of(2024, 2, 20, 8, 20, 0, 0, ZoneId.of("UTC"));
  }

  @Test
  public void testOCPPTime() throws InterruptedException, OCPPMessageFailure {
    doNothing().when(client).send(anyString());
    HeartbeatResponse response =
        new HeartbeatResponse(new Heartbeat(), ZonedDateTime.now().minusSeconds(24));

    ocppTime.setHeartbeatInterval(20L, TimeUnit.SECONDS);
    ocppTime.heartbeat.task.run();
    response.setMessageID(ocppTime.heartbeat.getMessage().getMessageID());
    client.popAllMessages();

    client.onMessage(response.toJsonString());

    Duration duration = Duration.between(ocppTime.getSynchronizedTime(), ZonedDateTime.now());
    assert duration.getSeconds() == 24;
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
      client.popMessage();
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
    when(heartbeatResponse.getMessageID()).thenReturn("abc");
    ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC")); // Ensure UTC time
    when(heartbeatResponse.getCurrentTime()).thenReturn(currentTime);

    this.ocppTime.heartbeats.add(heartbeatResponse.getMessageID());
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
    ocppTime.setHeartbeatInterval(20L, TimeUnit.SECONDS);

    HeartbeatResponse heartbeatResponse1 = mock(HeartbeatResponse.class);
    when(heartbeatResponse1.getCurrentTime()).thenReturn(currentTime1);
    when(heartbeatResponse1.getMessageID())
        .thenReturn(ocppTime.heartbeat.getMessage().getMessageID());
    this.client.getScheduler().getTime().heartbeats.add(heartbeatResponse1.getMessageID());
    ocppTime.listener.onMessageReceived(new OnOCPPMessage(heartbeatResponse1, client));

    ZonedDateTime synchronizedTime1 = ocppTime.getSynchronizedTime();
    Duration difference = Duration.between(currentTime1, synchronizedTime1);
    assertTrue(
        difference.abs().toMillis() < 100,
        "Synchronized time after first heartbeat should match heartbeat time.");

    HeartbeatResponse heartbeatResponse2 = mock(HeartbeatResponse.class);
    when(heartbeatResponse2.getCurrentTime()).thenReturn(currentTime2);
    when(heartbeatResponse2.getMessageID())
        .thenReturn(ocppTime.heartbeat.getMessage().getMessageID());
    ocppTime.heartbeats.add(heartbeatResponse2.getMessageID());
    ocppTime.lastHeartbeat = heartbeatResponse2.getMessageID();

    ocppTime.listener.onMessageReceived(new OnOCPPMessage(heartbeatResponse2, client));

    ZonedDateTime synchronizedTime2 = ocppTime.getSynchronizedTime();
    difference = Duration.between(currentTime2, synchronizedTime2);
    assertTrue(
        difference.abs().toMillis() < 100,
        "Synchronized time after first heartbeat should match heartbeat time.");
  }

  @Test
  void testTimeSynchronizationWithOffset() {

    ocppTime.setHeartbeatInterval(20L, TimeUnit.SECONDS);
    ZonedDateTime currentTime = ZonedDateTime.now(UTC); // Ensure UTC time
    ZonedDateTime heartbeatTime = currentTime.minusSeconds(30); // 30 seconds behind
    HeartbeatResponse heartbeatResponse = mock(HeartbeatResponse.class);
    when(heartbeatResponse.getMessageID()).thenReturn("abc");
    when(heartbeatResponse.getCurrentTime()).thenReturn(heartbeatTime);
    when(heartbeatResponse.getMessageID())
        .thenReturn(ocppTime.heartbeat.getMessage().getMessageID());

    this.ocppTime.heartbeats.add(heartbeatResponse.getMessageID());
    this.ocppTime.lastHeartbeat = heartbeatResponse.getMessageID();
    ocppTime.listener.onMessageReceived(new OnOCPPMessage(heartbeatResponse, client));

    ZonedDateTime synchronizedTime = ocppTime.getSynchronizedTime(currentTime);

    Duration difference = Duration.between(synchronizedTime, currentTime.minusSeconds(30));
    long diff = difference.abs().toMillis();
    assertTrue(diff < 100, "Synchronized time should be adjusted by approximately 30 seconds.");
  }

  @Test
  void testInvalidClientThrowsException() {
    // Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> new OCPPTime(null),
        "Should throw IllegalArgumentException when client is null.");
  }

  @Test
  void testHeartBeatResponseCSCAP142() throws InterruptedException, OCPPMessageFailure {
    doNothing().when(client).send(anyString());
    OCPPTime spyTime = spy(client.getScheduler().getTime());

    HeartbeatResponse heartbeatResponse =
        new HeartbeatResponse(new Heartbeat(), ZonedDateTime.now());

    OCPPRepeatingTimedTask heartbeat = ocppTime.setHeartbeatInterval(20L, TimeUnit.SECONDS);
    heartbeatResponse.setMessageID(heartbeat.getMessage().getMessageID());

    client.addPreviousMessage(heartbeat.getMessage());

    heartbeat.setMessage(heartbeat.getMessage().cloneMessage());
    spyTime.listener.onMessageReceived(new OnOCPPMessage(heartbeatResponse, client));

    OCPPMessageError error = (OCPPMessageError) client.popMessage();
    assertNotNull(error);
    assertEquals(error.getErrorCode(), ErrorCode.ProtocolError);
  }

  @Test
  public void testOCPPLatestTime() throws InterruptedException, OCPPMessageFailure {
    doNothing().when(client).send(anyString());
    Heartbeat beat = new Heartbeat();
    HeartbeatResponse response = new HeartbeatResponse(beat, ZonedDateTime.now().minusSeconds(24));
    client.addPreviousMessage(beat);

    ocppTime.setOffset(ZonedDateTime.now().minusSeconds(20));

    ocppTime.lastHeartbeat = "abc";
    ocppTime.heartbeats.add(response.getMessageID());

    client.onMessage(response.toJsonString());

    Duration duration = Duration.between(ocppTime.getSynchronizedTime(), ZonedDateTime.now());
    assertEquals(20, duration.getSeconds());
  }
}
