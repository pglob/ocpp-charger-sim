package com.sim_backend.websockets;

import com.sim_backend.websockets.events.OnOCPPMessageListener;
import com.sim_backend.websockets.messages.HeartbeatResponse;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

/** Get Synchronized Time with our last received HeartbeatResponse. */
@Slf4j
public class OCPPTime implements AutoCloseable {
  /** UTC ZoneID */
  public static final ZoneId UTC = ZoneId.of("UTC");

  /** Our stored offset based on the time we received in our last HeartbeatResponse. */
  private final AtomicReference<Duration> offset = new AtomicReference<>(Duration.ZERO);

  /** Our stored OCPPMessageListener. */
  private final OnOCPPMessageListener listener =
      message -> {
        HeartbeatResponse response = (HeartbeatResponse) message.getMessage();

        offset.set(Duration.between(ZonedDateTime.now(UTC), response.getCurrentTime()));
      };

  /** The client we are listening on. */
  private OCPPWebSocketClient client = null;

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
}
