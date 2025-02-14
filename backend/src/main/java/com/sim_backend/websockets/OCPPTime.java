package com.sim_backend.websockets;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.sim_backend.websockets.enums.ErrorCode;
import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.Heartbeat;
import com.sim_backend.websockets.messages.HeartbeatResponse;
import com.sim_backend.websockets.types.OCPPMessageError;
import com.sim_backend.websockets.types.OCPPRepeatingTimedTask;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** Get Synchronized Time with our last received HeartbeatResponse. */
@Slf4j
public class OCPPTime implements AutoCloseable {
  /** UTC ZoneID */
  public static final ZoneId UTC = ZoneId.of("UTC");

  /** Our stored offset based on the time we received in our last HeartbeatResponse. */
  private final AtomicReference<Duration> offset = new AtomicReference<>(Duration.ZERO);

  /** Our heartbeat job. */
  private OCPPRepeatingTimedTask heartbeat;

  /**
   * Our set heartbeat interval, we don't want this too common but every 4 minutes should be enough.
   */
  @Getter private static final long HEARTBEAT_INTERVAL = 240L; // seconds

  /** The client we are listening on. */
  private OCPPWebSocketClient client = null;

  /** Our stored OCPPMessageListener. */
  @VisibleForTesting
  final OnOCPPMessageListener listener =
      message -> {
        HeartbeatResponse response = (HeartbeatResponse) message.getMessage();
        if (heartbeat != null
            && !heartbeat.message.getMessageID().equals(response.getMessageID())) {
          log.error(String.format("Received old message ID %s", response.getMessageID()));
          client.pushMessage(
              new OCPPMessageError(
                  ErrorCode.ProtocolError,
                  "Received HeartbeatResponse with an old message ID",
                  new JsonObject()));
          return;
        }
        setOffset(response.getCurrentTime());
      };

  /**
   * Create an OCPP Synchronized Time Object.
   *
   * @param currClient The client you want to listen for time from.
   */
  public OCPPTime(OCPPWebSocketClient currClient) {
    if (currClient == null) {
      throw new IllegalArgumentException("Client cannot be null");
    }

    this.client = currClient;
    this.client.onReceiveMessage(HeartbeatResponse.class, listener);
  }

  /**
   * Get the current time synchronized with our last received HeartbeatResponse.
   *
   * @return The Synchronized time.
   */
  public ZonedDateTime getSynchronizedTime() {
    return this.getSynchronizedTime(ZonedDateTime.now(UTC));
  }

  /**
   * Get a Time synchronized with our last received HeartbeatResponse.
   *
   * @param time the time you wish to sync up.
   * @return The Synchronized time.
   */
  public ZonedDateTime getSynchronizedTime(ZonedDateTime time) {
    return time.plus(offset.get());
  }

  /**
   * Synchronizes the clock to match the Central System's current time. Used when a
   * BootNotificationResponse returns an Accepted status.
   *
   * @param time the time you wish to sync up.
   */
  public void setOffset(ZonedDateTime time) {
    Duration calculatedOffset = Duration.between(ZonedDateTime.now(UTC), time);
    offset.set(calculatedOffset);
  }

  /**
   * Called when this is destroyed in a try catch.
   *
   * @throws Exception error in removing the receiver.
   */
  @Override
  public void close() throws Exception {
    if (this.client != null) {
      this.client.deleteOnReceiveMessage(HeartbeatResponse.class, listener);
    } else {
      log.warn("Client is null, cannot deregister message listener.");
    }
  }

  /**
   * Set our interval between heartbeats.
   *
   * @param interval The Duration between heartbeats.
   * @param unit The unit of your interval.
   */
  public OCPPRepeatingTimedTask setHeartbeatInterval(Long interval, TimeUnit unit) {
    if (this.heartbeat != null) {
      this.client.getScheduler().killJob(this.heartbeat);
    }

    this.heartbeat = this.client.getScheduler().periodicJob(0, interval, unit, new Heartbeat());
    return this.heartbeat;
  }
}
